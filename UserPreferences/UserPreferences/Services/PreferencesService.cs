using Microsoft.EntityFrameworkCore;
using UserPreferences.Data;
using UserPreferences.Models;
using UserPreferences.Models.DTOs;

namespace UserPreferencesService.Services
{

    public class PreferencesService
    {
        private readonly ApplicationDbContext _context;
        private readonly ILogger<PreferencesService> _logger;

        public PreferencesService(ApplicationDbContext context, ILogger<PreferencesService> logger)
        {
            _context = context;
            _logger = logger;
        }

        public async Task<UserPreferencesDto> GetPreferencesAsync(Guid userId, AppType appType)
        {
            var preferences = await _context.UserPreferences
                .Include(p => p.HiddenAccounts)
                .FirstOrDefaultAsync(p => p.UserId == userId && p.AppType == appType);

            if (preferences == null)
            {
                preferences = new UserPreference
                {
                    UserId = userId,
                    AppType = appType,
                    Theme = Theme.Light,
                    UpdatedAt = DateTime.UtcNow
                };

                _context.UserPreferences.Add(preferences);
                await _context.SaveChangesAsync();
            }

            return MapToDto(preferences);
        }

        public async Task<UserPreferencesDto> UpdateThemeAsync(Guid userId, AppType appType, Theme theme)
        {
            var preferences = await GetOrCreatePreferences(userId, appType);

            preferences.Theme = theme;
            preferences.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            return MapToDto(preferences);
        }

        public async Task<UserPreferencesDto> UpdateHiddenAccountsAsync(Guid userId, AppType appType, List<Guid> hiddenAccountIds)
        {
            var preferences = await GetOrCreatePreferences(userId, appType);

            var existingHidden = _context.HiddenAccounts
                .Where(h => h.UserPreferenceId == preferences.Id);
            _context.HiddenAccounts.RemoveRange(existingHidden);

            foreach (var accountId in hiddenAccountIds)
            {
                _context.HiddenAccounts.Add(new HiddenAccount
                {
                    UserPreferenceId = preferences.Id,
                    AccountId = accountId,
                    HiddenAt = DateTime.UtcNow
                });
            }

            preferences.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            preferences = await _context.UserPreferences
                .Include(p => p.HiddenAccounts)
                .FirstAsync(p => p.Id == preferences.Id);

            return MapToDto(preferences);
        }


        public async Task<UserPreferencesDto> GetPreferencesByDeviceAsync(Guid userId, AppType appType)
        {
            return await GetPreferencesAsync(userId, appType);
        }

        private async Task<UserPreference> GetOrCreatePreferences(Guid userId, AppType appType)
        {
            var preferences = await _context.UserPreferences
                .Include(p => p.HiddenAccounts)
                .FirstOrDefaultAsync(p => p.UserId == userId && p.AppType == appType);

            if (preferences == null)
            {
                preferences = new UserPreference
                {
                    UserId = userId,
                    AppType = appType,
                    Theme = Theme.Light,
                    UpdatedAt = DateTime.UtcNow
                };

                _context.UserPreferences.Add(preferences);
                await _context.SaveChangesAsync();
            }

            return preferences;
        }

        private UserPreferencesDto MapToDto(UserPreference preference)
        {
            return new UserPreferencesDto
            {
                UserId = preference.UserId,
                AppType = preference.AppType,
                Theme = preference.Theme,
                HiddenAccountIds = preference.HiddenAccounts.Select(h => h.AccountId).ToList()
            };
        }
    }
}