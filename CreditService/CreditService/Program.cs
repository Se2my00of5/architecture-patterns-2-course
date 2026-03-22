using CreditService.Data;
using CreditService.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Net;
using System.Reflection;
using System.Security.Cryptography;
using System.Text.Json.Serialization;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        var publicKey = builder.Configuration["JWT_PUBLIC_KEY"]
            ?? throw new InvalidOperationException("JWT_PUBLIC_KEY missing");

        var rsa = RSA.Create();
        rsa.ImportFromPem(publicKey);

        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = builder.Configuration["JWT_ISSUER"] ?? "user-service",
            ValidateAudience = true,
            ValidAudience = builder.Configuration["JWT_AUDIENCE"] ?? "credit-service",
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new RsaSecurityKey(rsa)
        };
    });

builder.Services.AddAuthorization();
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddDbContext<CreditDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));



//builder.WebHost.ConfigureKestrel(options =>
//{
//    options.Listen(IPAddress.Loopback, 5005); // http://localhost:5005
//});


builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "Credit Service API",
        Version = "v1",
        Description = "API для управления кредитными тарифами"
    });

    c.EnableAnnotations();

});



builder.Services.AddHttpClient("CoreService", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["Services:Core"] ?? "http://localhost:1111");
});
builder.Services.AddHttpClient("UserService", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["Services:Users"] ?? "http://localhost:1115");
});


builder.Services.AddScoped<CreditService.Services.CreditService>();
builder.Services.AddHostedService<PaymentProcessingService>();

builder.Services.AddHttpClient();
builder.Services.AddScoped<TokenService, TokenService>();
builder.Services.AddScoped<IAuthorizationService, AuthorizationService>();

// Добавить политики
builder.Services.AddAuthorization(options =>
{
    options.AddPolicy("EmployeeOnly", policy =>
        policy.RequireClaim("role", "EMPLOYEE"));
});


// Настройка CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAllOrigins", policy =>  // Название политики должно совпадать
    {
        policy.AllowAnyOrigin()    // Разрешаем запросы с любых источников
              .AllowAnyHeader()    // Разрешаем любые заголовки
              .AllowAnyMethod();   // Разрешаем любые методы
    });
});


//- enumConverter
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
    });
//-



var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    var dbContext = scope.ServiceProvider.GetRequiredService<CreditDbContext>();

    // Создаст БД и таблицы, если их нет
    dbContext.Database.EnsureCreated();
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Включение CORS
app.UseCors("AllowAllOrigins");

//---

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();
