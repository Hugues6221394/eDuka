"""
/ai/ask endpoint - Natural language Q&A
"""
from fastapi import APIRouter, HTTPException
from datetime import datetime
import logging

from app.schemas import AskRequest, AIResponse, Suggestion, ErrorResponse
from app.core.model_loader import get_model_loader
from app.core.prompt_engine import get_system_prompt, build_ask_prompt
from app.core.permissions import permission_filter

logger = logging.getLogger(__name__)

router = APIRouter()


@router.post("/ask", response_model=AIResponse)
async def ask(request: AskRequest):
    """
    Answer natural language questions based on provided context
    
    - **user_id**: User making the request
    - **role**: User role (client, vendor, admin)
    - **question**: Natural language question
    - **context**: Business data from backend service
    """
    try:
        logger.info(f"Ask request from user {request.user_id} (role: {request.role})")
        
        # Filter context based on permissions
        filtered_context = permission_filter.filter_context(
            request.context,
            request.role.value,
            request.user_id
        )
        
        # Check if we have sufficient data
        if not filtered_context:
            return AIResponse(
                response="I don't have sufficient data to answer that question. Please provide more context or check your permissions.",
                confidence=0.0,
                suggestions=[],
                data_quality="insufficient",
                timestamp=datetime.utcnow().isoformat()
            )
        
        # Build prompts
        system_prompt = get_system_prompt(request.role.value)
        user_prompt = build_ask_prompt(
            request.question,
            filtered_context,
            request.role.value
        )
        
        # Generate response
        model = get_model_loader()
        result = model.generate(system_prompt, user_prompt)
        
        # Parse response
        response_text = result["response"]
        
        # Sanitize response
        sanitized_response = permission_filter.sanitize_response(
            response_text,
            request.role.value
        )
        
        # Assess data quality
        data_quality = "sufficient"
        if len(filtered_context) < 2:
            data_quality = "limited"
        
        # Calculate confidence based on data quality and token usage
        tokens_used = result["metadata"]["tokens_used"]
        confidence = min(0.95, 0.6 + (len(filtered_context) * 0.05))
        if data_quality == "limited":
            confidence *= 0.7
        
        # Extract suggestions (simple heuristic for now)
        suggestions = _extract_suggestions(sanitized_response)
        
        logger.info(f"Generated response with {tokens_used} tokens, confidence: {confidence:.2f}")
        
        return AIResponse(
            response=sanitized_response,
            confidence=round(confidence, 2),
            suggestions=suggestions,
            data_quality=data_quality,
            timestamp=datetime.utcnow().isoformat()
        )
        
    except Exception as e:
        logger.error(f"Error in ask endpoint: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


def _extract_suggestions(response_text: str) -> list:
    """Extract actionable suggestions from response text"""
    suggestions = []
    
    # Simple heuristic: look for numbered lists or bullet points
    lines = response_text.split('\n')
    
    for line in lines:
        line = line.strip()
        
        # Check for recommendations or suggestions
        if any(keyword in line.lower() for keyword in ['recommend', 'suggest', 'should', 'consider']):
            # Remove numbering/bullets
            clean_line = line.lstrip('0123456789.-*• ')
            
            if len(clean_line) > 10:  # Meaningful suggestion
                suggestions.append(Suggestion(
                    title=clean_line[:50] + "..." if len(clean_line) > 50 else clean_line,
                    description=clean_line,
                    priority="medium"
                ))
    
    return suggestions[:5]  # Limit to top 5
