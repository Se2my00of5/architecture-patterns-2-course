using CreditService.Controllers;
using System.Diagnostics;

namespace CreditService
{

    public class RequestLoggingMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly ILogger<RequestLoggingMiddleware> _logger;
        private static readonly ActivitySource ActivitySource = new("CreditService");

        public RequestLoggingMiddleware(RequestDelegate next, ILogger<RequestLoggingMiddleware> logger)
        {
            _next = next;
            _logger = logger;
        }


        public async Task InvokeAsync(HttpContext context)
        {
            using var activity = ActivitySource.StartActivity("ProcessRequest");
            activity?.SetTag("http.method", context.Request.Method);
            activity?.SetTag("http.path", context.Request.Path);

            var stopwatch = Stopwatch.StartNew();

            await _next(context);

            stopwatch.Stop();
            activity?.SetTag("http.status_code", context.Response.StatusCode);
            activity?.SetTag("elapsed.ms", stopwatch.ElapsedMilliseconds);

            MetricsController.RecordRequest(context.Response.StatusCode);

            _logger.LogInformation(
                "{Method} {Path} -> {StatusCode} in {ElapsedMs}ms",
                context.Request.Method,
                context.Request.Path,
                context.Response.StatusCode,
                stopwatch.ElapsedMilliseconds
            );
        }

    }
}
