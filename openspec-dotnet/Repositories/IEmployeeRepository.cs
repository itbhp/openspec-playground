using EmployeeApi.Model;

namespace EmployeeApi.Repositories;

public interface IEmployeeRepository
{
    Task<IReadOnlyList<Employee>> FindAllAsync();
    Task<Employee?> FindByIdAsync(int id);
    Task<Employee> SaveAsync(Employee employee);
    Task DeleteByIdAsync(int id);
    Task<bool> ExistsByIdAsync(int id);
}

