using EmployeeApi.Model;
using EmployeeApi.Services;
using Microsoft.AspNetCore.Mvc;

namespace EmployeeApi.Controllers;

public record EmployeeDto(
    string? FirstName,
    string? LastName,
    string? Email,
    string? Department);

[ApiController]
[Route("employees")]
public class EmployeesController : ControllerBase
{
    private readonly EmployeeService _service;

    public EmployeesController(EmployeeService service)
    {
        _service = service;
    }

    [HttpGet]
    public async Task<IReadOnlyList<Employee>> List() => await _service.FindAllAsync();

    [HttpPost]
    public async Task<ActionResult<Employee>> Create([FromBody] EmployeeDto dto)
    {
        var created = await _service.CreateAsync(ToEmployee(dto));
        return StatusCode(StatusCodes.Status201Created, created);
    }

    [HttpGet("{id:int}")]
    public async Task<ActionResult<Employee>> Get(int id)
    {
        var employee = await _service.FindByIdAsync(id);
        if (employee is null)
        {
            return NotFound();
        }
        return employee;
    }

    [HttpPut("{id:int}")]
    public async Task<ActionResult<Employee>> Update(int id, [FromBody] EmployeeDto dto)
    {
        if (!await _service.ExistsAsync(id))
        {
            return NotFound();
        }
        return await _service.UpdateAsync(id, ToEmployee(dto));
    }

    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete(int id)
    {
        if (!await _service.ExistsAsync(id))
        {
            return NotFound();
        }
        await _service.DeleteAsync(id);
        return NoContent();
    }

    private static Employee ToEmployee(EmployeeDto dto) => new()
    {
        FirstName = dto.FirstName,
        LastName = dto.LastName,
        Email = dto.Email,
        Department = dto.Department,
    };
}

