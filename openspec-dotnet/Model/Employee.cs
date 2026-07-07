namespace EmployeeApi.Model;

// Plain POCO — no EF Core mapping/attributes yet, intentionally
public class Employee
{
    public int? Id { get; set; }
    public string? FirstName { get; set; }
    public string? LastName { get; set; }
    public string? Email { get; set; }
    public string? Department { get; set; }
}

