"""
FastAPI Main Application
"""
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging
from datetime import datetime

from app.core.config import get_settings
from app.api import ask, analyze, advise

# Logging setup
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize settings
settings = get_settings()

# Create FastAPI app
app = FastAPI(
    title="Inzozi Space AI Intelligence Service",
    description="Enterprise AI service for business intelligence and customer support",
    version="1.0.0",
    docs_url="/docs" if settings.environment == "development" else None,
    redoc_url="/redoc" if settings.environment == "development" else None
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, restrict this
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Exception handler
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unhandled exception: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={
            "error": "Internal server error",
            "detail": str(exc) if settings.environment == "development" else "An error occurred",
            "timestamp": datetime.utcnow().isoformat()
        }
    )


# Health check
@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "ai-intelligence",
        "timestamp": datetime.utcnow().isoformat()
    }


# Include routers
app.include_router(ask.router, prefix="/ai", tags=["Ask"])
app.include_router(analyze.router, prefix="/ai", tags=["Analyze"])
app.include_router(advise.router, prefix="/ai", tags=["Advise"])


@app.on_event("startup")
async def startup_event():
    logger.info("Starting Inzozi Space AI Intelligence Service")
    logger.info(f"Environment: {settings.environment}")
    logger.info(f"Model: {settings.openai_model}")


@app.on_event("shutdown")
async def shutdown_event():
    logger.info("Shutting down AI Intelligence Service")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.environment == "development"
    )
