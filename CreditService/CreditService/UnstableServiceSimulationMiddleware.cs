namespace CreditService
{
    public class UnstableServiceSimulationMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly ILogger<UnstableServiceSimulationMiddleware> _logger;
        private readonly Random _random = new Random();

        public UnstableServiceSimulationMiddleware(
            RequestDelegate next,
            ILogger<UnstableServiceSimulationMiddleware> logger)
        {
            _next = next;
            _logger = logger;
        }

        public async Task InvokeAsync(HttpContext context)
        {
            if (ShouldSimulateError())
            {
                _logger.LogWarning("Simulating 500 error for request {Path} at {Time}",
                    context.Request.Path, DateTime.Now);

                context.Response.StatusCode = StatusCodes.Status500InternalServerError;
                await context.Response.WriteAsJsonAsync(new
                {
                    error = "Service temporarily unavailable",
                    timestamp = DateTime.Now
                });
                return;
            }

            await _next(context);
        }

        private bool ShouldSimulateError()
        {
            var now = DateTime.Now;
            bool isEvenMinute = now.Minute % 2 == 0;

            double errorProbability = 0.3;

            if (isEvenMinute)
            {
                errorProbability = 0.7;
            }

            double randomValue = _random.NextDouble();

            return randomValue < errorProbability;
        }
    }
}
