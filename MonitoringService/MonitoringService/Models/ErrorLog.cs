namespace MonitoringService.Models
{
    public class ErrorLog
    {
        public Guid Id { get; set; }
        public string? ServiceName { get; set; } = string.Empty;
        public string? TraceId { get; set; } = string.Empty;
        public string? ErrorMessage { get; set; } = string.Empty;
        public string? StackTrace { get; set; } = string.Empty;
        public DateTime Timestamp { get; set; }
    }
}
