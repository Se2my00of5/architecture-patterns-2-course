using CreditService.Data;
using CreditService.Models;
using CreditService.Models.DTOs;
using Microsoft.EntityFrameworkCore;
using System.Net.Http.Headers;
using System.Text.Json;

namespace CreditService.Services
{
    public class CreditService
    {
        private readonly CreditDbContext _context;
        private readonly HttpClient _httpClient;
        private readonly ILogger<CreditService> _logger;
        private readonly TokenService _tokenService;

        public CreditService(CreditDbContext context, HttpClient httpClient, ILogger<CreditService> logger, TokenService tokenService)
        {
            _context = context;
            _httpClient = httpClient;
            _logger = logger;
            _tokenService = tokenService;
        }

        // Управление тарифами
        public async Task<CreditTariff> CreateTariffAsync(CreateTariffDto dto)
        {

            var existing = await _context.CreditTariffs.FirstOrDefaultAsync(t => t.IdempotencyKey == dto.IdempotencyKey);
            if (existing != null)
                return existing;


            var tariff = new CreditTariff
            {
                Id = Guid.NewGuid(),
                Name = dto.Name,
                InterestRate = dto.InterestRate,
                CreatedAt = DateTime.UtcNow,
                IsActive = true
                IdempotencyKey = dto.IdempotencyKey
            };

            _context.CreditTariffs.Add(tariff);
            await _context.SaveChangesAsync();


            return tariff;
        }

        public async Task<IEnumerable<CreditTariff>> GetAllTariffsAsync(bool onlyActive = true)
        {
            var query = _context.CreditTariffs.AsQueryable();

            if (onlyActive)
            {
                query = query.Where(t => t.IsActive);
            }

            return await query.ToListAsync();
        }

        public async Task<CreditTariff?> GetTariffByIdAsync(Guid id)
        {
            return await _context.CreditTariffs.FindAsync(id);
        }

        public async Task<bool> DeactivateTariffAsync(Guid id)
        {
            var tariff = await _context.CreditTariffs.FindAsync(id);
            if (tariff == null) return false;

            tariff.IsActive = false;
            tariff.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();
            return true;
        }

        // Работа с кредитами
        public async Task<CreditResponseDto> ApplyForCreditAsync(ApplyForCreditDto dto)
        {
            var existing = await _context.Credits.FirstOrDefaultAsync(c => c.IdempotencyKey == dto.IdempotencyKey);
            if (existing != null)
            {
                return new CreditResponseDto
                {
                    Id = existing.Id,
                    ClientId = existing.ClientId,
                    AccountId = existing.AccountId,
                    TariffId = existing.TariffId,
                    Amount = existing.Amount,
                    RemainingAmount = existing.RemainingAmount,
                    MonthlyPayment = existing.MonthlyPayment,
                    StartDate = existing.StartDate
                };
            }



            // Получаем тариф
            var tariff = await _context.CreditTariffs.FindAsync(dto.TariffId);
            if (tariff == null || !tariff.IsActive)
            {
                throw new InvalidOperationException("Tariff not found or inactive");
            }

            // Рассчитываем ежемесячный платеж
            var monthlyPayment = await CalculateMonthlyPaymentAsync(
                dto.Amount,
                tariff.InterestRate,
                dto.TermInMonths
            );

            var credit = new Credit
            {
                Id = Guid.NewGuid(),
                ClientId = dto.ClientId,
                AccountId = dto.AccountId,
                TariffId = dto.TariffId,
                Amount = dto.Amount,
                RemainingAmount = monthlyPayment * dto.TermInMonths,
                MonthlyPayment = monthlyPayment,
                StartDate = DateTime.UtcNow,
                Status = CreditStatus.Active,
                CreatedAt = DateTime.UtcNow,
                IdempotencyKey = dto.IdempotencyKey
            };

            _context.Credits.Add(credit);
            await _context.SaveChangesAsync();

            await NotifyCoreAboutCreditIssuance(credit);

            var cred = new CreditResponseDto
            {
                Id = credit.Id,
                ClientId = credit.ClientId,
                AccountId = credit.AccountId,
                TariffId = credit.TariffId,
                Amount = credit.Amount,
                RemainingAmount = credit.Amount,
                MonthlyPayment = monthlyPayment,
                StartDate = credit.StartDate

            };

            return cred;
        }


