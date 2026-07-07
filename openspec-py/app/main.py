from fastapi import FastAPI

from app.controller.employee_controller import router as employee_router

app = FastAPI(title="Employee API")
app.include_router(employee_router)

