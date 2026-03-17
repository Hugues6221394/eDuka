"""
/ai/advise endpoint - Business advice and recommendations
"""
from fastapi import APIRouter, HTTPException, Depends
from datetime import datetime
import logging

from app.schemas import AdviseRequest, AIResponse, Suggestion
from app.core.model_loader import get_model_loader
from app.core.prompt_engine import get_system_prompt, build_advise_prompt
from app.core.security import require_internal_token
from app.core.permissions import permission_filter

logger = logging.getLogger(__name__)

router = APIRouter(dependencies=[Depends(require_internal_token)])


@router.post("/advise", response_model=AIResponse)
async def advise(request: AdviseRequest):
    """
    Provide strategic business advice
    
    - **advice_type**: pricing, inventory, marketing, or growth
    - **context**: Business context for advice
    """
    try:
        logger.info(f"Advise request ({request.advice_type}) from user {request.user_id}")
        
        # Only vendors and admins can request advice
        if request.role.value not in ["vendor", "admin"]:
            raise HTTPException(status_code=403, detail="Only vendors and admins can request business advice")
        
        # Filter context
        filtered_context = permission_filter.filter_context(
            request.context,
            request.role.value,
            request.user_id
        )
        
        if not filtered_context:
            return AIResponse(
                response="Insufficient data to provide advice. Please ensure you have some business history and metrics.",
                confidence=0.0,
                suggestions=[],
                data_quality="insufficient",
                timestamp=datetime.utcnow().isoformat()
            )
        
        # Build prompts
        system_prompt = get_system_prompt(request.role.value)
        user_prompt = build_advise_prompt(request.advice_type, filtered_context)
        
        # Generate advice
        model = get_model_loader()
        result = model.generate(system_prompt, user_prompt, temperature=0.7)
        
        response_text = result["response"]
        
        # Sanitize
        sanitized_response = permission_filter.sanitize_response(response_text, request.role.value)
        
        # Extract actionable suggestions
        suggestions = _extract_actionable_advice(sanitized_response, request.advice_type)
        
        # Data quality assessment
        data_quality = "sufficient" if len(filtered_context) >= 3 else "limited"
        confidence = 0.85 if data_quality == "sufficient" else 0.65
        
        logger.info(f"Generated {request.advice_type} advice with {len(suggestions)} suggestions")
        
        return AIResponse(
            response=sanitized_response,
            confidence=round(confidence, 2),
            suggestions=suggestions,
            data_quality=data_quality,
            timestamp=datetime.utcnow().isoformat()
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in advise endpoint: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


def _extract_actionable_advice(response_text: str, advice_type: str) -> list:
    """Extract actionable suggestions from advice"""
    suggestions = []
    
    lines = response_text.split('\n')
    
    for line in lines:
        line = line.strip()
        
        # Look for action-oriented language
        if any(keyword in line.lower() for keyword in [
            'should', 'recommend', 'suggest', 'consider', 'try', 'implement',
            'increase', 'decrease', 'add', 'remove', 'optimize'
        ]):
            clean_line = line.lstrip('0123456789.-*• ')
            
            if len(clean_line) > 15:
                # Determine priority based on urgency keywords
                if any(urgent in clean_line.lower() for urgent in ['immediately', 'urgent', 'asap', 'critical']):
                    priority = "high"
                elif any(future in clean_line.lower() for future in ['consider', 'future', 'eventually', 'later']):
                    priority = "low"
                else:
                    priority = "medium"
                
                suggestions.append(Suggestion(
                    title=f"{advice_type.title()} Action: {clean_line[:50]}...",
                    description=clean_line,
                    priority=priority
                ))
    
    # Sort by priority
    priority_order = {"high": 0, "medium": 1, "low": 2}
    suggestions.sort(key=lambda x: priority_order[x.priority])
    
    return suggestions[:6]  # Top 6 suggestions