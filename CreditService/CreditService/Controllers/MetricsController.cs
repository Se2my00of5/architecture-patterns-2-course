using Microsoft.AspNetCore.Mvc;

namespace CreditService.Controllers
{

    [ApiController]
    [Route("api/metrics")]
    public class MetricsController : ControllerBase
    {
        private static int _total = 0;
        private static int _errors = 0;

        public static void RecordRequest(int statusCode)
        {
            _total++;
            if (statusCode >= 500) _errors++;
        }

        [HttpGet("error-rate")]
        public IActionResult GetErrorRate()
        {
            var percentage = _total == 0 ? 0 : (double)_errors / _total * 100;
            return Ok(new { error_percentage = percentage });
        }
    }
}
