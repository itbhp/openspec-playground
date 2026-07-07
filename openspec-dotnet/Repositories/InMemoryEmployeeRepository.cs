using EmployeeApi.Model;

namespace EmployeeApi.Repositories;

public class InMemoryEmployeeRepository : IEmployeeRepository
{
    private readonly Dictionary<int, Employee> _store = new();
    private int _sequence = 1;

    public Task<IReadOnlyList<Employee>> FindAllAsync()
        => Task.FromResult<IReadOnlyList<Employee>>(_store.Values.ToList());

    public Task<Employee?> FindByIdAsync(int id)
        => Task.FromResult(_store.TryGetValue(id, out var employee) ? employee : null);

    public Task<Employee> SaveAsync(Employee employee)
    {
        if (employee.Id is null)
        {
            employee.Id = _sequence++;
        }
        _store[employee.Id.Value] = employee;
        return Task.FromResult(employee);
    }

    public Task DeleteByIdAsync(int id)
    {
        _store.Remove(id);
        return Task.CompletedTask;
    }

    public Task<bool> ExistsByIdAsync(int id)
        => Task.FromResult(_store.ContainsKey(id));
}

