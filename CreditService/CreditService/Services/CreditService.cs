using Microsoft.EntityFrameworkCore;
using CreditService.Data;
using CreditService.Models;
using CreditService.Models.DTOs;

namespace CreditService.Services
{
    public class CreditService
    {
        private readonly CreditDbContext _context;
        private readonly HttpClient _httpClient;

        public CreditService(CreditDbContext context, HttpClient httpClient)
        {
            _context = context;
            _httpClient = httpClient;
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
        public async Task<Credit> ApplyForCreditAsync(ApplyForCreditDto dto)
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

            return credit;
        }

        public async Task<Credit?> GetCreditByIdAsync(Guid id)
        {
            return await _context.Credits
                .Include(c => c.Tariff)
                .Include(c => c.Payments)
                .FirstOrDefaultAsync(c => c.Id == id);
        }

        public async Task<IEnumerable<Credit>> GetClientCreditsAsync(Guid clientId)
        {
            return await _context.Credits
                .Include(c => c.Tariff)
                .Where(c => c.ClientId == clientId)
                .ToListAsync();
        }

        public async Task<IEnumerable<Credit>> GetAllCreditsAsync()
        {
            return await _context.Credits
                .Include(c => c.Tariff)
                .ToListAsync();
        }

        public async Task<CreditPayment> MakePaymentAsync(MakePaymentDto dto)
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
                PaymentDate = DateTime.UtcNow,
                Type = dto.Type
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
            await NotifyCoreAboutPayment(credit.ClientId, dto.Amount);

            return payment;
        }

        public async Task<IEnumerable<CreditPayment>> GetCreditPaymentsAsync(Guid creditId)
        {
            return await _context.CreditPayments
                .Where(p => p.CreditId == creditId)
                .OrderByDescending(p => p.PaymentDate)
                .ToListAsync();
        }

        // Расчеты
        public async Task<decimal> CalculateMonthlyPaymentAsync(decimal amount, decimal interestRate, int months)
        {
            // Аннуитетный платеж
            var monthlyRate = interestRate / 100 / 12;
            if (monthlyRate == 0) return amount / months;

            var factor = (decimal)Math.Pow(1 + (double)monthlyRate, months);
            return amount * monthlyRate * factor / (factor - 1);
        }

        public async Task ProcessDailyPaymentsAsync()
        {
            var activeCredits = await _context.Credits
                .Where(c => c.Status == CreditStatus.Active)
                .ToListAsync();

            foreach (var credit in activeCredits)
            {
                // Здесь логика ежедневного списания для тестирования
                // В реальности тут было бы списание раз в месяц

                var payment = new MakePaymentDto
                {
                    CreditId = credit.Id,
                    Amount = credit.MonthlyPayment / 30, // Ежедневный платеж для теста
                    Type = PaymentType.Scheduled
                };

                try
                {
                    await MakePaymentAsync(payment);
                }
                catch (Exception ex)
                {
                    // Здесь можно пометить кредит как просроченный
                }
            }
        }

        // Вспомогательные методы для взаимодействия с другими сервисами
        private async Task<bool> CheckClientExistsAsync(Guid clientId)
        {
            try
            {
                // Запрос к сервису пользователей
                var response = await _httpClient.GetAsync($"http://user-service/api/users/{clientId}");
                return response.IsSuccessStatusCode;
            }
            catch
            {
                return false; 
            }
        }

        private async Task NotifyCoreAboutCreditIssuance(Credit credit)
        {
            try
            {
                // Уведомление ядра о выдаче кредита
                var request = new
                {
                    credit.Amount
                };

                //await _httpClient.PostAsJsonAsync($"http://localhost:1111/api/accounts/{credit.ClientId}/deposit", request);
                string url = string.Format("http://localhost:1111/api/accounts/{0}/deposit", credit.ClientId);
                await _httpClient.PostAsJsonAsync(url, request);
            }
            catch (Exception ex)
            {
            }
        }

        private async Task NotifyCoreAboutPayment(Guid clientId, decimal amount)
        {
            try
            {
                var request = new
                {
                    Amount = amount
                };

                //await _httpClient.PostAsJsonAsync("http://core-service/api/transactions/debit", request);
                string url = string.Format("http://localhost:1111/api/accounts/{0}/withdraw", clientId);
                await _httpClient.PostAsJsonAsync(url, request);
            }
            catch (Exception ex)
            {
            }
        }
    }


}
