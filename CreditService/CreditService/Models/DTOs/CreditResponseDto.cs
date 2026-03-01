namespace CreditService.Models.DTOs
{
    public class CreditResponseDto
    {
        public Guid Id { get; set; }
        public Guid ClientId { get; set; }

        public Guid AccountId { get; set; }
        public Guid TariffId { get; set; }
        public decimal Amount { get; set; }
        public decimal RemainingAmount { get; set; }
        public decimal MonthlyPayment { get; set; }
        public DateTime StartDate { get; set; }
    }
}
