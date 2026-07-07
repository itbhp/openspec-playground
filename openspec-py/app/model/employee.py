from dataclasses import dataclass
from typing import Optional


# Plain domain model — no SQLAlchemy mapping yet, intentionally
@dataclass
class Employee:
    id: Optional[int] = None
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    email: Optional[str] = None
    department: Optional[str] = None

