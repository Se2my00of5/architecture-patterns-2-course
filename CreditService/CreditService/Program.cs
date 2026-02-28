using CreditService.Data;
using CreditService.Services;
using Microsoft.EntityFrameworkCore;
using System.Net;
using System.Text.Json.Serialization;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddDbContext<CreditDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));



//builder.WebHost.ConfigureKestrel(options =>
//{
//    options.Listen(IPAddress.Loopback, 5005); // http://localhost:5005
//});


//builder.Services.AddSwaggerGen(c =>
//{
//    c.SwaggerDoc("v1", new OpenApiInfo
//    {
//        Version = "v1", 
//        Title = "Credit Service API"
//    });
//});



builder.Services.AddHttpClient("CoreService", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["Services:Core"] ?? "http://localhost:1111");
});
builder.Services.AddHttpClient("UserService", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["Services:Users"] ?? "http://localhost:1115");
});


builder.Services.AddScoped<CreditService.Services.CreditService>();


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
