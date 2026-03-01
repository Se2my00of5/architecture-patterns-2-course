using System.ComponentModel.DataAnnotations;

namespace CreditService.Models.DTOs
{
    public class ApplyForCreditDto
    {
        [Required]
        public Guid ClientId { get; set; }

        [Required]
        public Guid AccountId { get; set; }

        [Required]
        public Guid TariffId { get; set; }

        [Required]
        [Range(1000, 10000000)]
        public decimal Amount { get; set; }

        [Required]
        [Range(1, 60)]
        public int TermInMonths { get; set; }
    }
}
