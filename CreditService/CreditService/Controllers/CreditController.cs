using CreditService.Models;
using CreditService.Models.DTOs;
using CreditService.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Swashbuckle.AspNetCore.Annotations;

namespace CreditService.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class CreditController : ControllerBase
    {
        private readonly Services.CreditService _creditService;
        private readonly Services.IAuthorizationService _authz;

        public CreditController(Services.CreditService creditService, Services.IAuthorizationService authz)
        {
            _creditService = creditService;
            _authz = authz;
        }

        // Управление тарифами
        [HttpPost("tariffs")]
        [Authorize(Policy = "EmployeeOnly")]
        [SwaggerOperation(
            Summary = "Создание нового тарифа",
            Description = "Создает новый кредитный тариф"
        )]

        public async Task<ActionResult<CreditTariff>> CreateTariff([FromBody] CreateTariffDto dto)
        {
            try
            {
                var tariff = await _creditService.CreateTariffAsync(dto);
                return CreatedAtAction(nameof(GetTariff), new { id = tariff.Id }, tariff);
            }
            catch (Exception ex)
            {
                return BadRequest();
            }
        }

        [HttpGet("tariffs")]
        [SwaggerOperation(Summary = "Получение всех тарифов")]
        public async Task<ActionResult<IEnumerable<CreditTariff>>> GetAllTariffs([FromQuery] bool onlyActive = true)
        {
            var tariffs = await _creditService.GetAllTariffsAsync(onlyActive);
            return Ok(tariffs);
        }

        [HttpGet("tariffs/{id}")]
        [SwaggerOperation(Summary = "Получение тарифа по id")]
        public async Task<ActionResult<CreditTariff>> GetTariff(Guid id)
        {
            var tariff = await _creditService.GetTariffByIdAsync(id);
            if (tariff == null)
                return NotFound();

            return Ok(tariff);
        }

        [HttpDelete("tariffs/{id}")]
        [Authorize(Policy = "EmployeeOnly")]
        [SwaggerOperation(Summary = "Удаление тарифа")]
        public async Task<IActionResult> DeactivateTariff(Guid id)
        {
            var result = await _creditService.DeactivateTariffAsync(id);
            if (!result)
                return NotFound();

            return NoContent();
        }

        // Заявки на кредит
        [HttpPost("apply")]
        [SwaggerOperation(Summary = "Взятие кредита")]
        public async Task<ActionResult<Credit>> ApplyForCredit([FromBody] ApplyForCreditDto dto)
        {
            try
            {
                var userId = User.FindFirst("sub")?.Value;

                if (userId != dto.ClientId.ToString())
                    return Forbid();

                var credit = await _creditService.ApplyForCreditAsync(dto);
                return StatusCode(201, credit);
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest();
            }
            catch (Exception ex)
            {
                return StatusCode(500);
            }
        }

        // Информация о кредитах
        [HttpGet]
        [Authorize(Policy = "EmployeeOnly")]
        [SwaggerOperation(Summary = "Получение информации о всех кредитах")]
        public async Task<ActionResult<IEnumerable<Credit>>> GetAllCredits()
        {
            var credits = await _creditService.GetAllCreditsAsync();
            return Ok(credits);
        }

        [HttpGet("{id}")]
        [SwaggerOperation(Summary = "Получение информации о кредите по id")]
        public async Task<ActionResult<Credit>> GetCredit(Guid id)
        {
            var userId = User.FindFirst("sub")?.Value;

            if (!await _authz.CanViewCredit(userId, id))
                return Forbid();

            var credit = await _creditService.GetCreditByIdAsync(id);
            if (credit == null)
                return NotFound();

            return Ok(credit);
        }

        [HttpGet("client/{clientId}")]
        [SwaggerOperation(Summary = "Получение всех кредитов клиента")]
        public async Task<ActionResult<IEnumerable<Credit>>> GetClientCredits(Guid clientId)
        {
            var userId = User.FindFirst("sub")?.Value;

            if (!await _authz.CanViewClientCredits(userId, clientId))
                return Forbid();

            var credits = await _creditService.GetClientCreditsAsync(clientId);
            return Ok(credits);
        }

        // Платежи
        [HttpPost("payments")]
        [SwaggerOperation(Summary = "Внести платеж по кредиту")]
        public async Task<ActionResult<CreditPayment>> MakePayment([FromBody] MakePaymentDto dto)
        {
            try
            {
                var payment = await _creditService.MakePaymentAsync(dto);
                return Ok(payment);
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        [HttpGet("{creditId}/payments")]
        [SwaggerOperation(Summary = "Получение информации о всех платежах по кредиту")]
        public async Task<ActionResult<IEnumerable<CreditPayment>>> GetCreditPayments(Guid creditId)
        {
            var payments = await _creditService.GetCreditPaymentsAsync(creditId);
            return Ok(payments);
        }


        [HttpGet("{creditId}/payments/overdue")]
        [SwaggerOperation(Summary = "Получение просроченных платежей по кредиту")]
        public async Task<ActionResult<IEnumerable<CreditPaymentDto>>> GetOverduePayments(Guid creditId)
        {
            var payments = await _creditService.GetOverduePaymentsAsync(creditId);
            return Ok(payments);
        }


        [HttpGet("client/{clientId}/payments/overdue")]
        [SwaggerOperation(Summary = "Получение всех просроченных платежей клиента")]
        public async Task<ActionResult<IEnumerable<CreditPaymentDto>>> GetClientOverduePayments(Guid clientId)
        {
            var payments = await _creditService.GetClientOverduePaymentsAsync(clientId);
            return Ok(payments);
        }


        [HttpGet("client/{clientId}/rating")]
        [SwaggerOperation(Summary = "Получение кредитного рейтинга клиента")]
        public async Task<ActionResult<CreditRatingDto>> GetClientCreditRating(Guid clientId)
        {
            var rating = await _creditService.GetCreditRatingAsync(clientId);
            return Ok(rating);
        }



    }
}
