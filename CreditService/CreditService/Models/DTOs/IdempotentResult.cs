namespace CreditService.Models.DTOs
{
    public class IdempotentResult
    {
        public int StatusCode { get; set; }
        public string ResponseBody { get; set; } = string.Empty;
    }
}
