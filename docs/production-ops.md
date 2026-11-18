# MARABAGAM PRODUCTION OPERATIONS & RUNBOOK

This operational runbook details administrative procedures for maintaining, monitoring, backing up, and deploying the Marabagam platform in production.

---

## 1. PRODUCTION ARCHITECTURE OVERVIEW

The production deployment uses a containerized multi-tier architecture managed via `deployment/docker-compose.prod.yml`:

```
                       [ PUBLIC INTERNET ]
                                │
                          (HTTPS :443)
                                │
                      ┌─────────▼─────────┐
                      │ NGINX REVERSE     │ (Ports 80/443 Public)
                      │ PROXY EDGE GATEWAY│
                      └─────────┬─────────┘
                                │ (Internal Subnet: marabagam-prod-net)
             ┌──────────────────┼──────────────────┐
             │                  │                  │
             ▼                  ▼                  ▼
      ┌────────────┐     ┌────────────┐     ┌────────────┐
      │ React SPA  │     │Spring Boot │     │ S3 Object  │
      │ (Static)   │     │API Backend │     │ Storage    │
      └────────────┘     └──────┬─────┘     └────────────┘
                                │
                                ▼
                       ┌────────────────┐
                       │ FastAPI AI     │
                       │ Microservice   │
                       └────────┬───────┘
                                │
              ┌─────────────────┴────────────────┐
              ▼                                  ▼
     ┌────────────────┐                 ┌────────────────┐
     │ PostgreSQL 16  │                 │  Redis Cache   │
     │  + pgvector    │                 │ & Rate Limiter │
     └────────────────┘                 └────────────────┘
```

### Network Access Control:
- **Publicly Accessible**: Nginx Reverse Proxy (Ports 80, 443).
- **Private Subnet Only (`marabagam-prod-net`)**:
  - `postgres` (Port 5432 - closed to external host access).
  - `ai-service` (Port 8000 - closed to external host access).
  - `backend` (Port 8080 - closed to external host access).

---

## 2. ENVIRONMENT CONFIGURATION & SECRETS MANAGEMENT

Production secrets MUST be provided via secure runtime environment variables.

### Secret Scoping & Isolation:
1. **Spring Boot Backend**:
   - `POSTGRES_PASSWORD`: Production database user password.
   - `JWT_SECRET`: 512-bit HS512 secret string (min 64 chars).
   - `MARABAGAM_CERTIFICATE_SECRET`: Certificate QR verification HMAC signing key (min 32 chars).
   - *Note*: Spring Boot backend does NOT require `OPENAI_API_KEY`.
2. **FastAPI AI Microservice**:
   - `OPENAI_API_KEY`: API key for OpenAI RAG embeddings and LLM responses. Scoped exclusively to `ai-service`.
   - `AI_WORKERS`: Number of Gunicorn worker processes (defaults to `4`).

---

## 3. SINGLE-HOST CONTROLLED REPLACEMENT DEPLOYMENT

### Deployment Command:
```bash
docker compose -f deployment/docker-compose.prod.yml up -d --build
```

### Zero-Downtime Limitation & Restart Window:
> [!IMPORTANT]
> Single-host controlled replacement strategy **does not guarantee zero downtime**. Replacing the backend container (`docker compose -f deployment/docker-compose.prod.yml up -d --no-deps backend`) incurs a brief **3 to 10 second application replacement window** while the Spring Boot JVM initializes and completes readiness probes.

### Rollback Procedure:
If health checks fail during rollout:
```bash
docker compose -f deployment/docker-compose.prod.yml rollback
```
Or revert git state and execute container replacement to redeploy previous image tag.

---

## 4. TLS CERTIFICATE MANAGEMENT (LET'S ENCRYPT / CERTBOT)

### Initial Issuance:
```bash
docker exec marabagam-nginx-proxy certbot certonly --webroot -w /var/www/certbot -d marabagam.org -d www.marabagam.org
```

### Automated Renewal & Nginx Reload:
Certbot container executes renewal check twice daily. Upon renewal, Nginx receives `SIGHUP` signal:
```bash
docker exec marabagam-nginx-proxy nginx -s reload
```

---

## 5. BACKUP & ISOLATED RESTORE PROCEDURES

### Daily GPG-Encrypted Logical Backup (`backup.sh`):
```bash
./deployment/scripts/backup.sh
```
- Creates `pg_dump` compressed custom dump.
- Encrypts archive using GPG AES-256.
- Uploads to encrypted S3 bucket (`s3://marabagam-backups/db/`).

### Safe Isolated Restore Verification (`restore.sh`):
> [!CAUTION]
> NEVER run restore verification against production database `marabagam_db`!

Execute test restoration into temporary isolated database `marabagam_restore_test`:
```bash
./deployment/scripts/restore.sh marabagam_restore_test /path/to/backup.sql.gz.gpg
```
- Decrypts backup archive into temporary memory space.
- Restores database schema into `marabagam_restore_test`.
- Verifies table counts, Flyway history records, and core data rows (`users`, `courses`, `cultural_events`).
- Automatically drops temporary database upon successful verification.

---

## 6. MEDIA STORAGE RECOVERY

Media assets are stored in AWS S3 or S3-compatible object storage:
- **Public Assets**: `s3://marabagam-media-public/` (avatars, thumbnails).
- **Private Assets**: `s3://marabagam-media-private/` (lesson media, movement keypoint baselines).

Disaster Recovery Sync:
```bash
aws s3 sync s3://marabagam-media-public/ s3://marabagam-media-public-backup/
```

---

## 7. OBSERVABILITY & SCHEDULER MONITORING

### Health & Metrics Probes:
- Liveness Probe: `GET http://localhost:8080/actuator/health/liveness`
- Readiness Probe: `GET http://localhost:8080/actuator/health/readiness`
- Prometheus Metrics: `GET http://localhost:8080/actuator/prometheus` (Admin/Private Net Only)

### Event Reminder Scheduler Monitoring:
- Log metrics tracked: `scheduler.event_reminder.execution.count`, `scheduler.event_reminder.execution.duration`, `scheduler.event_reminder.failures.count`.
- Alerting: Triggered if scheduler execution age exceeds 15 minutes or encounters unhandled exceptions.

---

## 8. AI SERVICE GRACEFUL DEGRADATION

If OpenAI or FastAPI AI service is offline:
- Spring Boot HTTP client enforces 10-second timeout with 1 max retry.
- Spring Boot returns fallback response ("AI Cultural Guide operating in offline mode").
- Core features (Art Directory, Courses, Quizzes, Certificates, Events, Community) remain 100% operational.
