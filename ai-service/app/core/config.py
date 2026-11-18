from pydantic_settings import BaseSettings
from typing import List

class Settings(BaseSettings):
    AI_SERVICE_NAME: str = "marabagam-ai"
    PORT: int = 8000
    ENVIRONMENT: str = "development"
    CORS_ORIGINS: str = "http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173"

    # Database connection for pgvector
    DB_HOST: str = "postgres"
    DB_PORT: int = 5432
    DB_NAME: str = "marabagam_db"
    DB_USER: str = "marabagam_user"
    DB_PASSWORD: str = "marabagam_dev_password"

    # AI & Embedding Model Configuration
    OPENAI_API_KEY: str = ""
    LLM_API_KEY: str = ""
    EMBEDDING_API_KEY: str = ""
    AI_MODEL: str = "gpt-4o-mini"
    EMBEDDING_MODEL: str = "text-embedding-3-small"
    EMBEDDING_DIMENSION: int = 1536
    RAG_SIMILARITY_THRESHOLD: float = 0.70
    RAG_TOP_K: int = 4

    @property
    def api_key(self) -> str:
        return self.OPENAI_API_KEY or self.LLM_API_KEY

    @property
    def cors_origins_list(self) -> List[str]:
        return [origin.strip() for origin in self.CORS_ORIGINS.split(",") if origin.strip()]

    @property
    def database_url(self) -> str:
        return f"postgresql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()

