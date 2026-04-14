using Microsoft.AspNetCore.Mvc;
using MonitoringService.Models;

namespace MonitoringService.Controllers
{
    [ApiController]
    [Route("api/telemetry")]
    public class TelemetryController : ControllerBase
    {
        private readonly MonitoringDbContext _db;
        private readonly ILogger<TelemetryController> _logger;

        public TelemetryController(MonitoringDbContext db, ILogger<TelemetryController> logger)
        {
            _db = db;
            _logger = logger;
        }

        [HttpPost("trace")]
        public async Task<IActionResult> ReceiveTrace([FromBody] TraceLog trace)
        {
            trace.Id = Guid.NewGuid();
            trace.Timestamp = DateTime.UtcNow;

            _db.TraceLogs.Add(trace);
            await _db.SaveChangesAsync();

            return Ok();
        }

        [HttpPost("error")]
        public async Task<IActionResult> ReceiveError([FromBody] ErrorLog error)
        {
            error.Id = Guid.NewGuid();
            error.Timestamp = DateTime.UtcNow;

            _db.ErrorLogs.Add(error);
            await _db.SaveChangesAsync();

            return Ok();
        }

        [HttpPost("metric")]
        public async Task<IActionResult> ReceiveMetric([FromBody] Metric metric)
        {
            metric.Id = Guid.NewGuid();
            metric.Timestamp = DateTime.UtcNow;

            _db.Metrics.Add(metric);
            await _db.SaveChangesAsync();

            return Ok();
        }
    }
}