        public async Task<CreditInfoDto?> GetCreditByIdAsync(Guid id)
        {
            var credit = await _context.Credits
                .Include(c => c.Tariff)
                .Include(c => c.Payments)
                .FirstOrDefaultAsync(c => c.Id == id);

            if (credit == null)
                return null;

            return MapToCreditInfoDto(credit);
        }

        public async Task<IEnumerable<CreditInfoDto>> GetClientCreditsAsync(Guid clientId)
        {
            var credits = await _context.Credits
                .Include(c => c.Tariff)
                .Include(c => c.Payments)
                .Where(c => c.ClientId == clientId)
                .ToListAsync();

            return credits.Select(MapToCreditInfoDto);
        }

        public async Task<IEnumerable<CreditInfoDto>> GetAllCreditsAsync()
        {
            var credits = await _context.Credits
                .Include(c => c.Tariff)
                .Include(c => c.Payments)
                .ToListAsync();

            return credits.Select(MapToCreditInfoDto);
        }

        // Приватный метод для маппинга
        private CreditInfoDto MapToCreditInfoDto(Credit credit)
        {
            return new CreditInfoDto
            {
                Id = credit.Id,
                ClientId = credit.ClientId,
                AccountId = credit.AccountId,
                TariffName = credit.Tariff?.Name ?? "Unknown",
                InterestRate = credit.Tariff?.InterestRate ?? 0,
                Amount = credit.Amount,
                RemainingAmount = credit.RemainingAmount,
                MonthlyPayment = credit.MonthlyPayment,
                StartDate = credit.StartDate,
                EndDate = credit.EndDate,
                Status = credit.Status.ToString(),
                PaymentsCount = credit.Payments?.Count ?? 0,
                TotalPaid = credit.Payments?.Sum(p => p.Amount) ?? 0
            };
        }

        public async Task<CreditPaymentDto> MakePaymentAsync(MakePaymentDto dto)
        {
            var existing = await _context.CreditPayments.FirstOrDefaultAsync(p => p.IdempotencyKey == dto.IdempotencyKey);
            if (existing != null)
            {
                return MapToCreditPaymentDto(existing);
            }


            var credit = await _context.Credits
                .Include(c => c.Payments)
                .FirstOrDefaultAsync(c => c.Id == dto.CreditId);

            if (credit == null)
            {
                throw new InvalidOperationException("Credit not found");
            }

            // Ищем просроченные платежи
            var overduePayment = credit.Payments
                .FirstOrDefault(p => p.Status == PaymentStatus.Overdue && p.DueDate < DateTime.UtcNow);

            var payment = new CreditPayment
            {
                Id = Guid.NewGuid(),
                CreditId = credit.Id,
                Amount = overduePayment?.Amount ?? dto.Amount,
                PaymentDate = DateTime.UtcNow,
                DueDate = DateTime.UtcNow.AddDays(30),
                Status = PaymentStatus.Pending
                IdempotencyKey = dto.IdempotencyKey
            };

            var result = await NotifyCoreAboutPayment(dto.AccountId, credit.Id, payment.Amount);

            if (result)
            {
                payment.Status = PaymentStatus.Completed;

                if (overduePayment != null)
                {
                    overduePayment.Status = PaymentStatus.Completed;
                    overduePayment.PaymentDate = DateTime.UtcNow;
                }

                credit.RemainingAmount -= payment.Amount;

                if (credit.RemainingAmount <= 0)
                {
                    credit.RemainingAmount = 0;
                    credit.Status = CreditStatus.Paid;
                    credit.EndDate = DateTime.UtcNow;
                }

                _context.CreditPayments.Add(payment);
                await _context.SaveChangesAsync();
            }
            else
            {
                payment.Status = PaymentStatus.Overdue;
                _context.CreditPayments.Add(payment);
                await _context.SaveChangesAsync();
                credit.Status = CreditStatus.Overdue;
                await _context.SaveChangesAsync();
            }

            return MapToCreditPaymentDto(payment);
        }


