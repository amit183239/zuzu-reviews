# ZUZU Review Service (Spring Boot, Java 8)

A microservice that ingests hotel reviews from AWS S3 (JSON Lines), validates/transforms, and stores into PostgreSQL.
Includes scheduler, REST trigger, idempotent file processing, Docker images, and docker-compose for a full local run.

## Features
- Java 8 / Spring Boot 2.7.x
- S3 listing + download (AWS SDK v2)
- JSONL streaming parse (Jackson)
- Validation (required fields)
- Idempotency:
  - `processed_files` table (S3 key + etag)
  - Unique `(provider_id, external_review_id)` for reviews
- Schema via Flyway (providers, hotels, reviews, review_aspects, processed_files)
- Scheduler (`cron`) + REST endpoint to trigger
- Concurrency for file processing
- Logging + basic error handling
- Dockerfile and docker-compose

## Quick Start (Docker Compose)

### Prereqs
- Docker + Docker Compose
- AWS credentials with `s3:ListBucket` and `s3:GetObject` on your bucket

### 1) Edit config
Update `src/main/resources/application.yml`:
```yaml
app:
  ingestion:
    s3-bucket: YOUR_BUCKET_NAME
    s3-prefix: reviews-daily/
aws:
  region: ap-southeast-2
```
Export credentials (or use `~/.aws/credentials`):
```bash
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
export AWS_REGION=ap-southeast-1
```

### 2) Build & Run
```bash

# Build docker image
docker build -t zuzu/review-service:0.1.0 .

# Start DB + app
docker compose up
```

The app will run Flyway migrations, then start the scheduler.
Manual trigger:
```bash
curl -X POST http://localhost:8080/api/ingest/run
```

### 3) Stop
```bash
docker compose down -v
```


## Configuration
- `app.ingestion.poll-cron` controls schedule (default: every 15 minutes).
- `app.ingestion.max-concurrent-files` controls parallel S3 files processed.

## Testing
```bash
docker compose -f docker-compose.test.yml up --abort-on-container-exit --exit-code-from maven-tests
```


## Endpoints
- `POST /api/ingest/run` — process all new files
- `POST /api/ingest/file?key=...` — process a single S3 key
- `GET /api/reviews/hotel/{hotel_id}` - api to fetch hotel reviews

## Design Decisions
- **Idempotency**: `processed_files` per S3 key/etag and unique constraint on provider+external_review_id.
- **Extensibility**: Separate `review_aspects` table for arbitrary aspect ratings; raw JSON stored for forward compatibility.
- **Validation**: Minimal required fields; malformed lines logged and skipped.
- **Concurrency**: Fixed thread pool sized via config for predictable resource usage.
- **Resilience**: Errors in one file do not stop others; per-row errors logged.
- **Support for multiple third-party providers**: The system can handle multiple providers like agoda, booking etc

## Folder Structure
```
zuzu-review-service/
  src/main/java/com/zuzu/reviews/...
  src/main/resources/db/migration/V1__init.sql
  Dockerfile
  docker-compose.yml
```

## Architecture Diagram
```mermaid
flowchart TD
  subgraph Clients
    U1["Mobile App"]
    U2["Web App"]
    Ops["Ops Scripts"]
  end

  U1 -->|HTTPS/JSON| GW["API Gateway / Ingress"]
  U2 -->|HTTPS/JSON| GW
  Ops -->|Trigger| GW

  GW --> RS["Review Service (Spring Boot)"]

  subgraph Review Service
    direction TB
    CTRL["Controller Layer<br/>(REST)"] --> SVC["Service Layer"]
    SVC --> JPA["JPA / Hibernate"]
    SVC --> Cache[("Optional Redis Cache")]
  end

  JPA --> DB[("RDBMS<br/>PostgreSQL / MySQL")]
  class DB db

  %% Ingestion side
  S3[("AWS S3 Bucket<br/>reviews-daily/")] --> ING["Ingestion Worker<br/>@Scheduled cron<br/>*/30 * * * * *"]
  ING --> PARSE["Parser<br/>(JSON Lines)"] --> JPA

  %% Infra & Ops
  subgraph Infra & Ops
    MON["Metrics / Logging<br/>(Logback, Actuator)"]
    MIG["DB Migrations<br/>(Flyway)"]
    TC["Testcontainers<br/>(Postgres)"]
    K8s["Kubernetes<br/>(HPA, PDB, Autoscaling)"]
  end

  RS --- MON
  MIG --> DB
  TC -. CI tests .-> RS
  RS --- K8s

  classDef db fill:#eef,stroke:#557;
  classDef comp fill:#fefefe,stroke:#333,stroke-width:1px;
  class RS,CTRL,SVC,JPA,ING,PARSE,Cache comp;
```

## Sequence Diagram
```mermaid
sequenceDiagram
autonumber
participant C as "Client"
participant G as "API Gateway / Ingress"
participant R as "Review Service"
participant S as "Service Layer"
participant J as "JPA / Hibernate"
participant D as "RDBMS"


Note over C,G: Request path for GET /reviews/{id}
C->>G: GET /reviews/42
G->>R: Forward request (auth, rate limit)
R->>S: Map to service method


opt Redis cache enabled
S->>S: Check cache(key=review:42)
alt Cache hit
S-->>R: Review DTO (from cache)
else Cache miss
S->>J: findById(42)
J->>D: SELECT * FROM reviews WHERE id=42
D-->>J: Row
J-->>S: Entity
S->>S: Put into cache (TTL)
S-->>R: DTO/Response
end
end


R-->>G: 200 OK (JSON)
G-->>C: 200 OK (JSON)


%% Error branch (e.g., not found)
alt Review not found
S-->>R: throws NotFound
R-->>G: 404 Not Found (problem+json)
G-->>C: 404 Not Found
end
```

## Notes
- This project uses AWS default credential provider chain; when running in Docker, pass env vars via compose.

