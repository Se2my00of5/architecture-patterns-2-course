using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace CreditService
{
    using Microsoft.AspNetCore.Mvc;
    using Microsoft.AspNetCore.Mvc.Filters;
    using Microsoft.Extensions.DependencyInjection;
    using Microsoft.Extensions.Caching.Memory;
    using System.Text.Json;

    namespace CreditService
    {
        public class IdempotencyAttribute : Attribute, IAsyncActionFilter
        {
            private readonly IMemoryCache _cache;

            public IdempotencyAttribute()
            {
            }

            public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
            {
                var idempotencyKey = context.HttpContext.Request.Headers["Idempotency-Key"].FirstOrDefault();

                if (IsIdempotentMethod(context.HttpContext.Request.Method))
                {
                    if (string.IsNullOrEmpty(idempotencyKey))
                    {
                        context.Result = new BadRequestObjectResult(new
                        {
                            error = "Idempotency-Key header is required for this operation"
                        });
                        return;
                    }
                    var cache = context.HttpContext.RequestServices.GetRequiredService<IMemoryCache>();
                    var cacheKey = $"idempotent:{context.HttpContext.Request.Path}:{idempotencyKey}";

                    if (cache.TryGetValue(cacheKey, out IActionResult? cachedResult))
                    {
                        context.Result = cachedResult;
                        return;
                    }

                    var executedContext = await next();

                    if (IsSuccessStatusCode(executedContext.Result))
                    {
                        var clonedResult = CloneActionResult(executedContext.Result);
                        cache.Set(cacheKey, clonedResult, TimeSpan.FromHours(24));
                    }
                }
                else
                {
                    await next();
                }
            }

            private bool IsIdempotentMethod(string method) =>
                method == "POST" || method == "PUT" || method == "PATCH";

            private bool IsSuccessStatusCode(IActionResult result)
            {
                return result switch
                {
                    OkResult => true,
                    OkObjectResult ok => ok.StatusCode is null or 200,
                    CreatedResult created => created.StatusCode is 201,
                    CreatedAtActionResult created => created.StatusCode is 201,
                    StatusCodeResult status => status.StatusCode >= 200 && status.StatusCode < 300,
                    ObjectResult obj => obj.StatusCode >= 200 && obj.StatusCode < 300,
                    _ => false
                };
            }

            private IActionResult CloneActionResult(IActionResult original)
            {
                return original switch
                {
                    OkObjectResult ok => new OkObjectResult(ok.Value),
                    CreatedResult created => new CreatedResult(created.Location, created.Value),
                    CreatedAtActionResult created => new CreatedAtActionResult(
                        created.ActionName,
                        created.ControllerName,
                        created.RouteValues,
                        created.Value),
                    ObjectResult obj => new ObjectResult(obj.Value) { StatusCode = obj.StatusCode },
                    StatusCodeResult status => new StatusCodeResult(status.StatusCode),
                    _ => original
                };
            }
        }
    }
}
