namespace MonitoringService.Models
{
    public class TraceLog
    {
        public Guid Id { get; set; }
        public string ServiceName { get; set; } = string.Empty;  // CreditService, CoreService
        public string TraceId { get; set; } = string.Empty;
        public string Method { get; set; } = string.Empty;       // GET, POST
        public string Path { get; set; } = string.Empty;
        public int StatusCode { get; set; }
        public long ElapsedMs { get; set; }
        public bool IsError => StatusCode >= 400;
        public DateTime Timestamp { get; set; }
    }
}
