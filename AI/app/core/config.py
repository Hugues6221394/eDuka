from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # Environment
    environment: str = "development"
    
    # OpenAI
    openai_api_key: str
    openai_model: str = "gpt-4-turbo-preview"
    internal_service_token: str = ""

    cors_allowed_origins: str = "http://localhost:8082"
    allowed_hosts: str = "localhost,127.0.0.1,0.0.0.0"
    
    # Vector Store
    vector_store_type: str = "chromadb"
    vector_store_path: str = "./data/vector_store"
    
    # Embedding
    embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2"
    
    # Server
    host: str = "0.0.0.0"
    port: int = 8000
    
    # Logging
    log_level: str = "INFO"
    
    # Database (read-only)
    database_url: str = ""
    db_read_only: bool = True
    
    # Rate Limiting
    max_requests_per_minute: int = 60
    max_context_length: int = 4000
    
    # Response Settings
    max_response_tokens: int = 1000
    temperature: float = 0.7

    class Config:
        env_file = ".env"
        case_sensitive = False


@lru_cache()
def get_settings() -> Settings:
    return Settings()
