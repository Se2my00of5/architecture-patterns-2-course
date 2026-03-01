using System.ComponentModel.DataAnnotations;

namespace CreditService.Models.DTOs
{
    public class MakePaymentDto
    {
        [Required]
        public Guid CreditId { get; set; }

        public Guid AccountId { get; set; }

        [Required]
        [Range(1, double.MaxValue)]
        public decimal Amount { get; set; }

        public PaymentType Type { get; set; } = PaymentType.Scheduled;
    }
}
