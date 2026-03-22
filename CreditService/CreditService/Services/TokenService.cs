using CreditService.Models;
using Microsoft.AspNetCore.DataProtection;
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

    //public async Task<string> GetServiceTokenAsync()
    //{
    //    if (_cachedToken != null && DateTime.UtcNow < _expiry)
    //        return _cachedToken;

    //    var response = await _httpClient.PostAsync(
    //        $"{_config["USER_SERVICE_URL"]}/oauth2/token",
    //        new FormUrlEncodedContent(new Dictionary<string, string>
    //        {
    //            ["grant_type"] = "client_credentials",
    //            ["client_id"] = "service-client",
    //            ["client_secret"] = "service-client-secret-1233321",
    //            ["scope"] = "api"
    //        }));

    //    var json = await response.Content.ReadAsStringAsync();
    //    var token = JsonSerializer.Deserialize<TokenResponse>(json);

    //    _cachedToken = token!.AccessToken;
    //    _expiry = DateTime.UtcNow.AddSeconds(token.ExpiresIn - 60);
    //    return _cachedToken;
    //}
    public async Task<string> GetServiceTokenAsync()
    {
        if (_cachedToken != null && DateTime.UtcNow < _expiry)
            return _cachedToken;

        var response = await _httpClient.PostAsync(
            $"{_config["USER_SERVICE_URL"]}/oauth2/token",
            new FormUrlEncodedContent(new Dictionary<string, string>
            {
                ["grant_type"] = "client_credentials",
                ["client_id"] = "service-client",
                ["client_secret"] = "service-client-secret-1233321",  
                ["scope"] = "api"
            }));

        var json = await response.Content.ReadAsStringAsync();

        Console.WriteLine($"Token response status: {response.StatusCode}");
        Console.WriteLine($"Token response body: {json}");

        var token = JsonSerializer.Deserialize<TokenResponse>(json);

        if (token == null || string.IsNullOrEmpty(token.AccessToken))
        {
            Console.WriteLine($" Failed to deserialize token. JSON: {json}");
            throw new Exception("Failed to get access token");
        }

        Console.WriteLine($" Token obtained: {token.AccessToken.Substring(0, Math.Min(20, token.AccessToken.Length))}...");

        _cachedToken = token.AccessToken;
        _expiry = DateTime.UtcNow.AddSeconds(token.ExpiresIn - 60);
        return _cachedToken;
    }

}