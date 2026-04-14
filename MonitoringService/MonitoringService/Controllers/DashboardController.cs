using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace MonitoringService.Controllers
{
    [ApiController]
    [Route("api/dashboard")]
    public class DashboardController : ControllerBase
    {
        private readonly MonitoringDbContext _db;

        public DashboardController(MonitoringDbContext db)
        {
            _db = db;
        }

        [HttpGet("stats")]
        public async Task<IActionResult> GetStats([FromQuery] int hours = 24)
        {
            var since = DateTime.UtcNow.AddHours(-hours);

            var stats = new
            {
                total_requests = await _db.TraceLogs.CountAsync(t => t.Timestamp >= since),
                error_requests = await _db.TraceLogs.CountAsync(t => t.Timestamp >= since && t.IsError),
                error_percentage = 0.0,
                avg_response_time = await _db.TraceLogs.Where(t => t.Timestamp >= since).AverageAsync(t => t.ElapsedMs),
                services = await _db.TraceLogs.Where(t => t.Timestamp >= since).Select(t => t.ServiceName).Distinct().ToListAsync()
            };

            var errorPercentage = stats.total_requests == 0 ? 0 : (double)stats.error_requests / stats.total_requests * 100;

            return Ok(new { stats, error_percentage = errorPercentage });
        }

        [HttpGet("requests-timeline")]
        public async Task<IActionResult> GetRequestsTimeline([FromQuery] int hours = 24)
        {
            var since = DateTime.UtcNow.AddHours(-hours);

            var data = await _db.TraceLogs
                .Where(t => t.Timestamp >= since)
                .GroupBy(t => new { t.Timestamp.Year, t.Timestamp.Month, t.Timestamp.Day, t.Timestamp.Hour, t.Timestamp.Minute })
                .Select(g => new
                {
                    timestamp = g.Key,
                    total = g.Count(),
                    errors = g.Count(t => t.IsError)
                })
                .OrderBy(g => g.timestamp)
                .ToListAsync();

            return Ok(data);
        }

        [HttpGet("response-time")]
        public async Task<IActionResult> GetResponseTime([FromQuery] int hours = 24)
        {
            var since = DateTime.UtcNow.AddHours(-hours);

            var data = await _db.TraceLogs
                .Where(t => t.Timestamp >= since)
                .GroupBy(t => t.ServiceName)
                .Select(g => new
                {
                    service = g.Key,
                    avg_ms = g.Average(t => t.ElapsedMs),
                    min_ms = g.Min(t => t.ElapsedMs),
                    max_ms = g.Max(t => t.ElapsedMs),
                    total_requests = g.Count()
                })
                .ToListAsync();

            return Ok(data);
        }

        [HttpGet("error-rate")]
        public async Task<IActionResult> GetErrorRate([FromQuery] int hours = 24)
        {
            var since = DateTime.UtcNow.AddHours(-hours);

            var data = await _db.TraceLogs
                .Where(t => t.Timestamp >= since)
                .GroupBy(t => t.ServiceName)
                .Select(g => new
                {
                    service = g.Key,
                    total = g.Count(),
                    errors = g.Count(t => t.IsError),
                    error_percentage = g.Count() == 0 ? 0 : (double)g.Count(t => t.IsError) / g.Count() * 100
                })
                .ToListAsync();

            return Ok(data);
        }

        [HttpGet("recent-errors")]
        public async Task<IActionResult> GetRecentErrors([FromQuery] int limit = 50)
        {
            var errors = await _db.ErrorLogs
                .OrderByDescending(e => e.Timestamp)
                .Take(limit)
                .ToListAsync();

            return Ok(errors);
        }
    }
}
