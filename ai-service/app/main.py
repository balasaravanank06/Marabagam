from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings
from app.api.health import router as health_router
from app.api.rag import router as rag_router
from app.api.movement import router as movement_router

app = FastAPI(
    title="Marabagam AI Service",
    description="AI Microservice for Tamil Digital Heritage Preservation & Cultural Guide RAG Pipeline",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health_router)
app.include_router(rag_router)
app.include_router(movement_router)

@app.get("/")
async def root():
    return {"message": "Marabagam AI Service Operational", "docs": "/docs"}
