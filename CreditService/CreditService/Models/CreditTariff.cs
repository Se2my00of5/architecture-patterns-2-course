namespace CreditService.Models
{
    public class CreditTariff
    {
        public Guid Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public decimal InterestRate { get; set; } 
        public bool IsActive { get; set; } = true;
        public DateTime CreatedAt { get; set; }
        public DateTime? UpdatedAt { get; set; }

        public ICollection<Credit> Credits { get; set; } = new List<Credit>();
    }
}
