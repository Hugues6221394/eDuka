# Inzozi Space AI Intelligence Service

Enterprise-grade AI service for the Inzozi Space digital marketplace ecosystem.

## Overview

This service provides intelligent business analysis, strategic advice, and customer support using advanced language models and RAG (Retrieval-Augmented Generation).

## Architecture

- **Framework**: FastAPI (Python 3.11)
- **LLM**: OpenAI GPT-4 (configurable)
- **Vector Store**: ChromaDB
- **Embeddings**: sentence-transformers

## Key Features

✅ **Permission-Based Context Filtering** - Data access controlled by user roles  
✅ **No Direct Database Access** - Receives sanitized context from backend services  
✅ **RAG Support** - Vector store for policy documents and historical data  
✅ **Role-Specific Prompts** - Tailored system prompts for vendors, admins, and clients  
✅ **Anomaly Detection** - Identifies suspicious patterns in business data  
✅ **Structured Responses** - JSON responses with confidence scores and suggestions  

## API Endpoints

### POST /ai/ask
Natural language Q&A based on provided context.

**Request:**
```json
{
  "user_id": "uuid",
  "role": "vendor",
  "question": "Why did my sales drop last month?",
  "context": {
    "business_id": "uuid",
    "current_month_sales": 1500000,
    "last_month_sales": 2500000
  }
}
```

**Response:**
```json
{
  "response": "Your sales dropped 40% compared to last month...",
  "confidence": 0.85,
  "suggestions": [...],
  "data_quality": "sufficient",
  "timestamp": "2024-01-11T18:30:00Z"
}
```

### POST /ai/analyze
Deep analysis of business data.

**Analysis types:** `sales`, `performance`, `fraud_detection`, `trends`

### POST /ai/advise
Strategic business advice.

**Advice types:** `pricing`, `inventory`, `marketing`, `growth`

## Setup

### 1. Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Required variables:
- `OPENAI_API_KEY` - Your OpenAI API key
- `OPENAI_MODEL` - Model to use (default: gpt-4-turbo-preview)

### 2. Install Dependencies

```bash
pip install -r requirements.txt
```

### 3. Run Locally

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. Docker Build

```bash
docker build -t inzozi-ai-service .
docker run -p 8000:8000 --env-file .env inzozi-ai-service
```

## Security

🔒 **No Direct Database Access** - AI never queries production DB  
🔒 **Permission Filtering** - Context filtered by role before processing  
🔒 **Response Sanitization** - Removes sensitive data from AI responses  
🔒 **PII Protection** - Competitor data anonymized for vendors  

## Integration with Backend Services

Backend services (Analytics, Business Verification, etc.) should:

1. Fetch data based on user permissions
2. Sanitize data (remove PII if needed)
3. Structure context as JSON
4. Call AI service endpoint
5. Log AI response for audit

**Example from Analytics Service:**

```java
// In AnalyticsService.java
public AiInsightResponse getBusinessInsights(String userId, String role) {
    // 1. Fetch user's business data
    BusinessMetrics metrics = fetchMetrics(userId);
    
    // 2. Structure context
    Map<String, Object> context = Map.of(
        "business_id", userId,
        "monthly_sales", metrics.getMonthlySales(),
        "category", metrics.getCategory()
    );
    
    // 3. Call AI service
    AiAnalysisRequest request = new AiAnalysisRequest(
        userId, role, "performance", context
    );
    
    return aiServiceClient.analyze(request);
}
```

## Monitoring

- Health endpoint: `GET /health`
- Logs: JSON format with request/response tracking
- Metrics: Token usage, confidence scores, error rates

## Future Enhancements

- [ ] Fine-tuned model for Rwandan market
- [ ] Multi-language support (Kinyarwanda, French)
- [ ] Advanced RAG with business documents
- [ ] Real-time trend detection
- [ ] A/B testing framework for prompts

