# Marabagam: An AI-Powered Digital Heritage Preservation and E-Learning Platform for Tamil Traditional Arts

---

## 1. Project Name
**Marabagam (மரபகம்)** — Digital Heritage Preservation and E-Learning Ecosystem for Tamil Traditional Arts.

---

## 2. Project Description
Marabagam is a digital platform built to preserve, catalog, and teach Tamil traditional arts (such as Bharatanatyam, Karakattam, Therukoothu, Silambam, Carnatic music, and ancient Tamil craft arts). 

Future modules will include structured bilingual e-learning courses, artist profiles, an interactive Tamil Nadu heritage map, assessments, digital certificates, an AI Cultural Guide using Retrieval-Augmented Generation (RAG), and an AI Movement Coach using computer vision and pose estimation.

*Phase 1 establishes the production-ready monorepo foundation, cross-platform containerization, security baseline, and microservice infrastructure.*

---

## 3. Architecture Overview

```
                      +-------------------+
                      |   Client Browser  |
                      | (Desktop & Mobile)|
                      +---------+---------+
                                |
                                | HTTP / JSON
                                v
                      +-------------------+
                      |     Frontend      |
                      | (React 18 + TS    |
                      | + Vite + Tailwind)|
                      +---------+---------+
                                |
                                | REST API (/api/v1)
                                v
                      +-------------------+
                      |      Backend      |
                      | (Java 21 + Spring |
                      |   Boot 3.3.3)     |
                      +----+---------+----+
                           |         |
          Internal REST    |         | SQL (JPA / Flyway)
        (http://ai-service)v         v
         +-------------------+     +-------------------+
         |    AI Service     |     |    PostgreSQL     |
         | (Python FastAPI)  |     |   (Database)      |
         +-------------------+     +-------------------+
```

---

## 4. Technology Stack
- **Frontend**: React 18, TypeScript 5.5, Vite 5.4, Tailwind CSS 3.4, React Router DOM 6.26, i18next (Tamil & English), Vitest.
- **Backend**: Java 21, Spring Boot 3.3.3, Spring Security 6, Spring Data JPA, Spring Validation, Flyway Migrations, PostgreSQL Driver, JUnit 5.
- **Database**: PostgreSQL 16 Alpine.
- **AI Service**: Python 3.12, FastAPI 0.116, Pydantic v2, Uvicorn, Pytest.
- **Infrastructure**: Docker, Docker Compose, GitHub Actions CI.

---

## 5. Repository Structure
```
Marabagam/
├── .editorconfig              # Cross-platform code style definitions
├── .env.example               # Environment variables template
├── .gitattributes             # Line-ending enforcement (LF/CRLF)
├── .gitignore                 # Cross-platform git ignore rules
├── .github/
│   └── workflows/
│       └── ci.yml             # GitHub Actions CI pipeline
├── LICENSE                    # Apache-2.0 License
├── README.md                  # Project master documentation
├── docker-compose.yml         # Container orchestration manifest
├── docs/                      # Architectural documentation
├── deployment/                # Production deployment manifests
├── scripts/                   # Development startup scripts (dev.sh, dev.ps1)
├── database/                  # Schema documentation & ERD diagrams
├── frontend/                  # React + TS + Vite SPA
├── backend/                   # Spring Boot REST microservice
└── ai-service/                # Python FastAPI microservice
```

---

## 6. Prerequisites
- **Docker**: Docker Desktop 4.x+ (Windows/macOS) or Docker Engine 24.x+ & Docker Compose v2 (Linux)
- **Node.js**: Node.js 20.x or 24.x & npm 10.x+
- **Java**: Java 21 LTS JDK
- **Python**: Python 3.10 to 3.12 & pip
- **Git**: Git 2.30+

---

## 7. Environment Configuration
Copy `.env.example` to `.env` before starting:

```bash
cp .env.example .env
```

### Essential Environment Variables
| Variable | Description | Development Default |
| :--- | :--- | :--- |
| `FRONTEND_PORT` | Host port for React UI | `5173` |
| `BACKEND_PORT` | Host port for Spring Boot API | `8080` |
| `AI_SERVICE_PORT` | Host port for FastAPI AI Service | `8000` |
| `POSTGRES_PORT` | Host port for PostgreSQL | `5432` |
| `POSTGRES_DB` | Database name | `marabagam_db` |
| `POSTGRES_USER` | Database username | `marabagam_user` |
| `POSTGRES_PASSWORD` | Database password | `marabagam_dev_password` |
| `VITE_API_BASE_URL` | Frontend API client endpoint | `http://localhost:8080/api/v1` |

