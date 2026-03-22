namespace CreditService.Models
{
    public enum PaymentStatus
    {
        Pending,
        Completed,
        Overdue
    }
    public class CreditPayment
    {
        public Guid Id { get; set; }
        public Guid CreditId { get; set; }
        public decimal Amount { get; set; }
        public DateTime PaymentDate { get; set; }
        public DateTime? DueDate { get; set; }  
        public PaymentStatus Status { get; set; } = PaymentStatus.Pending;
        public Guid? TransactionId { get; set; } 

        public Credit Credit { get; set; } = null!;
    }

}
