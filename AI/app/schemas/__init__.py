"""
AI Service Schemas for Request/Response
"""
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any, Literal
from enum import Enum


class UserRole(str, Enum):
    CLIENT = "client"
    VENDOR = "vendor"
    ADMIN = "admin"


class AskRequest(BaseModel):
    """Request for /ai/ask endpoint"""
    user_id: str = Field(..., description="ID of the user making the request")
    role: UserRole = Field(..., description="Role of the user")
    question: str = Field(..., description="Natural language question", max_length=500)
    context: Dict[str, Any] = Field(..., description="Business context provided by backend service")


class AnalyzeRequest(BaseModel):
    """Request for /ai/analyze endpoint"""
    user_id: str
    role: UserRole
    analysis_type: Literal["sales", "performance", "fraud_detection", "trends"] = Field(..., description="Type of analysis")
    context: Dict[str, Any] = Field(..., description="Data to analyze")


class AdviseRequest(BaseModel):
    """Request for /ai/advise endpoint"""
    user_id: str
    role: UserRole
    advice_type: Literal["pricing", "inventory", "marketing", "growth"] = Field(..., description="Type of advice needed")
    context: Dict[str, Any] = Field(..., description="Business context for advice")


class Suggestion(BaseModel):
    """Individual suggestion"""
    title: str
    description: str
    priority: Literal["high", "medium", "low"]


class AIResponse(BaseModel):
    """Standard AI response"""
    response: str = Field(..., description="AI-generated response")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Confidence score 0-1")
    suggestions: List[Suggestion] = Field(default_factory=list, description="Actionable suggestions")
    data_quality: Literal["sufficient", "limited", "insufficient"] = Field(..., description="Quality of input data")
    timestamp: str = Field(..., description="Response timestamp")


class Anomaly(BaseModel):
    """Detected anomaly"""
    entity_id: str
    entity_type: str  # "business", "booking", "transaction"
    score: float = Field(..., ge=0.0, le=1.0, description="Anomaly score")
    reason: str
    severity: Literal["high", "medium", "low"]


class AnalysisResponse(BaseModel):
    """Response for analysis requests"""
    analysis_type: str
    summary: str
    key_findings: List[str]
    anomalies: List[Anomaly] = Field(default_factory=list)
    recommendations: List[Suggestion] = Field(default_factory=list)
    confidence: float
    timestamp: str


class ErrorResponse(BaseModel):
    """Error response"""
    error: str
    detail: Optional[str] = None
    timestamp: str
