<?php

declare(strict_types=1);

namespace App\Controller;

use App\Model\Employee;
use App\Service\EmployeeService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/employees')]
class EmployeeController extends AbstractController
{
    public function __construct(
        private readonly EmployeeService $service,
    ) {
    }

    #[Route('', methods: ['GET'])]
    public function list(): JsonResponse
    {
        return $this->json($this->service->findAll());
    }

    #[Route('', methods: ['POST'])]
    public function create(Request $request): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $employee = $this->mapToEmployee($data);

        $created = $this->service->create($employee);

        return $this->json($created, Response::HTTP_CREATED);
    }

    #[Route('/{id}', methods: ['GET'])]
    public function get(int $id): JsonResponse
    {
        $employee = $this->service->findById($id);

        if ($employee === null) {
            throw new NotFoundHttpException();
        }

        return $this->json($employee);
    }

    #[Route('/{id}', methods: ['PUT'])]
    public function update(int $id, Request $request): JsonResponse
    {
        if (!$this->service->exists($id)) {
            throw new NotFoundHttpException();
        }

        $data = json_decode($request->getContent(), true);
        $employee = $this->mapToEmployee($data);

        $updated = $this->service->update($id, $employee);

        return $this->json($updated);
    }

    #[Route('/{id}', methods: ['DELETE'])]
    public function delete(int $id): Response
    {
        if (!$this->service->exists($id)) {
            throw new NotFoundHttpException();
        }

        $this->service->delete($id);

        return new Response(null, Response::HTTP_NO_CONTENT);
    }

    /**
     * @param array<string, mixed>|null $data
     */
    private function mapToEmployee(?array $data): Employee
    {
        $employee = new Employee();
        $employee->setFirstName($data['firstName'] ?? null);
        $employee->setLastName($data['lastName'] ?? null);
        $employee->setEmail($data['email'] ?? null);
        $employee->setDepartment($data['department'] ?? null);

        return $employee;
    }
}

