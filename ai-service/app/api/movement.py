from fastapi import APIRouter, HTTPException, status
from app.movement.schemas import AnalysisRequest, AnalysisResponse
from app.movement.service import analyze_movement

router = APIRouter(prefix="/internal/v1/movement", tags=["movement"])

@router.post("/analyze", response_model=AnalysisResponse)
async def evaluate_movement(request: AnalysisRequest):
    if not request.frames:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Frame keypoints array cannot be empty"
        )
    if len(request.frames) > 300:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Frame count exceeds maximum limit of 300 frames"
        )

    try:
        response = analyze_movement(request)
        return response
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Movement analysis error: {str(e)}"
        )
