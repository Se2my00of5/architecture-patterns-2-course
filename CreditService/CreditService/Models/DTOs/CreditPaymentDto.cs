namespace CreditService.Models.DTOs
{
    public class CreditPaymentDto
    {
        public Guid Id { get; set; }
        public Guid CreditId { get; set; }
        public decimal Amount { get; set; }
        public DateTime PaymentDate { get; set; }
        public PaymentType Type { get; set; }
        public Guid? TransactionId { get; set; }

    }
}