        public async Task<IEnumerable<CreditPaymentDto>> GetCreditPaymentsAsync(Guid creditId)
        {
            var payments = await _context.CreditPayments
                .Where(p => p.CreditId == creditId)
                .OrderByDescending(p => p.PaymentDate)
                .ToListAsync();

            return payments.Select(MapToCreditPaymentDto);
        }
        private CreditPaymentDto MapToCreditPaymentDto(CreditPayment payment)
        {
            return new CreditPaymentDto
            {
                Id = payment.Id,
                CreditId = payment.CreditId,
                Amount = payment.Amount,
                PaymentDate = payment.PaymentDate,
                TransactionId = payment.TransactionId
            };
        }

        // Расчеты
        public async Task<decimal> CalculateMonthlyPaymentAsync(decimal amount, decimal interestRate, int months)
        {
            var monthlyRate = interestRate / 100 / 12;
            if (monthlyRate == 0) return amount / months;

            var factor = (decimal)Math.Pow(1 + (double)monthlyRate, months);
            return amount * monthlyRate * factor / (factor - 1);
        }

        public async Task ProcessDailyPaymentsAsync()
        {
            var activeCredits = await _context.Credits
                .Where(c => c.Status == CreditStatus.Active || c.Status == CreditStatus.Overdue)
                .ToListAsync();

            foreach (var credit in activeCredits)
            {
                var payment = new MakePaymentDto
                {
                    CreditId = credit.Id,
                    AccountId = credit.AccountId,
                    Amount = credit.MonthlyPayment // Ежеминутный платеж
                };

                try
                {
                    await MakePaymentAsync(payment);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, $"Ошибка платежа для кредита {credit.Id}");
                    credit.Status = CreditStatus.Overdue;
                    await _context.SaveChangesAsync();
                }
            }

        }

        private async Task NotifyCoreAboutCreditIssuance(Credit credit)
        {
            try
            {
                _logger.LogInformation("=== START NotifyCoreAboutCreditIssuance ===");
                _logger.LogInformation("Credit ID: {CreditId}, Account ID: {AccountId}, Amount: {Amount}",
                    credit.Id, credit.AccountId, credit.Amount);

                var token = await _tokenService.GetServiceTokenAsync();
                _logger.LogInformation("Token obtained successfully. Token prefix: {TokenPrefix}",
                    token?.Substring(0, Math.Min(50, token?.Length ?? 0)));

                var request = new
                {
                    creditId = credit.Id,
                    amount = credit.Amount
                };

                _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

                string url = string.Format("http://core-service-backend:1111/api/accounts/{0}/loan-disbursement", credit.AccountId);
                _logger.LogInformation("Sending POST request to: {Url}", url);
                _logger.LogInformation("Request body: {@Request}", request);

                // Отправляем запрос и получаем ответ
                var response = await _httpClient.PostAsJsonAsync(url, request);

                var responseBody = await response.Content.ReadAsStringAsync();
                _logger.LogInformation("Response status code: {StatusCode}", response.StatusCode);
                _logger.LogInformation("Response body: {ResponseBody}", responseBody);

                if (response.IsSuccessStatusCode)
                {
                    _logger.LogInformation("Core service successfully processed credit disbursement {CreditId}. Response: {ResponseBody}",
                        credit.Id, responseBody);
                }
                else
                {
                    _logger.LogError("Failed to notify core service about credit {CreditId}. Status: {StatusCode}, Body: {ResponseBody}",
                        credit.Id, response.StatusCode, responseBody);


                }

                _logger.LogInformation("=== END NotifyCoreAboutCreditIssuance ===");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in NotifyCoreAboutCreditIssuance for credit {CreditId}", credit.Id);
            }
        }

