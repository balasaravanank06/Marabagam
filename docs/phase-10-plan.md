PHASE 10 — PRODUCTION READINESS & DEPLOYMENT
STATUS: REVISED PROPOSED PLAN — PENDING FINAL APPROVAL

## 1. CURRENT PRODUCTION-READINESS ASSESSMENT

An exhaustive inspection of the Marabagam platform baseline (Phases 1 through 9) confirms a fully functional, tested, feature-complete platform encompassing the React frontend, Spring Boot API gateway, FastAPI AI microservice, and PostgreSQL vector storage. To transition from development to a secure, resilient production system, the following operational and configuration gaps must be addressed:

| Subsystem | Current Development State | Production Gap / Risk | Production-Readiness Solution |
| :--- | :--- | :--- | :--- |
| **Frontend** | React 18 + Vite SPA, served via Nginx in Docker | Unrestricted CORS, exposed DevTools source maps, default caching | Environment-driven API URLs, asset caching headers (immutable hashed JS/CSS, no-cache HTML), CSP strictly matched to actual resources |
| **Backend** | Spring Boot 3.3, JPA/Hibernate, Flyway V1–V9 | Development fallback secret in `application.yml`, verbose dev logging | Mandatory environment secret validation (`MARABAGAM_CERTIFICATE_SECRET`, `JWT_SECRET`, `POSTGRES_PASSWORD`), `application-prod.yml`, HikariCP pool tuning |
| **AI Service** | FastAPI + Uvicorn, Pytest 100% pass | Direct port exposure (8000), unconfigurable worker count, missing secret separation | Gunicorn process manager with configurable Uvicorn ASGI workers (`AI_WORKERS`), container network isolation, `OPENAI_API_KEY` scoped exclusively to AI service |
| **Database** | PostgreSQL 16 + `pgvector` | Exposed port 5432, default dev passwords, lack of automated backup scripts | Container network isolation, vault/env secret injection, automated logical `pg_dump` backups with GPG encryption, isolated restore verification |
| **CI/CD** | Basic GitHub Actions workflow (`ci.yml`) | Basic build/test check, missing container scanning, secret scanning, SBOM, and staging workflow | Multi-stage pipeline with secret scanning (Gitleaks), dependency scanning (`npm`/`pip` audit), Trivy container scan, SBOM (Syft), staging deploy, and manual approval gate |

---

## 2. REQUIRED PRODUCTION ARCHITECTURE

```
                                [ PUBLIC INTERNET ]
                                         │
                                    (HTTPS :443)
                                         │
                        ┌────────────────┴────────────────┐
                        │       NGINX REVERSE PROXY       │
                        │       (EDGE GATEWAY & TLS)      │
                        │  (SSL Termination, HSTS, WAF)   │
                        └────────────────┬────────────────┘
                                         │
                  ┌──────────────────────┼──────────────────────┐
                  │ (Internal HTTP)      │ (Internal HTTP)      │
                  ▼                      ▼                      ▼
        ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
        │  React SPA       │   │  Spring Boot     │   │  Object Storage  │
        │  Static Container│   │  API Backend     │   │  (MinIO / S3)    │
        └──────────────────┘   └─────────┬────────┘   └──────────────────┘
                                         │ (Internal HTTP)
                                         ▼
                               ┌──────────────────┐
                               │  FastAPI AI      │
                               │  Microservice    │
                               └─────────┬────────┘
                                         │
                  ┌──────────────────────┴──────────────────────┐
                  │ Internal DB Wire (Encrypted / PrivNet)      │
                  ▼                                             ▼
        ┌──────────────────┐                          ┌──────────────────┐
        │  PostgreSQL 16   │                          │  Redis Cache &   │
        │  + pgvector      │                          │  Rate Limiter    │
        └──────────────────┘                          └──────────────────┘
```

