namespace CreditService.Models.DTOs
{
    public class CreditRatingDto
    {
        public Guid ClientId { get; set; }
        public int Score { get; set; }  
        public string Grade { get; set; } 
        public int TotalCredits { get; set; }
        public int PaidCredits { get; set; }
        public int OverduePayments { get; set; }
        public decimal OnTimePaymentRate { get; set; }
    }
}
