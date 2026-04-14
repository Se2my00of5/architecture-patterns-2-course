namespace CreditService.Models
{
    public class IdempotentRequest
    {
        public Guid Id { get; set; }
        public string Key { get; set; }
        public string Method { get; set; } 
        public string Path { get; set; }
        public string ResponseBody { get; set; }
        public int StatusCode { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime ExpiresAt { get; set; }
    }

}
