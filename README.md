# Developer Setup (Local Environment)

This repo contains multiple services:

- **Gateway** (`backend/gateway`) — Spring Boot (Gradle), port **8080**, context path **`/api`**
- **User Service** (`backend/userservice`) — Spring Boot (Maven), port **8082**
- **Chat Service** (`backend/chat-service`) — Spring Boot (Gradle), port **8085**
- **Analytics Service** (`backend/analytics-service`) — Spring Boot (Gradle), port **8086**
- **AI Service** (`AI/`) — FastAPI (Python 3.11), port **8000**
- **Dependencies**: PostgreSQL (**5432**) and Redis (**6379**)

The services communicate with each other on localhost using these default ports.

## Prerequisites

Install the following on your machine:

- **Git**
- **Java 17**
- **Gradle wrapper included** (no global Gradle required)
- **Maven wrapper included** (no global Maven required for `backend/userservice`)
- **Python 3.11** (for `AI/`)
- **Docker Desktop** (recommended) or native installs for Postgres + Redis

## Clone

```bash
git clone <your-repo-url>
cd eduka
````

## Start dependencies (Postgres + Redis)

### Option A (Docker, recommended)

```bash
docker run --name eduka-postgres -d \
  -p 5432:5432 \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=trigga \
  -e POSTGRES_DB=inzozi_space \
  postgres:16

docker run --name eduka-redis -d -p 6379:6379 redis:7
```

If containers already exist:

```bash
docker start eduka-postgres eduka-redis
```

### Option B: Native installs

* Create a Postgres database named **`inzozi_space`**
* Ensure user **`postgres`** exists with password `trigga`
* Ensure Redis is reachable at **`localhost:6379`**

## Environment variables

### Shared (recommended in your shell)

```bash
export INTERNAL_SERVICE_TOKEN=dev-internal-token
export DB_PASSWORD=trigga
export CHAT_WS_SECRET=dev-chat-ws-secret
```

### AI service (`AI/.env`)

```bash
cd AI
cp .env.example .env
```

Edit `AI/.env` and set at least:

* `OPENAI_API_KEY`

## Run services (local)

Run each service in its own terminal tab.

### 1) User Service (8082)

```bash
cd backend/userservice
./mvnw spring-boot:run
```

Config: `backend/userservice/src/main/resources/application.properties`.

### 2) Chat Service (8085)

```bash
cd backend/chat-service
./gradlew bootRun
```

Uses Postgres at `jdbc:postgresql://localhost:5432/inzozi_space`.

### 3) Analytics Service (8086)

```bash
cd backend/analytics-service
./gradlew bootRun
```

Calls AI service at `http://localhost:8000`.

### 4) AI Service (8000)

Python:

```bash
cd AI
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Or Docker:

```bash
cd AI
docker build -t inzozi-ai-service .
docker run --rm -p 8000:8000 --env-file .env inzozi-ai-service
```

### 5) Gateway (8080, context `/api`)

```bash
cd backend/gateway
./gradlew bootRun
```

Gateway expects:

* User service at `http://localhost:8082`
* Chat service at `http://localhost:8085`
* Analytics service at `http://localhost:8086`
* AI service at `http://localhost:8000`
* Redis at `localhost:6379`

## Quick port map

* **Postgres**: `localhost:5432` (db: `inzozi_space`, user: `postgres`)
* **Redis**: `localhost:6379`
* **User Service**: `http://localhost:8082`
* **Gateway**: `http://localhost:8080/api`
* **Chat Service**: `http://localhost:8085`
* **Analytics Service**: `http://localhost:8086`
* **AI Service**: `http://localhost:8000` (`GET /health` for health check)

## Common issues

* **Postgres auth fails**: ensure Postgres user/password match (`postgres` / `trigga`)
* **Redis connection errors**: ensure Redis is running on `localhost:6379`
* **AI calls failing**: ensure `AI/.env` has valid `OPENAI_API_KEY` and AI service is running
* **AI CORS env typo**: fix `CORS_ALLOWED_ORIGINS` in `AI/.env` if needed
* **CORS**: Gateway allowed origins in `backend/gateway/src/main/resources/application.yml`

```
```
