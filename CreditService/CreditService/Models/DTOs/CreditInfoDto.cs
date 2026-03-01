namespace CreditService.Models.DTOs
{
    public class CreditInfoDto
    {
        public Guid Id { get; set; }
        public Guid ClientId { get; set; }

        public Guid AccountId { get; set; }

        public string TariffName { get; set; } = string.Empty;
        public decimal InterestRate { get; set; }
        public decimal Amount { get; set; }
        public decimal RemainingAmount { get; set; }
        public decimal MonthlyPayment { get; set; }
        public DateTime StartDate { get; set; }
        public DateTime? EndDate { get; set; }
        public string Status { get; set; } = string.Empty;
        public int PaymentsCount { get; set; }
        public decimal TotalPaid { get; set; }
    }

}
