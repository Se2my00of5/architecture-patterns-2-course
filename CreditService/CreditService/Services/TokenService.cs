using CreditService.Models;
using System.Text.Json;

public class TokenService
{
    private readonly HttpClient _httpClient;
    private readonly IConfiguration _config;
    private string? _cachedToken;
    private DateTime _expiry = DateTime.MinValue;

    public TokenService(HttpClient httpClient, IConfiguration config)
    {
        _httpClient = httpClient;
        _config = config;
    }

    public async Task<string> GetServiceTokenAsync()
    {
        if (_cachedToken != null && DateTime.UtcNow < _expiry)
            return _cachedToken;

        var response = await _httpClient.PostAsync(
            $"{_config["USER_SERVICE_URL"]}/oauth2/token",
            new FormUrlEncodedContent(new Dictionary<string, string>
            {
                ["grant_type"] = "client_credentials",
                ["client_id"] = _config["SERVICE_CLIENT_ID"]!,
                ["client_secret"] = _config["SERVICE_CLIENT_SECRET"]!,
                ["scope"] = "api"
            }));

        var json = await response.Content.ReadAsStringAsync();
        var token = JsonSerializer.Deserialize<TokenResponse>(json);

        _cachedToken = token!.AccessToken;
        _expiry = DateTime.UtcNow.AddSeconds(token.ExpiresIn - 60);
        return _cachedToken;
    }

}