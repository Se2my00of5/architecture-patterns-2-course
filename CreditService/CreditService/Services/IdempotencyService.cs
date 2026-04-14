using CreditService.Data;
using CreditService.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Memory;

    using CreditService.Data;
    using CreditService.Models;
    using global::CreditService.Models.DTOs;
    using Microsoft.AspNetCore.Mvc;
    using Microsoft.EntityFrameworkCore;
    using Microsoft.Extensions.Caching.Memory;
    using System.Text.Json;

    namespace CreditService.Services
    {

        public class IdempotencyService
        {
            private readonly CreditDbContext _context;
            private readonly IMemoryCache? _cache;
            private readonly ILogger<IdempotencyService> _logger;
            private readonly TimeSpan _ttl = TimeSpan.FromHours(24);

            public IdempotencyService(
                CreditDbContext context,
                IMemoryCache? cache = null,
                ILogger<IdempotencyService>? logger = null)
            {
                _context = context;
                _cache = cache;
                _logger = logger;
            }

            public async Task<IdempotentResult?> GetExistingResultAsync(string key, string path)
            {
                var cacheKey = $"idemp:{path}:{key}";

                if (_cache.TryGetValue(cacheKey, out IdempotentResult? cached))
                    return cached;

                var record = await _context.IdempotentRequests
                    .FirstOrDefaultAsync(r => r.Key == key && r.Path == path && r.ExpiresAt > DateTime.UtcNow);

                if (record == null)
                    return null;

                var result = new IdempotentResult
                {
                    StatusCode = record.StatusCode,
                    ResponseBody = record.ResponseBody
                };

                _cache.Set(cacheKey, result, TimeSpan.FromMinutes(5));

                return result;
            }

            public async Task SaveResultAsync(string key, string path, IActionResult result, int statusCode)
            {
                try
                {
                    var responseBody = await SerializeResultAsync(result);

                    var record = new IdempotentRequest
                    {
                        Id = Guid.NewGuid(),
                        Key = key,
                        Path = path,
                        StatusCode = statusCode,
                        ResponseBody = responseBody,
                        CreatedAt = DateTime.UtcNow,
                        ExpiresAt = DateTime.UtcNow.Add(_ttl)
                    };

                    _context.IdempotentRequests.Add(record);
                    await _context.SaveChangesAsync();

                    var cacheKey = $"idemp:{path}:{key}";
                    var cachedResult = new IdempotentResult
                    {
                        StatusCode = statusCode,
                        ResponseBody = responseBody
                    };
                    _cache.Set(cacheKey, cachedResult, TimeSpan.FromMinutes(5));

                    _logger.LogDebug("Saved idempotent result for key: {Key}, path: {Path}", key, path);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Failed to save idempotent result for key: {Key}", key);
                }
            }

            private async Task<string> SerializeResultAsync(IActionResult result)
            {
                var responseObject = new
                {
                    StatusCode = GetStatusCode(result),
                    Data = ExtractValue(result),
                    Timestamp = DateTime.UtcNow
                };

                return JsonSerializer.Serialize(responseObject, new JsonSerializerOptions
                {
                    PropertyNamingPolicy = JsonNamingPolicy.CamelCase
                });
            }

            private int GetStatusCode(IActionResult result)
            {
                return result switch
                {
                    OkResult => 200,
                    OkObjectResult ok => ok.StatusCode ?? 200,
                    CreatedResult created => created.StatusCode ?? 201,
                    CreatedAtActionResult created => created.StatusCode ?? 201,
                    BadRequestResult => 400,
                    BadRequestObjectResult bad => bad.StatusCode ?? 400,
                    NotFoundResult => 404,
                    NotFoundObjectResult notFound => notFound.StatusCode ?? 404,
                    StatusCodeResult status => status.StatusCode,
                    ObjectResult obj => obj.StatusCode ?? 200,
                    _ => 200
                };
            }

            private object? ExtractValue(IActionResult result)
            {
                return result switch
                {
                    OkObjectResult ok => ok.Value,
                    CreatedResult created => created.Value,
                    CreatedAtActionResult created => created.Value,
                    ObjectResult obj => obj.Value,
                    _ => null
                };
            }
        }
    }
