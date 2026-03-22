using Microsoft.AspNetCore.Mvc;
using System.Drawing;
using UserPreferences.Models;
using UserPreferences.Models.DTOs;
using UserPreferencesService.Services;


namespace UserPreferencesService.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class PreferencesController : ControllerBase
    {
        private readonly PreferencesService _preferencesService;

        public PreferencesController(PreferencesService preferencesService)
        {
            _preferencesService = preferencesService;
        }

        /// <summary>
        /// Получить настройки пользователя 
        /// </summary>
        [HttpGet("{userId}/{appType}")]
        public async Task<ActionResult<UserPreferencesDto>> GetPreferences(
            Guid userId,
            string appType)
        {
            try
            {
                var preferences = await _preferencesService.GetPreferencesAsync(userId, (AppType)Enum.Parse(typeof(AppType), appType));
                return Ok(preferences);
            }
            catch (Exception ex)
            {
                return StatusCode(500, "Internal server error");
            }
        }

        /// <summary>
        /// Обновить тему
        /// </summary>
        [HttpPatch("{userId}/{appType}/theme")]
        public async Task<ActionResult<UserPreferencesDto>> UpdateTheme(
            Guid userId,
            string appType,
            [FromBody] UpdateThemeRequest request)
        {
            try
            {
                var preferences = await _preferencesService.UpdateThemeAsync(userId, (AppType)Enum.Parse(typeof(AppType), appType), request.Theme);
                return Ok(preferences);
            }
            catch (Exception ex)
            {
                return StatusCode(500, "Internal server error");
            }
        }

        /// <summary>
        /// Обновить список скрытых счетов
        /// </summary>
        [HttpPut("{userId}/{appType}/hidden-accounts")]
        public async Task<ActionResult<UserPreferencesDto>> UpdateHiddenAccounts(
            Guid userId,
            string appType,
            [FromBody] UpdateHiddenAccountsRequest request)
        {
            try
            {
                var preferences = await _preferencesService.UpdateHiddenAccountsAsync(
                    userId, (AppType)Enum.Parse(typeof(AppType), appType), request.HiddenAccountIds);
                return Ok(preferences);
            }
            catch (Exception ex)
            {
                return StatusCode(500, "Internal server error");
            }
        }

        
    }
}