### Network Topology & Component Boundaries:
- **Nginx Reverse Proxy (Edge Gateway)**: The single public-facing ingress point bound to ports 443 (HTTPS) and 80 (HTTP 301 redirect). Manages TLS termination, rate-limiting, and static asset proxying. (Note: Container terminology is Nginx Reverse Proxy / Edge Gateway; Kubernetes "Ingress" terminology is NOT used as deployment uses Docker Compose).
- **React Frontend Container**: Serves static HTML/JS/CSS internally to Nginx over port 80 (not exposed to external internet).
- **Spring Boot API Backend (`backend`)**: Operates on internal port 8080. Accessible ONLY via Nginx Reverse Proxy proxying `/api/v1/*`.
- **FastAPI AI Microservice (`ai-service`)**: Operates on internal port 8000. Accessible ONLY by the Spring Boot backend service. Port 8000 is NOT mapped to the host network.
- **PostgreSQL Database (`postgres`)**: Operates on internal port 5432. Accessible ONLY by `backend` and `ai-service`. Port 5432 is NOT mapped to the host network.
- **Redis Cache & Rate Limiter (`redis`)**: Operates on internal port 6379 for session state and rate limiting. Accessible ONLY by `backend`.

---

## 3. STRICT SECRET SEPARATION & PRODUCTION VALIDATION

### A. Clear Separation of Service Secrets
Spring Boot and FastAPI secrets are strictly compartmentalized:

1. **Spring Boot Backend Secrets**:
   - `POSTGRES_PASSWORD`: Database connection credential.
   - `JWT_SECRET`: HS512 JWT signing key (min 64 chars / 512-bit).
   - `MARABAGAM_CERTIFICATE_SECRET`: Certificate verification HMAC signing key (preserving exact Phase 8 contract).
   - `STORAGE_S3_ACCESS_KEY` / `STORAGE_S3_SECRET_KEY`: Object storage credentials for course media and thumbnails.
   - **Important**: Spring Boot does NOT consume or require `OPENAI_API_KEY`. Spring Boot startup will NOT fail if `OPENAI_API_KEY` is absent or if `ai-service` is offline.

2. **FastAPI AI Microservice Secrets**:
   - `OPENAI_API_KEY`: OpenAI API key for RAG vector embeddings (`text-embedding-3-small`) and LLM query generation (`gpt-4o-mini`). Scoped **exclusively** to `ai-service`.
   - `DB_PASSWORD`: Read/write database password for `pgvector` index access.

### B. Fail-Fast Startup Validation
In `application-prod.yml`, Spring Boot validates on startup that `POSTGRES_PASSWORD`, `JWT_SECRET`, and `MARABAGAM_CERTIFICATE_SECRET` are non-empty and NOT set to development default values (`marabagam_dev_password`, `404E63526655...`, `marabagam_development_certificate_secret_key_2026`). If dev defaults are detected, Spring Boot aborts initialization immediately.

In `ai-service/app/core/config.py`, FastAPI validates on startup that `OPENAI_API_KEY` starts with valid key prefix `sk-` and is non-empty.

### C. Secret Exposure & Leak Controls
- Secrets are NEVER committed to version control (`.gitignore` enforces `.env` exclusion).
- Secrets are NEVER compiled into client-side Vite bundles (no `VITE_` prefix for server secrets).
- Secrets are NEVER printed in Logback / Python logging outputs or exception tracebacks.
- Secrets are NEVER returned in HTTP error responses or Spring Actuator endpoints (`/actuator/env`, `/actuator/configprops` are strictly disabled).

---

## 4. DATABASE PRODUCTION READINESS (LOGICAL BACKUP & SAFE RESTORE)

### A. Selected Strategy: PostgreSQL Logical Backup (`pg_dump`)
For the Marabagam production deployment, **PostgreSQL Logical Backup using `pg_dump`** is explicitly selected as the primary backup mechanism. Full Point-in-Time Recovery (PITR) via continuous WAL archiving is documented as a future enterprise option and is NOT claimed as a Phase 10 baseline deliverable.

