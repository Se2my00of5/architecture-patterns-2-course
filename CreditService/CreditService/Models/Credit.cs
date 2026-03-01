namespace CreditService.Models
{
    public class Credit
    {
        public Guid Id { get; set; }
        public Guid ClientId { get; set; }
        public Guid AccountId { get; set; }

        public Guid TariffId { get; set; }
        public decimal Amount { get; set; } 
        public decimal RemainingAmount { get; set; } 
        public decimal MonthlyPayment { get; set; } 
        public DateTime StartDate { get; set; }
        public DateTime? EndDate { get; set; }
        public CreditStatus Status { get; set; }
        public DateTime CreatedAt { get; set; }

        public CreditTariff Tariff { get; set; } = null!;
        public ICollection<CreditPayment> Payments { get; set; } = new List<CreditPayment>();
    }

    public enum CreditStatus
    {
        Active,      
        Paid,        
        Overdue,     
        Defaulted    
    }
}
