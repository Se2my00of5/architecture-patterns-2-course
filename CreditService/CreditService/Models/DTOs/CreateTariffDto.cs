using System.ComponentModel.DataAnnotations;

namespace CreditService.Models.DTOs
{
    public class CreateTariffDto
    {
        [Required]
        [StringLength(100, MinimumLength = 3)]
        public string Name { get; set; } = string.Empty;

        [Required]
        [Range(0.01, 100.0)]
        public decimal InterestRate { get; set; }
    }
}