### B. Backup Specifications (`deployment/scripts/backup.sh`):
- **Frequency**: Daily automated execution via Cron at 02:00 UTC.
- **Format & Compression**: Custom archive format (`pg_dump -Fc -Z 9`), providing high compression and selective table restoration.
- **Encryption**: GPG symmetric AES-256 encryption (`gpg --symmetric --cipher-algo AES256`) applied immediately after dump creation before transmission.
- **Offsite Storage**: Uploaded via AWS CLI / S3 API to an encrypted S3 bucket (`s3://marabagam-backups/db/`) with Object Lock enabled.
- **Retention Schedule**: 30 daily backups, 12 monthly backups, 1 yearly backup.
- **Integrity Verification**: `sha256sum` checksum computed before upload and verified against S3 metadata after transfer; `pg_restore --list` sanity check executed on generated archive.
- **Backup Failure Alerting**: Automated Webhook / Email notification dispatched immediately if `pg_dump`, GPG encryption, checksum validation, or S3 transfer returns a non-zero exit code.

### C. Safe Database Restore Procedure (`deployment/scripts/restore.sh`):
> [!CAUTION]
> **PRODUCTION OVERWRITE PROTECTION**: Restore testing MUST NEVER run against the live production database (`marabagam_db`).

Restore testing procedure enforces strict safeguards:
1. **Isolated Target Environment**: Restoration target (`RESTORE_TARGET`) defaults to a temporary isolated PostgreSQL container or database (`marabagam_restore_test`).
2. **Explicit Production Protection Safeguards**:
   - `restore.sh` inspects `$RESTORE_TARGET` and aborts immediately if target name equals `marabagam_db` or if `$ENVIRONMENT == "production"` unless an explicit `--force-production-disaster-recovery` override flag is provided with interactive confirmation.
3. **Weekly Automated Restore Testing Schedule**: Executed weekly in an isolated CI/test container.
4. **Verification Protocol**:
   - **Schema Verification**: Verifies table counts match expected baseline (38 JPA tables).
   - **Flyway History Verification**: Confirms `flyway_schema_history` table contains successful records for `V1` through `V9`.
   - **Data Sanity Check**: Verifies non-zero row counts in `users`, `art_forms`, `courses`, `cultural_events`, `quiz_questions`, `certificates`.
   - **Application Connectivity Test**: Executes Spring Boot test container query against the restored instance.
5. **Environment Cleanup**: Automatically drops and destroys the temporary `marabagam_restore_test` instance upon test completion.

---

## 5. MEDIA & OBJECT STORAGE DISASTER RECOVERY

### A. Media Inventory & Access Control Classification

| Media Asset Type | Storage Location | Access Control | CDN / Caching Policy |
| :--- | :--- | :--- | :--- |
| **User Avatars** | `s3://marabagam-media-public/avatars/` | Public Read | Cached via CDN (1 day) |
| **Course Thumbnails** | `s3://marabagam-media-public/courses/` | Public Read | Cached via CDN (30 days) |
| **Art Directory Assets** | `s3://marabagam-media-public/art-forms/` | Public Read | Cached via CDN (30 days) |
| **Lesson Video / Assets** | `s3://marabagam-media-private/lessons/` | Protected (Signed URLs) | Time-limited signed URL (1 hour) |
| **Reference Movement Media**| `s3://marabagam-media-private/movements/`| Protected (Signed URLs) | Time-limited signed URL (1 hour) |

### B. Object Storage Recovery & Protection Strategy:
1. **S3 Bucket Versioning**: Enabled on all media buckets to protect against accidental deletion or corrupt overwrites.
2. **Deletion Protection**: S3 Object Lock enabled for reference movement media; MFA delete required for bucket bucket deletion.
3. **Daily Backup & Replication**: Incremental `aws s3 sync` executed daily from primary media buckets to a secondary backup storage bucket in a separate geographic region.
4. **Recovery Protocol**: In the event of primary object storage degradation, Nginx proxy endpoint routes requests to the secondary backup bucket while primary restoration is executed via `aws s3 sync`.

