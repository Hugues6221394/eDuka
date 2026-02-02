"""
Model Loader - Initialize and manage LLM
"""
from openai import OpenAI
from app.core.config import get_settings
import logging

logger = logging.getLogger(__name__)


class ModelLoader:
    """Manages LLM initialization and inference"""
    
    def __init__(self):
        self.settings = get_settings()
        self.client = None
        self._initialize()
    
    def _initialize(self):
        """Initialize OpenAI client"""
        try:
            self.client = OpenAI(api_key=self.settings.openai_api_key)
            logger.info(f"OpenAI client initialized with model: {self.settings.openai_model}")
        except Exception as e:
            logger.error(f"Failed to initialize OpenAI client: {e}")
            raise
    
    def generate(
        self,
        system_prompt: str,
        user_prompt: str,
        temperature: float = None,
        max_tokens: int = None
    ) -> dict:
        """
        Generate response from LLM
        
        Returns:
            dict with 'response' and 'metadata'
        """
        if not self.client:
            raise RuntimeError("OpenAI client not initialized")
        
        temperature = temperature or self.settings.temperature
        max_tokens = max_tokens or self.settings.max_response_tokens
        
        try:
            response = self.client.chat.completions.create(
                model=self.settings.openai_model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                temperature=temperature,
                max_tokens=max_tokens,
                top_p=0.9
            )
            
            result = {
                "response": response.choices[0].message.content,
                "metadata": {
                    "model": response.model,
                    "tokens_used": response.usage.total_tokens,
                    "finish_reason": response.choices[0].finish_reason
                }
            }
            
            logger.debug(f"Generated response. Tokens used: {response.usage.total_tokens}")
            return result
            
        except Exception as e:
            logger.error(f"LLM generation failed: {e}")
            raise


# Global instance
_model_loader = None


def get_model_loader() -> ModelLoader:
    """Get or create ModelLoader singleton"""
    global _model_loader
    if _model_loader is None:
        _model_loader = ModelLoader()
    return _model_loader
