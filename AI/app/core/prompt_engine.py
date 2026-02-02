"""
Prompt Engineering for Inzozi Space AI
"""
from typing import Dict, Any


# System prompts by role
SYSTEM_PROMPTS = {
    "vendor": """You are a senior business analyst for Inzozi Space, Rwanda's leading digital marketplace.

Your role when advising vendors:
- Analyze vendor business performance data
- Provide actionable business advice
- Identify growth opportunities and risks
- Speak in a professional, supportive, data-driven manner

CRITICAL RULES:
- Only use data provided in the context
- If data is insufficient, explicitly say "Insufficient data to analyze" and explain what's missing
- No hallucinations - be honest about limitations
- Provide specific, measurable recommendations when possible
- Consider Rwandan market context
- Be encouraging but realistic

Voice: Professional yet approachable, like a trusted business advisor.""",

    "admin": """You are a business intelligence analyst for Inzozi Space platform administrators.

Your role when advising admins:
- Analyze platform-wide metrics and patterns
- Identify anomalies and potential fraud
- Provide strategic insights on market trends
- Monitor commission structures and revenue
- Detect system health issues

CRITICAL RULES:
- Only use data provided in context
- Flag anomalies with specific evidence
- Quantify risks and opportunities
- Be direct and precise
- No speculation beyond dataVoice: Technical, data-focused, executive-level clarity.""",

    "client": """You are a helpful shopping assistant for Inzozi Space customers.

Your role when helping clients:
- Answer questions about products and services
- Provide recommendations based on preferences
- Explain booking processes
- Help navigate the platform

CRITICAL RULES:
- Only recommend products/services in the provided context
- Be honest if information is not available
- Prioritize user safety and satisfaction
- Be warm and helpful

Voice: Friendly, clear, customer-service oriented."""
}


def get_system_prompt(role: str) -> str:
    """Get system prompt for user role"""
    return SYSTEM_PROMPTS.get(role, SYSTEM_PROMPTS["client"])


def build_ask_prompt(question: str, context: Dict[str, Any], role: str) -> str:
    """Build prompt for /ai/ask endpoint"""
    context_str = format_context(context)
    
    return f"""User Context:
Role: {role}
Question: {question}

Available Data:
{context_str}

Instructions:
1. Answer the question using ONLY the data provided above
2. If data is insufficient, say so explicitly
3. Provide specific, actionable insights
4. Format response clearly with bullet points where appropriate
5. Consider Rwandan business context

Answer:"""


def build_analyze_prompt(analysis_type: str, context: Dict[str, Any]) -> str:
    """Build prompt for /ai/analyze endpoint"""
    context_str = format_context(context)
    
    prompts = {
        "sales": f"""Analyze the following sales data:

{context_str}

Provide:
1. Key trends and patterns
2. Performance highlights and concerns
3. Comparison to previous periods
4. Specific recommendations for improvement

Analysis:""",

        "performance": f"""Analyze business performance metrics:

{context_str}

Provide:
1. Overall performance assessment
2. Strengths and weaknesses
3. Areas for optimization
4. Growth opportunities

Analysis:""",

        "fraud_detection": f"""Review the following data for anomalies:

{context_str}

Identify:
1. Unusual patterns or outliers
2. Potential fraud signals
3. Risk level assessment
4. Recommended actions

Report:""",

        "trends": f"""Analyze market trends from this data:

{context_str}

Identify:
1. Emerging patterns
2. Popular categories/locations
3. Seasonal trends
4. Market opportunities

Insights:"""
    }
    
    return prompts.get(analysis_type, prompts["sales"])


def build_advise_prompt(advice_type: str, context: Dict[str, Any]) -> str:
    """Build prompt for /ai/advise endpoint"""
    context_str = format_context(context)
    
    prompts = {
        "pricing": f"""Business pricing data:

{context_str}

Provide pricing advice:
1. Market position analysis
2. Competitive assessment
3. Pricing optimization recommendations
4. Expected impact

Advice:""",

        "inventory": f"""Inventory and product data:

{context_str}

Provide inventory advice:
1. Stock level assessment
2. Product gaps and opportunities
3. Seasonal considerations
4. Recommended actions

Advice:""",

        "marketing": f"""Business marketing data:

{context_str}

Provide marketing advice:
1. Current reach and visibility
2. Target audience insights
3. Marketing channel recommendations
4. Campaign ideas

Advice:""",

        "growth": f"""Business growth data:

{context_str}

Provide growth strategy:
1. Current trajectory analysis
2. Growth blockers
3. Expansion opportunities
4. Action plan

Strategy:"""
    }
    
    return prompts.get(advice_type, prompts["pricing"])


def format_context(context: Dict[str, Any], max_length: int = 3000) -> str:
    """Format context dictionary into readable string"""
    lines = []
    for key, value in context.items():
        # Convert key from snake_case to title
        formatted_key = key.replace('_', ' ').title()
        
        if isinstance(value, (list, dict)):
            import json
            value_str = json.dumps(value, indent=2)
        else:
            value_str = str(value)
        
        lines.append(f"{formatted_key}: {value_str}")
    
    result = "\n".join(lines)
    
    # Truncate if too long
    if len(result) > max_length:
        result = result[:max_length] + "\n... (context truncated)"
    
    return result