---

## 6. TLS CERTIFICATE LIFECYCLE MANAGEMENT

### A. Certificate Strategy: Let's Encrypt + ACME Container
Production TLS termination is managed at the **Nginx Reverse Proxy** using Let's Encrypt certificates issued via an automated Certbot container (`certbot/certbot`).

### B. Complete Lifecycle Protocol:
1. **Issuance**: Obtained via HTTP-01 challenge (`certbot certonly --webroot -w /var/www/certbot -d marabagam.org -d www.marabagam.org`).
2. **Storage**: Certificate files (`fullchain.pem`, `privkey.pem`) stored on persistent Docker volume (`letsencrypt_certs`) mounted read-only to `nginx-proxy`.
3. **Automated Renewal**: Certbot container executes `certbot renew --quiet` twice daily via automated cron schedule.
4. **Zero-Downtime Reload**: Upon successful renewal, Certbot triggers a post-renewal hook issuing a `SIGHUP` signal to `nginx-proxy` container (`docker exec nginx-proxy nginx -s reload`), reloading SSL certificates without dropping active connections.
5. **Expiration Monitoring**: Prometheus exporter monitors certificate expiry days remaining. Alert triggered if expiry drops below **14 days**.
6. **Failure Handling**: If renewal fails within **30 days** of expiration, an automated failure alert (Slack/Email Webhook) is triggered daily. Certbot retries every 12 hours, leaving a 30-day buffer to resolve DNS/network issues before certificate expiration.
7. **TLS Configuration**: TLS 1.2 & TLS 1.3 only; strong ECDHE ciphers; HTTP (port 80) $\rightarrow$ HTTPS (port 443) 301 redirect; HSTS header enabled.

---

## 7. HTTP SECURITY HEADERS & RESOURCE-MATCHED CSP

### A. Code-Inspected Content Security Policy (CSP)
Inspection of the actual frontend repository (`frontend/package.json`, `index.html`, `index.css`) confirms that the client consumes `@mediapipe/pose` WASM binaries from jsDelivr CDN, local bundle assets, inline SVG icons, and backend API endpoints. No external Google Fonts or Leaflet CDN stylesheets are imported.

The CSP is configured strictly to match actual application resources:

```nginx
add_header Content-Security-Policy "default-src 'self'; script-src 'self' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline'; font-src 'self'; img-src 'self' data: blob: https://*.tile.openstreetmap.org; connect-src 'self' https://api.marabagam.org; media-src 'self' blob:; frame-ancestors 'none';" always;
```