        private async Task<bool> NotifyCoreAboutPayment(Guid accountId, Guid id, decimal amount_)
        {
            var token = await _tokenService.GetServiceTokenAsync();

            _logger.LogInformation("Sending notification to core-service. Token prefix: {TokenPrefix}",
                token?.Substring(0, Math.Min(20, token?.Length ?? 0)));

            var request = new
            {
                creditId = id,
                amount = amount_
            };

            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

            string url = string.Format("http://core-service-backend:1111/api/accounts/{0}/loan-repayment", accountId);

            //  Логируем URL и заголовки
            _logger.LogInformation("POST {Url}, Authorization: {Auth}",
                url, _httpClient.DefaultRequestHeaders.Authorization?.ToString());

            var response = await _httpClient.PostAsJsonAsync(url, request);

            var responseBody = await response.Content.ReadAsStringAsync();

            //  Логируем ответ
            _logger.LogInformation("Core-service response: {StatusCode}, Body: {Body}",
                response.StatusCode, responseBody);

            if (response.IsSuccessStatusCode)
            {
                return true;
            }

            return false;
        }



        // Получение просроченных платежей по кредиту
        public async Task<IEnumerable<CreditPaymentDto>> GetOverduePaymentsAsync(Guid creditId)
        {
            var payments = await _context.CreditPayments
                .Where(p => p.CreditId == creditId &&
                            p.Status == PaymentStatus.Overdue &&
                            p.DueDate < DateTime.UtcNow)
                .OrderBy(p => p.DueDate)
                .ToListAsync();

            return payments.Select(MapToCreditPaymentDto);
        }

        // Получение всех просроченных платежей клиента
        public async Task<IEnumerable<CreditPaymentDto>> GetClientOverduePaymentsAsync(Guid clientId)
        {
            var payments = await _context.CreditPayments
                .Include(p => p.Credit)
                .Where(p => p.Credit.ClientId == clientId &&
                            p.Status == PaymentStatus.Overdue &&
                            p.DueDate < DateTime.UtcNow)
                .OrderBy(p => p.DueDate)
                .ToListAsync();

            return payments.Select(MapToCreditPaymentDto);
        }



        // Расчет кредитного рейтинга
        public async Task<CreditRatingDto> GetCreditRatingAsync(Guid clientId)
        {
            var credits = await _context.Credits
                .Include(c => c.Payments)
                .Where(c => c.ClientId == clientId)
                .ToListAsync();

            if (!credits.Any())
            {
                return new CreditRatingDto
                {
                    ClientId = clientId,
                    Score = 700, // Начальный рейтинг
                    Grade = "C",
                    TotalCredits = 0,
                    PaidCredits = 0,
                    OverduePayments = 0,
                    OnTimePaymentRate = 0
                };
            }

            var totalCredits = credits.Count;
            var paidCredits = credits.Count(c => c.Status == CreditStatus.Paid);
            var allPayments = credits.SelectMany(c => c.Payments).ToList();
            var totalPayments = allPayments.Count;
            var overduePayments = allPayments.Count(p => p.Status == PaymentStatus.Overdue);
            var onTimePayments = allPayments.Count(p => p.Status == PaymentStatus.Completed);

            var score = 700;
            score += (paidCredits * 50);
            score -= (overduePayments * 25);

            if (totalPayments > 0)
            {
                var onTimeRate = (decimal)onTimePayments / totalPayments;
                score += (int)(onTimeRate * 100);

                if (onTimeRate > 0.95m) score += 50;
                else if (onTimeRate > 0.8m) score += 20;
                else if (onTimeRate < 0.5m) score -= 50;
            }

            score = Math.Max(300, Math.Min(850, score));

            string grade = score switch
            {
                >= 750 => "A",
                >= 700 => "B",
                >= 650 => "C",
                >= 600 => "D",
                _ => "F"
            };

            return new CreditRatingDto
            {
                ClientId = clientId,
                Score = score,
                Grade = grade,
                TotalCredits = totalCredits,
                PaidCredits = paidCredits,
                OverduePayments = overduePayments,
                OnTimePaymentRate = totalPayments > 0
                    ? Math.Round((decimal)onTimePayments / totalPayments * 100, 2)
                    : 0
            };
        }

       




    }
}
