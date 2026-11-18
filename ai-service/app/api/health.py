from fastapi import APIRouter
from app.schemas.health import HealthResponse
from app.core.config import settings

router = APIRouter()

@router.get("/health", response_model=HealthResponse)
async def get_health():
    return HealthResponse(
        status="UP",
        service=settings.AI_SERVICE_NAME
    )
