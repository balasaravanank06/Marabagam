# Marabagam Architecture Documentation

## Overview
Marabagam is a digital heritage preservation and e-learning platform for Tamil traditional arts.

The system is designed as a modular, decoupled containerized monorepo.

## System Components

```
                      +-------------------+
                      |   Client Browser  |
                      |  (Desktop/Mobile) |
                      +---------+---------+
                                |
                                | HTTP / JSON
                                v
                      +-------------------+
                      |     Frontend      |
                      | (React + TS + Vite|
                      |  + Tailwind CSS)  |
                      +---------+---------+
                                |
                                | REST API (/api/v1)
                                v
                      +-------------------+
                      |      Backend      |
                      | (Java 21 + Spring |
                      |   Boot 3.3.x)     |
                      +----+---------+----+
                           |         |
          Internal REST    |         | SQL (JPA / Flyway)
         (http://ai:8000)  v         v
         +-------------------+     +-------------------+
         |    AI Service     |     |    PostgreSQL     |
         | (Python FastAPI)  |     |   (Database)      |
         +-------------------+     +-------------------+
```

## Security Architecture Baseline
- CORS configuration dynamically loaded from `CORS_ALLOWED_ORIGINS`
- Environment variables manage all credentials and passwords
- No secret hardcoding
- Stateless REST architecture foundation with Spring Security filter chain

## Future Module Readiness
- Authentication & RBAC (`auth`)
- Tamil Traditional Art Directory (`artform`)
- Courses & Multimedia Lessons (`course`, `lesson`, `media`)
- Artist & Expert Profiles (`artist`)
- Interactive Heritage Map (`heritage`)
- Quizzes, Assessments & Certificates (`quiz`, `certificate`)
- AI Cultural Guide (RAG) & Movement Coach (`ai-service`)
