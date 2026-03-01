using CreditService.Data;
using CreditService.Models;
using CreditService.Models.DTOs;
using Microsoft.EntityFrameworkCore;
using System.Text.Json;

namespace CreditService.Services
{
    public class CreditService
    {
        private readonly CreditDbContext _context;
        private readonly HttpClient _httpClient;
        private readonly ILogger<CreditService> _logger;

        public CreditService(CreditDbContext context, HttpClient httpClient, ILogger<CreditService> logger)
        {
            _context = context;
            _httpClient = httpClient;
            _logger = logger;
        }

        // Управление тарифами
        public async Task<CreditTariff> CreateTariffAsync(CreateTariffDto dto)
        {
            var tariff = new CreditTariff
            {
                Id = Guid.NewGuid(),
                Name = dto.Name,
                InterestRate = dto.InterestRate,
                CreatedAt = DateTime.UtcNow,
                IsActive = true
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
            //// Проверяем существование клиента через API сервиса пользователей
            //var clientExists = await CheckClientExistsAsync(dto.ClientId);
            //if (!clientExists)
            //{
            //    throw new InvalidOperationException("Client not found");
            //}

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
                RemainingAmount = dto.Amount,
                MonthlyPayment = monthlyPayment,
                StartDate = DateTime.UtcNow,
                Status = CreditStatus.Active,
                CreatedAt = DateTime.UtcNow
            };

            _context.Credits.Add(credit);
            await _context.SaveChangesAsync();

            // Отправляем запрос в ядро для создания счета и зачисления средств
            await NotifyCoreAboutCreditIssuance(credit);

            var cred = new CreditResponseDto
            {
                Id = credit.Id,
                ClientId = credit.ClientId,
                TariffId = credit.TariffId,
                Amount = credit.Amount,
                RemainingAmount = credit.Amount,
                MonthlyPayment = monthlyPayment,
                StartDate = credit.StartDate

            };

            return cred;
        }

        //public async Task<Credit?> GetCreditByIdAsync(Guid id)
        //{
        //    return await _context.Credits
        //        .Include(c => c.Tariff)
        //        .Include(c => c.Payments)
        //        .FirstOrDefaultAsync(c => c.Id == id);
        //}

        //public async Task<IEnumerable<Credit>> GetClientCreditsAsync(Guid clientId)
        //{
        //    return await _context.Credits
        //        .Include(c => c.Tariff)
        //        .Where(c => c.ClientId == clientId)
        //        .ToListAsync();
        //}

        //public async Task<IEnumerable<Credit>> GetAllCreditsAsync()
        //{
        //    return await _context.Credits
        //        .Include(c => c.Tariff)
        //        .ToListAsync();
        //}

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
            var credit = await _context.Credits
                .Include(c => c.Payments)
                .FirstOrDefaultAsync(c => c.Id == dto.CreditId);

            if (credit == null)
            {
                throw new InvalidOperationException("Credit not found");
            }

            if (credit.Status != CreditStatus.Active)
            {
                throw new InvalidOperationException("Credit is not active");
            }

            // Создаем платеж
            var payment = new CreditPayment
            {
                Id = Guid.NewGuid(),
                CreditId = credit.Id,
                Amount = dto.Amount,
                PaymentDate = DateTime.UtcNow
            };

            // Обновляем остаток
            credit.RemainingAmount -= dto.Amount;

            // Проверяем, погашен ли кредит полностью
            if (credit.RemainingAmount <= 0)
            {
                credit.RemainingAmount = 0;
                credit.Status = CreditStatus.Paid;
                credit.EndDate = DateTime.UtcNow;
            }

            _context.CreditPayments.Add(payment);
            await _context.SaveChangesAsync();

            // Отправляем запрос в ядро для списания средств
            await NotifyCoreAboutPayment(dto.AccountId, credit.Id, dto.Amount);

            var paymentDto = new CreditPaymentDto
            {
                Id = payment.Id,
                CreditId = payment.CreditId,
                Amount = payment.Amount,
                PaymentDate = payment.PaymentDate
            };

            return paymentDto;
        }

        //public async Task<IEnumerable<CreditPayment>> GetCreditPaymentsAsync(Guid creditId)
        //{
        //    return await _context.CreditPayments
        //        .Where(p => p.CreditId == creditId)
        //        .OrderByDescending(p => p.PaymentDate)
        //        .ToListAsync();
        //}
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

        //public async Task ProcessDailyPaymentsAsync()
        //{
        //    var activeCredits = await _context.Credits
        //        .Where(c => c.Status == CreditStatus.Active)
        //        .ToListAsync();

        //    foreach (var credit in activeCredits)
        //    {

        //        var payment = new MakePaymentDto
        //        {
        //            CreditId = credit.Id,
        //            AccountId = credit.AccountId,
        //            Amount = credit.MonthlyPayment / (30 * 24 * 60) // Ежеминутный платеж для теста
        //        };

        //        try
        //        {

        //            await MakePaymentAsync(payment);
        //        }
        //        catch (Exception ex)
        //        {
        //            credit.Status = CreditStatus.Overdue;
        //            await _context.SaveChangesAsync();
        //        }

        //    }
        //}
        public async Task ProcessDailyPaymentsAsync()
        {
            var activeCredits = await _context.Credits
                .Where(c => c.Status == CreditStatus.Active)
                .ToListAsync();

            foreach (var credit in activeCredits)
            {
                var payment = new MakePaymentDto
                {
                    CreditId = credit.Id,
                    AccountId = credit.AccountId,
                    Amount = credit.MonthlyPayment / (30 * 24 * 60) // Ежеминутный платеж
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

        // Вспомогательные методы для взаимодействия с другими сервисами

        private async Task NotifyCoreAboutCreditIssuance(Credit credit)
        {
            try
            {
                var request = new
                {
                    creditId = credit.Id,
                    amount = credit.Amount
                };

                string url = string.Format("http://core-service-backend:1111/api/accounts/{0}/loan-disbursement", credit.AccountId);

                // Отправляем запрос и получаем ответ
                var response = await _httpClient.PostAsJsonAsync(url, request);

                var responseBody = await response.Content.ReadAsStringAsync();

                if (response.IsSuccessStatusCode)
                {
                    // Успешный ответ (200-299)
                    _logger.LogInformation($"Core service успешно обработал выдачу кредита {credit.Id}. Ответ: {responseBody}");

                }
                else
                {
                    // Ошибка от Core Service
                    _logger.LogError($"Core service вернул ошибку {response.StatusCode} для кредита {credit.Id}. Тело ответа: {responseBody}");

                    // Здесь можно добавить логику повторной отправки или компенсации
                    throw new Exception($"Core service error: {response.StatusCode} - {responseBody}");
                }
            }
            catch (HttpRequestException ex)
            {
                _logger.LogError(ex, $"Сетевая ошибка при обращении к Core Service для кредита {credit.Id}");


                throw;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Неожиданная ошибка при уведомлении Core Service о кредите {credit.Id}");
                throw;
            }
        }


        private async Task NotifyCoreAboutPayment(Guid accountId, Guid id, decimal amount_)
        {
            var request = new
            {
                creditId = id,
                amount = amount_
            };

            string url = string.Format("http://core-service-backend:1111/api/accounts/{0}/loan-repayment", accountId);
            await _httpClient.PostAsJsonAsync(url, request);


        }
    }


}
