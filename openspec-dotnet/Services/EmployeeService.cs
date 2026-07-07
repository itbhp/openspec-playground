using EmployeeApi.Model;
using EmployeeApi.Repositories;

namespace EmployeeApi.Services;

public class EmployeeService
{
    private readonly IEmployeeRepository _repository;

    public EmployeeService(IEmployeeRepository repository)
    {
        _repository = repository;
    }

    public Task<IReadOnlyList<Employee>> FindAllAsync() => _repository.FindAllAsync();

    public Task<Employee?> FindByIdAsync(int id) => _repository.FindByIdAsync(id);

    public Task<Employee> CreateAsync(Employee employee) => _repository.SaveAsync(employee);

    public Task<Employee> UpdateAsync(int id, Employee employee)
    {
        employee.Id = id;
        return _repository.SaveAsync(employee);
    }

    public Task DeleteAsync(int id) => _repository.DeleteByIdAsync(id);

    public Task<bool> ExistsAsync(int id) => _repository.ExistsByIdAsync(id);
}

