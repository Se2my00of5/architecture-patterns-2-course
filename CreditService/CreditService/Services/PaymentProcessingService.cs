namespace CreditService.Services
{
    public class PaymentProcessingService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<PaymentProcessingService> _logger;
        private readonly TimeSpan _period = TimeSpan.FromMinutes(1);

        public PaymentProcessingService(IServiceProvider serviceProvider, ILogger<PaymentProcessingService> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("Сервис обработки платежей запущен");

            using var timer = new PeriodicTimer(_period);

            while (await timer.WaitForNextTickAsync(stoppingToken))
            {
                try
                {
                    _logger.LogInformation("Запуск обработки ежеминутных платежей...");
                    using (var scope = _serviceProvider.CreateScope())
                    {
                        var creditService = scope.ServiceProvider.GetRequiredService<CreditService>();
                        await creditService.ProcessDailyPaymentsAsync();
                    }

                    _logger.LogInformation("Обработка платежей завершена");
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Ошибка при обработке платежей");
                }
            }
        }

        public override async Task StopAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("Сервис обработки платежей останавливается");
            await base.StopAsync(stoppingToken);
        }
    }
}
