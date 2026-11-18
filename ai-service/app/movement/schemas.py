from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class LandmarkInput(BaseModel):
    x: float = Field(..., ge=0.0, le=1.0)
    y: float = Field(..., ge=0.0, le=1.0)
    z: float = 0.0
    visibility: float = Field(1.0, ge=0.0, le=1.0)
    presence: float = Field(1.0, ge=0.0, le=1.0)

class FrameLandmarksInput(BaseModel):
    frameIndex: Optional[int] = 0
    timestampMs: Optional[int] = 0
    landmarks: List[LandmarkInput]

class AnalysisRequest(BaseModel):
    movementCode: str
    jointRulesJson: Optional[str] = "{}"
    referenceTemplateJson: Optional[str] = "{}"
    frames: List[FrameLandmarksInput]

class FeedbackItem(BaseModel):
    bodyPart: str
    metricName: str
    measuredValue: float
    targetRangeMin: float
    targetRangeMax: float
    score: float
    feedbackTextEn: str
    feedbackTextTa: str
    severity: str = "INFO"

class AnalysisResponse(BaseModel):
    overallScore: float
    metricsSummaryJson: str
    feedbacks: List[FeedbackItem]