---

## 8. Docker Setup (Recommended Canonical Workflow)
To build and run all 4 services in Docker:

```bash
# Clone repository
git clone https://github.com/marabagam/marabagam.git
cd marabagam

# Setup environment
cp .env.example .env

# Start development containers
docker compose up -d --build

# Verify container status
docker compose ps
```

### Production Docker Setup (Phase 10 Standard)
```bash
# Deploy using production override overlay (ports 80 & 443 only exposed publicly)
docker compose -f docker-compose.yml -f deployment/docker-compose.prod.yml up -d --build
```
For full operational guidance, Let's Encrypt TLS termination, GPG-encrypted backup/restore procedures, and CI/CD pipelines, see the [Production Operations Runbook](file:///home/bala/Marabagam/docs/production-ops.md) and [Deployment README](file:///home/bala/Marabagam/deployment/README.md).

---

## 9. Native Development Setup
Developers can also run individual components natively on their host machine.

---

## 10. Windows Instructions
```powershell
# In PowerShell:
Copy-Item .env.example .env

# Option A: Run via Docker Compose
.\scripts\dev.ps1 -Action docker

# Option B: Run Frontend
cd frontend; npm install; npm run dev

# Option C: Run Backend
cd backend; .\mvnw.cmd spring-boot:run

# Option D: Run AI Service
cd ai-service; python -m venv venv; .\venv\Scripts\activate; pip install -r requirements.txt; uvicorn app.main:app --reload
```

---

## 11. Linux Instructions
```bash
# In Bash:
cp .env.example .env

# Option A: Run via Docker Compose
./scripts/dev.sh docker

# Option B: Native execution
cd frontend && npm install && npm run dev
cd backend && ./mvnw spring-boot:run
cd ai-service && python3 -m venv venv && source venv/bin/activate && pip install -r requirements.txt && uvicorn app.main:app --reload
```

---

## 12. macOS Instructions
```bash
cp .env.example .env
./scripts/dev.sh docker
```

---

## 13. Running Frontend
- **Port**: `http://localhost:5173`
- **Commands**:
  ```bash
  cd frontend
  npm install
  npm run dev      # Development server
  npm run build    # Production build check
  npm test         # Unit test runner
  ```

---

## 14. Running Backend
- **Health Endpoint**: `http://localhost:8080/api/v1/health`
- **Commands**:
  ```bash
  cd backend
  ./mvnw clean test         # Run unit & integration tests
  ./mvnw spring-boot:run   # Start Spring Boot backend
  ```

---

## 15. Running PostgreSQL
- **Port**: `5432`
- **Native/Docker run**:
  ```bash
  docker run -d --name marabagam-postgres -p 5432:5432 \
    -e POSTGRES_DB=marabagam_db \
    -e POSTGRES_USER=marabagam_user \
    -e POSTGRES_PASSWORD=marabagam_dev_password \
    postgres:16-alpine
  ```

---

## 16. Running AI Service
- **Health Endpoint**: `http://localhost:8000/health`
- **Commands**:
  ```bash
  cd ai-service
  python3 -m venv venv
  source venv/bin/activate
  pip install -r requirements.txt
  pytest                     # Run unit tests
  uvicorn app.main:app --reload --port 8000
  ```

---

## 17. Testing
```bash
# Frontend tests (Vitest)
cd frontend && npm test -- --run

# Backend tests (JUnit 5)
cd backend && ./mvnw test

# AI Service tests (Pytest)
cd ai-service && pytest
```

---

## 18. Troubleshooting
- **Port Conflict**: Update `FRONTEND_PORT`, `BACKEND_PORT`, `AI_SERVICE_PORT`, or `POSTGRES_PORT` in `.env`.
- **Line Ending Issues on Windows**: Git automatically converts text files via `.gitattributes`. Run `git checkout-index --force --all` if shell scripts contain `\r`.
- **Database Connection Error**: Ensure PostgreSQL container is healthy before launching Spring Boot backend.

---

## 19. Security Notes
- No plain-text passwords or secret keys are committed to Git repository.
- Docker containers run with non-root user permissions (`marabagam`, `appuser`, `nginx`).
- Spring Security 6 filter chain is configured for CORS allowed origins dynamically fetched from environment variables.
- Production log level disables raw SQL formatting and stack trace leakage.

---

## 20. Contribution Workflow
1. Create a feature branch: `git checkout -b feature/your-feature-name`
2. Commit changes with clear messages following standard conventions.
3. Push to remote: `git push origin feature/your-feature-name`
4. Submit Pull Request targeting `develop` branch.
