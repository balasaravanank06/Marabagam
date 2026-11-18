# Deployment Guidelines for Marabagam

## Containerized Deployment

### 1. Local Development Topology
Use `docker-compose.yml` located in the root repository for development:

```bash
cp .env.example .env
docker compose up -d
```

### 2. Production Topology (Phase 10 Standard)
Use `deployment/docker-compose.prod.yml` for production deployments:

```bash
# Set production environment variables
cp .env.example .env
# Set SPRING_PROFILES_ACTIVE=prod, POSTGRES_PASSWORD, JWT_SECRET, MARABAGAM_CERTIFICATE_SECRET, OPENAI_API_KEY, etc.

# Run production compose stack
docker compose -f docker-compose.yml -f deployment/docker-compose.prod.yml up -d --build
```

## Production Security & Isolation Topology
- **Public Traffic**: Only Nginx Reverse Proxy (ports 80 and 443) is exposed to host/public interface.
- **Internal Microservice Network (`marabagam-prod-net`)**:
  - PostgreSQL (5432) is isolated internally — **NOT** exposed publicly.
  - Spring Boot API (8080) is isolated internally — proxy-only access via Nginx.
  - FastAPI AI Service (8000) is isolated internally — accessible only to Spring Boot and Nginx.
- **Secrets Management**:
  - `OPENAI_API_KEY` is isolated strictly to `ai-service`.
  - Backend mandates non-default `POSTGRES_PASSWORD`, `JWT_SECRET`, and `MARABAGAM_CERTIFICATE_SECRET` via `ProductionSecretValidator`.

## Operations, Backup & Restore
- **Operational Runbook**: See detailed instructions in [docs/production-ops.md](file:///home/bala/Marabagam/docs/production-ops.md).
- **Encrypted Logical Backup**: `./deployment/scripts/backup.sh`
- **Isolated Target Restore Verification**: `./deployment/scripts/restore.sh marabagam_restore_test /path/to/backup.sql.gz.gpg`

