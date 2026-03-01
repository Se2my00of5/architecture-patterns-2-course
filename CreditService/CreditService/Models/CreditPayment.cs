namespace CreditService.Models
{
    public class CreditPayment
    {
        public Guid Id { get; set; }
        public Guid CreditId { get; set; }
        public decimal Amount { get; set; }
        public DateTime PaymentDate { get; set; }
        public Guid? TransactionId { get; set; } 

        public Credit Credit { get; set; } = null!;
    }

}
