using EmployeeApi.Repositories;
using EmployeeApi.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Active persistence: in-memory (singleton so state persists between requests).
// Swap this binding in Act 2 (MySQL/EF Core) or Act 3 (DynamoDB).
// Controller and service never change.
builder.Services.AddSingleton<IEmployeeRepository, InMemoryEmployeeRepository>();
builder.Services.AddScoped<EmployeeService>();

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI();
app.MapControllers();

app.Run();

