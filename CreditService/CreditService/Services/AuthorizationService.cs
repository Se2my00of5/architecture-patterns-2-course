using CreditService.Data;
using System.Net.Http.Headers;

namespace CreditService.Services
{
    public interface IAuthorizationService
    {
        Task<bool> CanViewCredit(string? userId, Guid creditId);
        Task<bool> CanViewClientCredits(string? userId, Guid clientId);
    }

    public class AuthorizationService : IAuthorizationService
    {
        private readonly TokenService _tokenService;
        private readonly HttpClient _httpClient;
        private readonly CreditDbContext _context;

        public AuthorizationService(TokenService tokenService, HttpClient httpClient, CreditDbContext context)
        {
            _tokenService = tokenService;
            _httpClient = httpClient;
            _context = context;
        }

        public async Task<bool> CanViewCredit(string? userId, Guid creditId)
        {
            if (string.IsNullOrEmpty(userId)) return false;

            var credit = await _context.Credits.FindAsync(creditId);
            if (credit == null) return false;

            // Владелец кредита
            if (credit.ClientId.ToString() == userId) return true;

            // Проверяем, сотрудник ли пользователь
            return await IsEmployee(userId);
        }

        public async Task<bool> CanViewClientCredits(string? userId, Guid clientId)
        {
            if (string.IsNullOrEmpty(userId)) return false;

            // Сам клиент
            if (userId == clientId.ToString()) return true;

            // Сотрудник
            return await IsEmployee(userId);
        }

        private async Task<bool> IsEmployee(string userId)
        {
            var token = await _tokenService.GetServiceTokenAsync();

            var request = new HttpRequestMessage(HttpMethod.Get,
                $"http://user-service-backend:1115/api/users/{userId}");
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);

            var response = await _httpClient.SendAsync(request);
            if (!response.IsSuccessStatusCode) return false;

            var user = await response.Content.ReadFromJsonAsync<UserResponse>();
            return user?.roles?.Contains("EMPLOYEE") == true;
        }

        public class UserResponse
        {
            public Guid id { get; set; }
            public string login { get; set; } = "";
            public string fullName { get; set; } = "";
            public List<string> roles { get; set; } = new();
            public bool isBlocked { get; set; }
        }
    }
}
