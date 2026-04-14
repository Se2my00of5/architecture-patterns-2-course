namespace MonitoringService.Models
{
    public class Metric
    {
        public Guid Id { get; set; }
        public string ServiceName { get; set; } = string.Empty;
        public string MetricName { get; set; } = string.Empty; 
        public double Value { get; set; }
        public DateTime Timestamp { get; set; }
    }
}
