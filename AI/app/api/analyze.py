"""
/ai/analyze endpoint - Data analysis and insights
"""
from fastapi import APIRouter, HTTPException, Depends
from datetime import datetime
import logging
import re

from app.schemas import AnalyzeRequest, AnalysisResponse, Anomaly, Suggestion
from app.core.model_loader import get_model_loader
from app.core.prompt_engine import get_system_prompt, build_analyze_prompt
from app.core.security import require_internal_token
from app.core.permissions import permission_filter

logger = logging.getLogger(__name__)

router = APIRouter(dependencies=[Depends(require_internal_token)])


@router.post("/analyze", response_model=AnalysisResponse)
async def analyze(request: AnalyzeRequest):
    """
    Perform deep analysis on business data
    
    - **analysis_type**: sales, performance, fraud_detection, or trends
    - **context**: Data to analyze
    """
    try:
        logger.info(f"Analyze request ({request.analysis_type}) from user {request.user_id}")
        
        # Filter context
        filtered_context = permission_filter.filter_context(
            request.context,
            request.role.value,
            request.user_id
        )
        
        if not filtered_context:
            raise HTTPException(status_code=403, detail="Insufficient permissions or no data available")
        
        # Build prompts
        system_prompt = get_system_prompt(request.role.value)
        user_prompt = build_analyze_prompt(request.analysis_type, filtered_context)
        
        # Generate analysis
        model = get_model_loader()
        result = model.generate(system_prompt, user_prompt, temperature=0.5)  # Lower temp for analysis
        
        response_text = result["response"]
        
        # Parse response into structured format
        summary, key_findings = _parse_analysis(response_text)
        
        # Detect anomalies for fraud detection
        anomalies = []
        if request.analysis_type == "fraud_detection":
            anomalies = _detect_anomalies(filtered_context, response_text)
        
        # Extract recommendations
        recommendations = _extract_recommendations(response_text)
        
        # Calculate confidence
        confidence = 0.8 if len(filtered_context) >= 5 else 0.6
        
        logger.info(f"Analysis completed with {len(key_findings)} findings")
        
        return AnalysisResponse(
            analysis_type=request.analysis_type,
            summary=summary,
            key_findings=key_findings,
            anomalies=anomalies,
            recommendations=recommendations,
            confidence=round(confidence, 2),
            timestamp=datetime.utcnow().isoformat()
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in analyze endpoint: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


def _parse_analysis(response_text: str) -> tuple:
    """Parse AI response into summary and key findings"""
    lines = [line.strip() for line in response_text.split('\n') if line.strip()]
    
    # First paragraph as summary
    summary = lines[0] if lines else "Analysis completed"
    
    # Extract bullet points as key findings
    key_findings = []
    for line in lines[1:]:
        if line.startswith(('-', '•', '*')) or re.match(r'^\d+\.', line):
            clean_line = re.sub(r'^[-•*\d.]\s*', '', line)
            if clean_line:
                key_findings.append(clean_line)
    
    return summary, key_findings[:10]  # Limit to 10 findings


def _detect_anomalies(context: dict, response_text: str) -> list:
    """Detect anomalies from context and AI response"""
    anomalies = []
    
    # Look for keywords indicating anomalies
    anomaly_keywords = ['unusual', 'anomaly', 'suspicious', 'outlier', 'abnormal', 'irregular']
    
    lines = response_text.lower().split('\n')
    for line in lines:
        if any(keyword in line for keyword in anomaly_keywords):
            # Try to extract entity ID if mentioned
            entity_match = re.search(r'(business|booking|transaction)\s*[:#]?\s*([a-zA-Z0-9-]+)', line, re.IGNORECASE)
            
            entity_id = entity_match.group(2) if entity_match else "unknown"
            entity_type = entity_match.group(1).lower() if entity_match else "transaction"
            
            # Determine severity
            severity = "high" if any(word in line for word in ['critical', 'severe', 'high']) else "medium"
            
            anomalies.append(Anomaly(
                entity_id=entity_id,
                entity_type=entity_type,
                score=0.8 if severity == "high" else 0.6,
                reason=line[:200],
                severity=severity
            ))
    
    return anomalies[:5]  # Limit to top 5


def _extract_recommendations(response_text: str) -> list:
    """Extract recommendations from analysis"""
    recommendations = []
    
    lines = response_text.split('\n')
    in_recommendation_section = False
    
    for line in lines:
        line_lower = line.lower()
        
        # Detect recommendation sections
        if 'recommend' in line_lower or 'action' in line_lower or 'next step' in line_lower:
            in_recommendation_section = True
            continue
        
        if in_recommendation_section:
            clean_line = line.strip().lstrip('0123456789.-*• ')
            if len(clean_line) > 15:
                # Determine priority
                priority = "high" if any(word in clean_line.lower() for word in ['immediately', 'urgent', 'critical']) else "medium"
                
                recommendations.append(Suggestion(
                    title=clean_line[:60],
                    description=clean_line,
                    priority=priority
                ))
    
    return recommendations[:7]  # Limit to 7