### B. Security Header Suite
- `Strict-Transport-Security: max-age=31536000; includeSubDomains; preload`
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Permissions-Policy: camera=(self), microphone=(), geolocation=()`

---

## 8. FRONTEND CACHING STRATEGY

Granular Nginx caching rules balance optimal browser performance with instant deployment updates:

| Resource Path / Pattern | Cache-Control Header | Rationale |
| :--- | :--- | :--- |
| **`index.html`** | `no-cache, no-store, must-revalidate` | Prevents browser from caching entry point; ensures clients instantly load updated bundle hashes after deployment |
| **Hashed Assets (`/assets/*.js`, `*.css`)** | `public, max-age=31536000, immutable` | Vite outputs unique content-hashed filenames (`index-B3x9kL.js`); safe for 1-year immutable cache |
| **Favicon & Manifest (`/favicon.svg`)** | `public, max-age=86400, must-revalidate` | 24-hour cache with mandatory revalidation |
| **Public Media (`/media/*`)** | `public, max-age=604800, must-revalidate` | 7-day cache for course thumbnails and art form images |

---

## 9. FASTAPI ASGI ARCHITECTURE & GRACEFUL DEGRADATION

### A. ASGI Runner & Configurable Worker Count
The AI service is deployed using a **Gunicorn process manager with Uvicorn ASGI workers**:
```bash
gunicorn -w ${AI_WORKERS:-4} -k uvicorn.workers.UvicornWorker app.main:app --bind 0.0.0.0:8000 --timeout 30 --graceful-timeout 10
```
- **Configurable Worker Count**: `AI_WORKERS` defaults to 4, but is fully configurable via environment variable to adapt to target host hardware CPU cores (e.g. `AI_WORKERS=2` for small VPS, `AI_WORKERS=8` for dedicated multi-core servers).
- **Python Runtime**: Python 3.12 slim container base.

### B. AI Graceful Degradation Protocol
If OpenAI API, FastAPI, or `pgvector` retrieval is unavailable or timing out:
1. **Timeout Budget**: Spring Boot HTTP client enforces a strict **10-second timeout** on RAG vector queries.
2. **Controlled Retry Policy**: Maximum **1 retry** with exponential backoff (500 ms) for transient network glitches. Aggressive retry loops are strictly prohibited to prevent API cost spikes and thread pool exhaustion.
3. **User-Facing Fallback Payload**: If `ai-service` is unreachable, Spring Boot catches `RestClientException` / `SocketTimeoutException` and returns a graceful fallback payload:
   ```json
   {
     "success": true,
     "data": {
       "answer": "The AI Cultural Guide is currently operating in offline mode. Please consult the Art Directory and Course Catalog for verified heritage documentation.",
       "sources": [],
       "isFallback": true
     }
   }
   ```
4. **Core Platform Availability**: Primary platform operations (Art Directory, Courses, Quizzes, Certificates, Events, Community) remain 100% operational even during complete AI microservice outages.

---

## 10. CATEGORIZED CONTAINER HARDENING CONTROLS

Production containers enforce defense-in-depth security:

| Hardening Control | Classification | Implementation Details |
| :--- | :--- | :--- |
| **Non-Root User Execution** | **Required** | All containers execute as unprivileged users (`user: 1000:1000` / `appuser` / `nginx`). Root execution prohibited. |
| **Minimal Base Images** | **Required** | `eclipse-temurin:21-jre-alpine`, `python:3.12-slim`, `nginx:alpine`. Unnecessary build tools removed. |
| **No-New-Privileges** | **Required** | `security_opt: ["no-new-privileges:true"]` set on all container services. |
| **Dropped Linux Capabilities** | **Required** | `cap_drop: ["ALL"]` on backend, ai-service, postgres. Nginx retains only `cap_add: ["NET_BIND_SERVICE"]`. |
| **Resource Quotas & Limits** | **Required** | CPU and Memory hard limits configured per container in `deployment/docker-compose.prod.yml`. |
| **Restart Policies** | **Required** | `restart: unless-stopped` on all core production containers. |
| **Health Checks** | **Required** | Liveness and readiness HTTP probes configured on all containers. |
| **Graceful Shutdown** | **Required** | `STOPSIGNAL SIGTERM` with 30-second termination grace period (`server.shutdown: graceful` in Spring Boot). |
| **Read-Only Root Filesystem** | **Recommended**| Container root filesystems set to read-only where practical, using `tmpfs` mounts for `/tmp` and `/var/run`. |

---

## 11. CI/CD PIPELINE, SUPPLY CHAIN SECURITY & SBOM POLICY

### A. Multi-Layer Security Scanning Pipeline
1. **Secret Scanning**: Gitleaks / Trufflehog executed on every push to detect accidentally committed secrets or tokens.
2. **Dependency Vulnerability Scanning**:
   - Frontend: `npm audit --audit-level=high`
   - AI Service: `pip-audit`
   - Backend: OWASP Dependency-Check Maven plugin (`dependency-check:check`)
3. **Container Vulnerability Scanning**: Trivy container image scanner (`trivy image --severity HIGH,CRITICAL`).
4. **Software Bill of Materials (SBOM)**: Automated SBOM generation during CI/CD build using Syft (`syft dir:. -o cyclonedx-json=sbom.json`).

### B. Vulnerability Severity Policy:
- **HIGH & CRITICAL**: Automatically fail the CI/CD pipeline and block deployment until remediated or formally suppressed with justification.
- **LOW & MEDIUM**: Logged in security scan reports for scheduled maintenance remediation without failing builds.

### C. Staging & Production Approval Workflow:
```
Developer Push → CI Quality Gate (TSC/Vitest/Maven/Pytest)
  → Security Gate (Gitleaks/Trivy/SBOM) → Container Build
  → Staging Deployment → Automated Staging Smoke Tests
  → Production Approval Gate (Manual Sign-Off) → Production Controlled Replacement
```

---

## 12. SINGLE-HOST DEPLOYMENT STRATEGY & DOWNTIME TRANSPARENCY

### A. Deployment Mechanism & Canonical Compose Path
All production deployment, management, and verification commands use the single canonical path:
```bash
docker compose -f deployment/docker-compose.prod.yml up -d --build
```

### B. Single-Host Controlled Replacement Zero-Downtime Limitation:
> [!IMPORTANT]
> **DOWNTIME TRANSPARENCY**: Single-host controlled replacement strategy **DOES NOT guarantee zero downtime**. During container replacement (`docker compose -f deployment/docker-compose.prod.yml up -d --no-deps backend`), a brief **3 to 10 second application initialization window** occurs while the Spring Boot JVM initializes. True zero-downtime blue-green deployment requires an external load balancer and duplicate host infrastructure, which is documented as a multi-host enterprise extension.

---

## 13. CONTAINER RESOURCES AND LIFECYCLE MANAGEMENT

### A. Resource Allocation Matrix (`deployment/docker-compose.prod.yml`)

| Service | CPU Limit | CPU Reservation | Memory Limit | Memory Reservation |
| :--- | :--- | :--- | :--- | :--- |
| `postgres` | 2.0 cores | 0.5 cores | 2048 MB | 1024 MB |
| `backend` | 2.0 cores | 0.5 cores | 2048 MB | 1024 MB |
| `ai-service` | 2.0 cores | 0.25 cores | 2048 MB | 512 MB |
| `frontend` | 0.5 cores | 0.1 cores | 256 MB | 64 MB |
| `nginx-proxy` | 0.5 cores | 0.1 cores | 256 MB | 64 MB |

### B. Application Readiness vs Container Started:
- **Container Started**: Indicates process is running in container.
- **Application Ready**: Verified via HTTP probes:
  - Backend: `GET http://localhost:8080/actuator/health/readiness` (returns HTTP 200 `UP` only when database connection pool and Flyway migrations are ready).
  - AI Service: `GET http://localhost:8000/health` (returns HTTP 200 `healthy` when FastAPI router is ready).

---

## 14. REAL PRODUCTION EXPOSURE & ISOLATION TESTING

Testing production security enforces both external port rejection and internal container network connectivity:

### A. External Exposure Testing (From Outside Host / Public Network):
- `curl -I https://marabagam.org/` $\rightarrow$ Expect HTTP 200 OK (Port 443 OPEN).
- `curl -I http://marabagam.org/` $\rightarrow$ Expect HTTP 301 Redirect to HTTPS (Port 80 OPEN).
- `nc -zv marabagam.org 8080` $\rightarrow$ Expect Connection Refused / Timeout (Port 8080 CLOSED externally).
- `nc -zv marabagam.org 8000` $\rightarrow$ Expect Connection Refused / Timeout (Port 8000 CLOSED externally).
- `nc -zv marabagam.org 5432` $\rightarrow$ Expect Connection Refused / Timeout (Port 5432 CLOSED externally).

### B. Internal Container Network Connectivity Testing (Inside `marabagam-prod-net`):
- `docker exec marabagam-backend nc -zv postgres 5432` $\rightarrow$ Expect Connection Open (`backend` $\rightarrow$ `postgres` OK).
- `docker exec marabagam-backend nc -zv ai-service 8000` $\rightarrow$ Expect Connection Open (`backend` $\rightarrow$ `ai-service` OK).
- `docker exec marabagam-ai-service nc -zv postgres 5432` $\rightarrow$ Expect Connection Open (`ai-service` $\rightarrow$ `postgres` OK).

---

## 15. ACTUATOR SECURITY & PRODUCTION SCOPING

1. **Enabled Public Probes**: `/actuator/health/liveness` and `/actuator/health/readiness` enabled for health checks (`show-details: never` for unauthenticated requests).
2. **Enabled Metrics**: `/actuator/prometheus` enabled for metric scraping.
3. **Disabled Endpoints**: `/actuator/env`, `/actuator/beans`, `/actuator/heapdump`, `/actuator/configprops`, `/actuator/shutdown` strictly disabled in `application-prod.yml`.
4. **Access Control**: Actuator endpoints restricted to internal administration subnet or protected by `ADMIN` role authentication.

---

## 16. PRODUCTION CORS ENFORCEMENT

- **Allowed Origins**: Bound strictly to production domain: `https://marabagam.org,https://www.marabagam.org`.
- **Development Origins**: `localhost` and `127.0.0.1` stripped in `application-prod.yml`.
- **Wildcard (`*`)**: Strictly prohibited on authenticated production API endpoints.

---

## 17. LAYERED RATE LIMITING DEMARCATION

To avoid overlapping lockout scenarios:
- **Nginx Proxy**: Network IP flood protection (20 req/s per IP).
- **Spring Boot (Auth)**: Brute-force login protection (5 attempts/min per IP).
- **Spring Boot (API)**: General API abuse protection (100 req/min per user).
- **AI Microservice**: Expensive LLM/RAG query protection (10 queries/min per user).
- **Community**: Report creation abuse limit (5 reports/hour per user).

---

## 18. SCHEDULER OBSERVABILITY & TELEMETRY

Production monitoring tracks Phase 9 background event reminder scheduler execution:
- Metrics: `scheduler.event_reminder.execution.count`, `scheduler.event_reminder.execution.duration`, `scheduler.event_reminder.notifications_sent.count`, `scheduler.event_reminder.failures.count`.
- Alerting: Alert triggered if scheduler execution throws an exception or if execution age exceeds **15 minutes**.

---

## 19. EXPANDED PHASE 1–9 PRODUCTION SMOKE TEST MATRIX

| Phase | Subsystem | Production Smoke Test Verification Procedure | Expected Result |
| :--- | :--- | :--- | :--- |
| **Phase 1** | Foundation | `GET /api/v1/health` & Flyway schema history check | HTTP 200 `UP`, V1–V9 applied |
| **Phase 2** | Auth & RBAC | POST `/api/v1/auth/login` with valid credentials | HTTP 200, JWT returned, HttpOnly `refreshToken` cookie set |
| **Phase 3** | Art Directory | GET `/api/v1/art-forms` (public access) | HTTP 200, JSON list of published art forms returned |
| **Phase 4** | E-learning | GET `/api/v1/courses`, GET `/api/v1/courses/{slug}/lessons` | HTTP 200, course hierarchy & progress tracking functional |
| **Phase 5** | AI Guide / RAG| POST `/api/v1/ai-guide/query` with heritage query | HTTP 200, response generated with source attributions |
| **Phase 6** | Heritage Map | GET `/api/v1/heritage-locations` | HTTP 200, geo-coordinates & linked events returned |
| **Phase 7** | Movement Coach| POST `/api/v1/practice-sessions/{id}/evaluate` with 3D keypoints | HTTP 200, biomechanical feedback returned; raw video NOT sent |
| **Phase 8** | Quizzes/Badges| POST `/api/v1/quizzes/{id}/submit`, GET `/api/v1/certificates/verify/{number}` | HTTP 200, score calculated, certificate HMAC verified |
| **Phase 9** | Events & UGC | GET `/api/v1/events`, POST `/api/v1/events/{id}/register`, POST `/api/v1/community/posts` | HTTP 200/201, registration capacity checked, UGC saved |

---

## 20. PRESERVATION OF RAG & MOVEMENT COACH BOUNDARIES

1. **Verified RAG Knowledge Boundary (Phase 5/9)**: Community UGC (posts/comments) stored in `community_posts` and `community_comments`. Vector store (`cultural_rag_documents`) contains **ONLY verified cultural documentation** approved by `CULTURAL_EXPERT` or `ADMIN`. Zero UGC enters RAG vector index.
2. **Movement Coach Privacy (Phase 7)**: Raw camera video processed locally in browser RAM and discarded frame-by-frame; NEVER sent to server. Derived 33 3D keypoint arrays only transmitted. Production logs scrub coordinate arrays.

---

## 21. DEVELOPMENT ENVIRONMENT PRESERVATION

- `docker-compose.yml` remains clean development configuration.
- Production overlays and assets located in `deployment/docker-compose.prod.yml` and `deployment/`.
- Platform compatibility across Windows, macOS, and Linux maintained.

---

## 22. FINAL IMPLEMENTATION DECISIONS

1. **Reverse Proxy & TLS**: Nginx Reverse Proxy container handling Let's Encrypt TLS termination, HTTP 301 redirects, and proxy routing.
2. **Secret Scoping**: `OPENAI_API_KEY` scoped exclusively to `ai-service`; `MARABAGAM_CERTIFICATE_SECRET`, `JWT_SECRET`, `POSTGRES_PASSWORD` scoped to Spring Boot backend.
3. **Backup Strategy**: Daily automated GPG-encrypted PostgreSQL logical backups (`pg_dump`) to S3 with weekly isolated restore verification (`RESTORE_TARGET=marabagam_restore_test`).
4. **AI Worker Pool**: Configurable Gunicorn process manager with `AI_WORKERS` Uvicorn ASGI workers.
5. **Single-Host Replacement**: Single-host Docker Compose deployment via `deployment/docker-compose.prod.yml` with explicit acknowledgement of 3–10s JVM restart window.

---

## 23. DEFINITION OF DONE FOR PHASE 10

Phase 10 planning is complete and ready for execution approval when:
- [ ] `deployment/docker-compose.prod.yml` canonical path defined with private network isolation (`marabagam-prod-net`).
- [ ] `deployment/nginx-proxy.conf` defined with TLS termination, code-inspected CSP, security headers, and IP rate limiting.
- [ ] `backend/src/main/resources/application-prod.yml` defined with fail-fast validation for `MARABAGAM_CERTIFICATE_SECRET`, `JWT_SECRET`, `POSTGRES_PASSWORD`.
- [ ] `ai-service/Dockerfile` updated for Gunicorn + `AI_WORKERS` Uvicorn ASGI worker execution.
- [ ] GPG-encrypted `deployment/scripts/backup.sh` and isolated `deployment/scripts/restore.sh` (`RESTORE_TARGET=marabagam_restore_test`) created.
- [ ] `.github/workflows/ci.yml` updated with Gitleaks secret scanning, dependency audit, Trivy container scan, Syft SBOM generation, staging deploy, and production manual approval gate.
- [ ] Phase 1–9 production smoke test matrix verified on staging.
- [ ] `docs/phase-10-report.md` generated upon implementation execution.

---

NO SOURCE CODE CHANGES MADE
NO DATABASE CHANGES MADE
NO DEPENDENCY INSTALLATIONS PERFORMED
NO DEPLOYMENT PERFORMED
