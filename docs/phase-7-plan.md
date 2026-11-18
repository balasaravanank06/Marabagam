# Implementation Plan — Phase 7: AI Movement Coach / Computer Vision Module (Amended)

This document details the amended technical design and technical implementation plan for **Phase 7: AI Movement Coach / Computer Vision Module** for the **Marabagam** digital heritage preservation platform.

> [!IMPORTANT]
> **PLANNING-ONLY NOTICE**: This document represents the amended technical design plan for Phase 7. Per strict project directives, no production code, unit tests, database migrations, or dependency changes have been executed. Implementation will commence only after external review and explicit approval.

---

## 1. Current Architecture Assessment

Marabagam is a modern, containerized, full-stack application built across three core tiers and a dedicated AI microservice:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Client / Browser Tier                         │
│   React 18 + TypeScript + Vite + TailwindCSS + i18next + Lucide Icons   │
│   (Local Real-Time Visual Guidance & WebAssembly Pose Extraction)       │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ HTTPS REST / Cookies (JWT)
                                     │ (Transmits Derived Keypoint JSON Only)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                             Backend Tier                                │
│          Spring Boot 3 (Java 21) REST API + Spring Security             │
│    (Role-Based Access Control: GUEST, LEARNER, ARTIST, INSTRUCTOR,      │
│                        CULTURAL_EXPERT, ADMIN)                          │
└──────────────┬──────────────────────────────────────────┬───────────────┘
               │ JPA / Hibernate                          │ Internal REST
               ▼                                          ▼
┌─────────────────────────────┐           ┌───────────────────────────────┐
│       Database Tier         │           │        AI Service Tier        │
│   PostgreSQL 16 + pgvector  │           │   Python 3.12 + FastAPI       │
│  Flyway Migrations (V1–V6)  │           │  (Kinematics, FastDTW, RAG)   │
└─────────────────────────────┘           └───────────────────────────────┘
```

### Key Architectural Traits to Preserve:
1. **Security & Authentication**: Dual-token architecture (Short-lived JWT in memory + HttpOnly `refreshToken` cookie), Spring Security filters, server-side RBAC, and `RateLimitingFilter`.
2. **PostgreSQL & Flyway Schema**: UUID primary keys, strict foreign keys, timestamp auditing (`created_at`, `updated_at`), versioned SQL migrations (`V1` through `V6`).
3. **Spring Boot ↔ FastAPI Integration**: Synchronous JSON REST communication where Spring Boot acts as the authoritative API gateway and business logic orchestrator, delegating complex biomechanical calculations to FastAPI over `http://ai-service:8000`.
4. **Cross-Platform Containerization**: Dockerized deployment via `docker-compose.yml` (`marabagam-net` bridge network) ensuring zero OS-specific host dependencies.

---

## 2. Existing Components Relevant to Phase 7

| Component Area | Files / System | Integration Point for Phase 7 |
| :--- | :--- | :--- |
| **Database Migrations** | `backend/src/main/resources/db/migration/` | `V7__movement_coach.sql` will extend schema with `movements`, `movement_versions`, `reference_movements`, `practice_sessions`, and `practice_feedbacks`. |
| **Spring Boot Domain & API** | `backend/src/main/java/com/marabagam/` | New package `com.marabagam.movement` containing entities, repositories, services, and controllers under `/api/v1/practice-sessions` & `/api/v1/movements`. |
| **FastAPI Service** | `ai-service/app/` | New package `app/movement` with API routers under `/internal/v1/movement/analyze`, separating CV algorithms completely from `app/rag`. |
| **Frontend Routing & Shell** | `frontend/src/app/App.tsx`, `AppShell.tsx` | New routes `/movement-coach`, `/movement-coach/:movementId/practice`, and navigation item in `AppShell`. |
| **User Role System** | `User.java`, `Role.java` (`LEARNER`, `ARTIST`, `INSTRUCTOR`, `CULTURAL_EXPERT`, `ADMIN`) | `LEARNER` practice session ownership; `CULTURAL_EXPERT` & `ADMIN` approval permissions for expert reference dataset. |
| **Art Directory** | `V3__art_form_directory.sql`, `ArtForm.java` | Foreign key binding between traditional art forms (e.g. Silambam) and movement catalog entries. |

---

## 3. Phase 7 Objectives

The primary objective of Phase 7 is to empower Tamil art learners to voluntarily practice selected traditional art movements using their device camera, receiving real-time local guidance and post-practice AI-assisted biomechanical feedback.

### Core Goals:
- **Privacy Boundary**: Raw camera/video processing and pose extraction occur on-device; only derived pose/keypoint data required for evaluation is transmitted over HTTPS to backend services.
- **Biomechanical Analysis**: Measure derived joint angles, stance width ratios, limb positioning, torso inclination, and movement timing relative to expert-approved baselines.
- **Explainable & Respectful Feedback**: Provide metric-driven improvement hints without claiming cultural authenticity, artistic perfection, or absolute correctness over traditional art forms.
- **Extensible Architecture**: Initial support for a controlled starting set of Silambam movements, structured for easy expansion to other Tamil traditional arts in future phases.

---

## 4. MVP Scope & Starting Art Selection

The Phase 7 Minimum Viable Product (MVP) will focus on **Silambam** (Tamil traditional staff martial art):

### Starting Art & Movement Selection Criteria
- **Selected Art**: **Silambam**
  - *Selection Rationale*:
    1. Clear discrete physical posture and stance rules (*Subadu*), making biomechanical joint angles distinct and quantifiable.
    2. High landmark visibility in front-facing camera orientation without complex self-occlusion or inversion.
    3. Already cataloged in Marabagam Phase 3 art directory and Phase 4 learning curriculum.
- **MVP Movement Candidate Set (3 Controlled Movements)**:
  1. **Silambam Stance — *Maankombu Subadu* (Deer-Horn / Base Stance)**: Static posture evaluating hip height ratio, knee bend angles, torso uprightness, and stance width relative to shoulder width.
  2. **Silambam Basic Strike Guard — *Straight Stance Guard***: Static guard posture evaluating shoulder elevation, elbow flexion angle, guard height relative to sternum, and chest-to-hip vertical stack.
  3. **Silambam Basic Swing Phase — *Errattai Chuttu* (Double Rotation Basic Phase)**: Dynamic 3-phase swing sequence evaluating shoulder rotation synchronization, elbow extension timing, and stance balance over a 3–5 second window.

> [!NOTE]
> All biomechanical target ranges in the MVP are **not hard-coded universal facts**. They serve as configurable baseline parameters derived from expert-approved reference data and subject to empirical validation.

---

## 5. Out-of-Scope Items

To prevent scope creep, the following features are strictly deferred to future phases:
- ❌ Audio-based rhythm/talam matching or music synchronization.
- ❌ Multiplayer/live dual-learner movement comparison.
- ❌ Gamification badges, leaderboards, or digital certificates (reserved for Phase 8+).
- ❌ Automated generation of new 3D avatar animations.
- ❌ Server-side video storage, stream transcoding, or raw clip rendering.
- ❌ AR/VR headset integrations.

---

## 6. Dual-Stage Computer-Vision Architecture

The system explicitly separates **Local Real-Time Visual Guidance** from **Server-Side Post-Practice Detailed Evaluation**:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          STAGE A: Client Browser                            │
│                  (Local Real-Time Visual Guidance Layer)                    │
│                                                                             │
│  [User Opt-In] ──► [Camera Feed (<video>)] ──► [@mediapipe/pose WASM Module] │
│                                                          │                  │
│                                            Extract 33 3D Keypoints          │
│                                                          ▼                  │
│  [Canvas Skeleton Overlay] ◄── [Local Angle Math] ──► [Keypoint JSON Buffer] │
│  (Instant Visual Feedback)                             (In-Memory Array)    │
└──────────────────────────────────────────────────────────┬──────────────────┘
                                                           │ POST Derived JSON
                                                           │ (No Raw Video)
                                                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                             Spring Boot Backend API                         │
│  • Validates JWT Auth & Learner Ownership                                  │
│  • Enforces Payload Size Limits & Rate Limiting                             │
│  • Validates Coordinate Bounds (x,y in [0,1])                               │
│  • Fetches Immutable Reference Version from PostgreSQL                      │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │ REST / internal API
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          STAGE B: FastAPI AI Microservice                   │
│                (Server-Side Post-Practice Detailed Evaluation)              │
│                                                                             │
│  • Endpoint: /internal/v1/movement/analyze                                  │
│  • Coordinate Normalization (Hip-Centric & Torso-Scaled)                    │
│  • Kinematic Joint Angle Time-Series Computation                            │
│  • FastDTW Trajectory Alignment & Geometric Delta Analysis                  │
│  • Generates Structured Bilingual Metric Feedback DTO                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 7. Single Pose-Estimation Stack Selection

To maintain code maintainability, bundle size, and architectural simplicity, **exactly ONE pose estimation stack** is selected for the MVP:

- **Selected Stack**: `@mediapipe/pose` (Native MediaPipe WebAssembly JavaScript library).
- **Stack Justification**: Provides 33 full 3D skeletal landmarks directly in browser memory without requiring TensorFlow.js wrapper overhead.

### Single-Person Pose Detection Realities & Degradation Handling
MediaPipe Pose is optimized for single-person pose tracking. It does not perform multi-person detection.
- **Handling Multi-Person / Frame Quality Issues**:
  - If multiple individuals enter the camera frame, single-person pose estimation will experience landmark jitter, rapid bounding-box switching, or confidence drops.
  - The system handles this by checking landmark visibility confidence ($c_i < 0.6$) and torso bounding-box stability.
  - If confidence drops or severe frame jitter occurs, local guidance displays a clear warning card: *"Frame alignment unsteady. Please ensure only one learner is standing in front of the camera in clear lighting."*

---

## 8. Exact Coordinate System & Input Validation

### Coordinate Convention Specification
Landmark input payloads transmitted from the client MUST conform to the exact MediaPipe Pose coordinate convention:
- **Image Plane Coordinates**: $x \in [0.0, 1.0]$ (normalized from left to right), $y \in [0.0, 1.0]$ (normalized from top to bottom).
- **Relative Depth Coordinate**: $z$ (normalized depth scale anchored relative to mid-hip center).
- **Confidence Metrics**: $visibility \in [0.0, 1.0]$ and $presence \in [0.0, 1.0]$.

```
      (0,0) ┌────────────────────────┐
            │   (x, y in [0.0, 1.0]) │
            │           O            │  Head / Shoulders
            │          /|\           │  Arms / Torso
            │          / \           │  Hips / Legs
            └────────────────────────┘ (1,1)
```

### Backend Validation Rules
Spring Boot and FastAPI validate incoming JSON keypoint arrays before processing:
- Reject payloads with missing mandatory joints (Hips, Knees, Ankles, Shoulders, Elbows, Wrists).
- Reject out-of-bounds spatial values ($x < 0.0$ or $x > 1.0$, $y < 0.0$ or $y > 1.0$) or out-of-bounds visibility ($visibility < 0.0$ or $visibility > 1.0$).
- Reject payloads containing `NaN`, `Infinity`, or malformed numeric strings with `400 Bad Request`.

---

## 9. Feature Extraction & Coordinate Normalization

To ensure invariant comparison regardless of camera aspect ratio, user distance, or height:

1. **Mid-Hip Origin Anchor Centering**: Shift origin to mid-hip center:
   $$C_{hip} = \frac{\mathbf{P}_{left\_hip} + \mathbf{P}_{right\_hip}}{2}, \quad \mathbf{P}'_i = \mathbf{P}_i - C_{hip}$$
2. **Torso Length Scaling**: Scale all distances by torso length $D_{torso}$ to normalize for height:
   $$D_{torso} = \|\mathbf{P}_{mid\_shoulder} - \mathbf{P}_{mid\_hip}\|_2, \quad \mathbf{P}''_i = \frac{\mathbf{P}'_i}{D_{torso}}$$
3. **3D Biomechanical Joint Angle Kinematics**: Compute vector angles:
   $$\theta = \arccos\left( \frac{\mathbf{v}_1 \cdot \mathbf{v}_2}{\|\mathbf{v}_1\|_2 \|\mathbf{v}_2\|_2} \right)$$
   - *Knee Flexion Angle*: Vector(Knee $\rightarrow$ Hip) vs Vector(Knee $\rightarrow$ Ankle).
   - *Elbow Flexion Angle*: Vector(Elbow $\rightarrow$ Shoulder) vs Vector(Elbow $\rightarrow$ Wrist).
   - *Spinal Upright Angle*: Vector(Mid-Hip $\rightarrow$ Mid-Shoulder) vs Vertical axis $(0, 1, 0)$.
   - *Stance Width Ratio*: Distance(Left Ankle, Right Ankle) / $D_{shoulder\_width}$.

---

## 10. Multi-Stage Expert Reference Data Governance

The lifecycle of expert reference data enforces strict editorial control, data provenance, and **self-approval prohibition**:

```
 [ARTIST / INSTRUCTOR]
        │
        ▼
 (1. Submit Reference Dataset) ──► status = 'PENDING_REVIEW'
        │
        ▼
 [CULTURAL_EXPERT / ADMIN Review]
  • Checks video proof, metadata, landmark fidelity
  • Validates contributor ID != reviewer ID
        │
        ▼
 (2. Formally Approve) ──► status = 'APPROVED', approved_by = reviewer_id
        │
        ▼
 [Production Learner Availability]
```

### Immutable Versioning Rules
- **Self-Approval Prohibition**: An `ARTIST` or `INSTRUCTOR` **cannot approve their own submitted reference dataset**. Enforced via application logic and PostgreSQL `CHECK (approved_by IS NULL OR approved_by <> created_by)`.
- **Immutability of Approved Versions**: Once a `reference_movement` or `movement_version` is approved into production (`status = 'APPROVED'`), it is **strictly read-only and immutable**.
- Any modification to biomechanical rules or reference templates requires creating a **new version record** (`version_number = 'v1.1.0'`), ensuring complete historical auditability for past learner practice sessions.

---

## 11. Deterministic Non-ML Analysis Method (FastDTW + Kinematics)

> [!IMPORTANT]
> The MVP comparison method is a **deterministic computational algorithm (Kinematic Angle Calculations + FastDTW)**, NOT a trained machine learning or deep neural network model.

### Initial Experimental Dataset Classification
- The 3–5 expert reference templates used in the MVP represent an **initial experimental baseline dataset**, intended for proof-of-concept testing rather than a claim of statistical robustness across all human body types.

### Algorithm Specification
- **Pure-Python FastDTW Integration**: FastAPI utilizes standard SciPy/NumPy vector operations and Python `fastdtw` for temporal sequence alignment. (Note: `fastdtw` is a pure Python implementation of the FastDTW algorithm).
- **Static Posture (Stances)**: Calculates absolute angular deltas $\Delta \theta_k = |\theta_{learner, k} - \theta_{ref, k}|$ against expert baseline ranges.
- **Dynamic Sequence (Swings)**: Uses FastDTW path distance to align variable-speed execution trajectories across 3 phase windows (Preparation, Strike Peak, Follow-through).

---

## 12. Educational Feedback Principles & Non-Claims

> [!CAUTION]
> **EXPLICIT CULTURAL NON-CLAIMS**: Marabagam **shall NOT** claim that the AI system determines whether a traditional art performance is culturally "correct", "authentic", "perfect", or "traditionally valid". All feedback describes measurable biomechanical metrics relative to specific expert reference samples.

### Phrasing Compliance Table

| ❌ Prohibited Phrasing | ✅ Approved Biomechanical Phrasing |
| :--- | :--- |
| "Your Silambam performance is wrong/unauthentic." | "Your left knee flex angle is $145^\circ$. The reference stance target is $120^\circ-130^\circ$." |
| "Failed cultural authenticity test." | "Torso inclination is $16^\circ$ forward. Focus on keeping your spine vertical." |
| "Authentic master performance!" | "Stance alignment match: 88%. Excellent posture stability and stance width balance." |

---

## 13. Derived Landmark Privacy & Data Minimization

Derived pose landmark data is classified as **privacy-sensitive biometric-derived data**:

- **Transient Processing**: Keypoint JSON arrays posted to `/api/v1/practice-sessions/{id}/evaluate` are evaluated transiently in FastAPI RAM to compute metrics.
- **Minimal Persistent Retention**: Raw frame-by-frame keypoint arrays are **NOT retained** in the primary PostgreSQL database by default. The database stores only derived aggregate summary scores, stance metrics, and text feedback items (`metrics_summary_json`).
- **Strict Log Hygiene**: Raw keypoint JSON arrays, coordinate values, and image buffers are **strictly excluded from all application and web server log files**.
- **Learner Deletion Rights**: Learners retain total ownership over practice records and can permanently delete any practice session via `DELETE /api/v1/practice-sessions/{id}`.

---

## 14. Security & Authorization Architecture

- **JWT Authentication & RBAC**: Practice endpoints require valid Spring Boot JWT tokens.
- **Strict Ownership & IDOR Blocking**: Server-side checks verify `practice_session.user_id == currentUser.id`. Learners cannot access, evaluate, or delete other users' sessions.
- **Jackson Payload Guard**: Maximum 300 frames per evaluation request (approx. 10 sec at 30 FPS, payload size capped at 2 MB).
- **Rate Limiting**: Spring Boot `RateLimitingFilter` caps evaluation requests at 10 requests/minute per user.

---

## 15. Database Schema Design — Migration `V7__movement_coach.sql`

The non-destructive Flyway migration script `V7__movement_coach.sql` introduces 5 entities with strict database constraints:

```sql
-- Migration: V7__movement_coach.sql
-- Description: Database schema for Phase 7 AI Movement Coach Module

-- 1. Movements Catalog Table
CREATE TABLE movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    art_form_id UUID NOT NULL REFERENCES art_forms(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL UNIQUE,
    name_en VARCHAR(255) NOT NULL,
    name_ta VARCHAR(255) NOT NULL,
    description_en TEXT,
    description_ta TEXT,
    category VARCHAR(50) NOT NULL, -- STANCE, STRIKE, SWING, FOOTWORK
    difficulty_level VARCHAR(50) NOT NULL DEFAULT 'BEGINNER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Movement Versions Table (Immutable Versioning)
CREATE TABLE movement_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    movement_id UUID NOT NULL REFERENCES movements(id) ON DELETE CASCADE,
    version_number VARCHAR(20) NOT NULL,
    joint_rules_json TEXT NOT NULL, -- Configurable baseline rules & joint targets
    is_current BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(movement_id, version_number)
);

-- Ensure Unique Current Version Per Movement
CREATE UNIQUE INDEX idx_current_movement_version 
ON movement_versions (movement_id) 
WHERE is_current = true;

-- 3. Expert Reference Movements Table
CREATE TABLE reference_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    movement_id UUID NOT NULL REFERENCES movements(id) ON DELETE CASCADE,
    version_id UUID NOT NULL REFERENCES movement_versions(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    performer_attribution VARCHAR(255) NOT NULL,
    keypoints_template_json TEXT NOT NULL, -- Expert time-series landmarks
    demonstration_video_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- DRAFT, PENDING_REVIEW, APPROVED, ARCHIVED
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Self-Approval Prohibition Constraint
    CONSTRAINT chk_no_self_approval CHECK (approved_by IS NULL OR approved_by <> created_by)
);

-- 4. Learner Practice Sessions Table
CREATE TABLE practice_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movement_id UUID NOT NULL REFERENCES movements(id) ON DELETE CASCADE,
    version_id UUID NOT NULL REFERENCES movement_versions(id) ON DELETE RESTRICT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, FAILED, ABORTED
    overall_score DOUBLE PRECISION,
    duration_seconds INTEGER,
    metrics_summary_json TEXT, -- Derived metrics summary (No raw video)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Score Constraint
    CONSTRAINT chk_overall_score CHECK (overall_score IS NULL OR (overall_score >= 0.0 AND overall_score <= 100.0))
);

-- 5. Detailed Practice Feedback Table
CREATE TABLE practice_feedbacks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    practice_session_id UUID NOT NULL REFERENCES practice_sessions(id) ON DELETE CASCADE,
    body_part VARCHAR(100) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    measured_value DOUBLE PRECISION NOT NULL,
    target_range_min DOUBLE PRECISION NOT NULL,
    target_range_max DOUBLE PRECISION NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    feedback_text_en TEXT NOT NULL,
    feedback_text_ta TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO', -- INFO, WARNING, CRITICAL
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Feedback Score Constraint
    CONSTRAINT chk_feedback_score CHECK (score >= 0.0 AND score <= 100.0)
);

-- Indexes for Performance & Ownership Lookups
CREATE INDEX idx_movements_art_form ON movements(art_form_id);
CREATE INDEX idx_reference_movements_movement ON reference_movements(movement_id, status);
CREATE INDEX idx_practice_sessions_user ON practice_sessions(user_id, created_at DESC);
CREATE INDEX idx_practice_sessions_movement ON practice_sessions(movement_id);
CREATE INDEX idx_practice_feedbacks_session ON practice_feedbacks(practice_session_id);
```

### Application-Level Entity Relationship & Version Integrity Checks
Spring Boot enforces multi-table relationship validation before persisting sessions or references:
- `reference_movements.version_id` MUST point to a `movement_versions` record whose `movement_id` matches `reference_movements.movement_id`.
- `practice_sessions.version_id` MUST point to a `movement_versions` record whose `movement_id` matches `practice_sessions.movement_id`.

---

## 16. API Specification (`/api/v1/`)

### Learner APIs (Spring Boot Gateway)
- `GET /api/v1/movements` — Catalog of practice movements.
- `GET /api/v1/movements/{id}` — Movement details & approved reference video metadata.
- `POST /api/v1/practice-sessions` — Start practice session.
- `POST /api/v1/practice-sessions/{id}/evaluate` — Submit derived landmark JSON for analysis.
- `GET /api/v1/practice-sessions` — Learner practice session history.
- `GET /api/v1/practice-sessions/{id}` — Detailed session feedback breakdown.
- `DELETE /api/v1/practice-sessions/{id}` — Permanently delete practice session record.

### Expert & Admin APIs
- `POST /api/v1/admin/movements` — Create/edit movement catalog (`CULTURAL_EXPERT`, `ADMIN`).
- `POST /api/v1/admin/movements/{id}/references` — Submit candidate reference template (`ARTIST`, `INSTRUCTOR`, `CULTURAL_EXPERT`, `ADMIN`).
- `PUT /api/v1/admin/references/{id}/approve` — Formally approve reference movement (`CULTURAL_EXPERT`, `ADMIN`). Checks `approved_by != created_by`.

---

## 17. Frontend Feature Flags & Runtime Configuration

- **Vite Build-Time Feature Flags**: `VITE_ENABLE_MOVEMENT_COACH=true` controls code inclusion during `npm run build`.
- **Runtime Configuration Mechanism**: To allow enabling/disabling the feature in production without rebuilding Docker images, frontend inspects a runtime config object (`window.__APP_CONFIG__.ENABLE_MOVEMENT_COACH`) loaded from `/config.js` or backend API.

---

## 18. Measurable Engineering Benchmark Targets

All performance targets are defined under explicit benchmark conditions:

| Metric | Target Benchmark | Test Hardware / Environment Conditions |
| :--- | :--- | :--- |
| **Desktop Client FPS** | $\ge 30 \text{ FPS}$ ($\le 33.3 \text{ ms/frame}$) | Intel i5 (11th Gen) / Apple M1, Chrome 120+, 1080p WebCam, single learner. |
| **Mobile Client FPS** | $\ge 15 \text{ FPS}$ ($\le 66.6 \text{ ms/frame}$) | Snapdragon 778G / iPhone 12, Safari Mobile / Chrome Mobile, 720p WebCam. |
| **FastAPI Inference Latency** | $\le 250 \text{ ms}$ | 150-frame sequence (5-second practice), FastAPI running on 2 vCPU cores / 4GB RAM. |
| **End-to-End API Roundtrip** | $\le 500 \text{ ms}$ | Spring Boot $\rightarrow$ FastAPI $\rightarrow$ PostgreSQL total execution time. |

---

## 19. Failure Scenarios & Graceful Fallbacks

| Failure Scenario | Detection Method | System Response & Fallback |
| :--- | :--- | :--- |
| **Camera Denied** | Browser exception | Display friendly UI card detailing how to grant camera permissions. |
| **Frame Jitter / Low Lighting** | Keypoint confidence $c_i < 0.6$ | Display warning: *"Low visibility. Ensure bright lighting and step back."* |
| **Multiple People in Frame** | Rapid landmark bounding box jitter | Display guidance: *"Frame unsteady. Ensure only one person is in front of camera."* |
| **FastAPI Offline** | Spring Boot HTTP client timeout / 503 | Present local stance summary with warning: *"Detailed AI analysis temporarily offline."* |

---

## 20. Safe Non-Destructive Rollback Strategy

> [!WARNING]
> Production Flyway migrations **must never use destructive `DROP TABLE` statements** as routine rollback mechanisms.

### Production Rollback Protocol
1. **Application-Level Deactivation**: Toggle `ENABLE_MOVEMENT_COACH=false` in application config to instantly isolate frontend and backend endpoints.
2. **Forward Migration Strategy**: If database schema changes are required, deploy a safe **forward migration** (`V8__deprecate_movement_coach.sql`) that safely deprecates or alters structures without data destruction.
3. **Database Restore**: Rely on standard PostgreSQL point-in-time recovery (PITR) and database backup snapshots for disaster recovery.

---

## 21. Categorized Acceptance Criteria

The acceptance criteria explicitly separate functional, technical, expert, performance, and cultural boundaries:

### A. Functional Correctness Criteria
1. Practice sessions correctly transition through `IN_PROGRESS` $\rightarrow$ `COMPLETED`.
2. Learner data isolation is 100% enforced; IDOR attempts return `403 Forbidden` or `404 Not Found`.
3. Privacy deletion endpoint permanently removes selected practice session records.

### B. Computer-Vision & Measurement Accuracy Criteria
1. MediaPipe coordinates are correctly normalized to mid-hip origin and torso scale.
2. Exact joint angles (knees, elbows, spine) are computed within $\pm 1.0^\circ$ of geometric ground truth.
3. FastDTW sequence alignment correctly aligns variable-speed movement trajectories.

### C. Expert Validation & Governance Criteria
1. `ARTIST` or `INSTRUCTOR` role can submit candidate reference movements (`status = 'PENDING_REVIEW'`).
2. Candidate references cannot be approved by their creator (`approved_by != created_by`).
3. Approved reference versions are strictly immutable.

### D. Performance Benchmark Criteria
1. In-browser pose estimation achieves $\ge 30 \text{ FPS}$ on desktop and $\ge 15 \text{ FPS}$ on target mobile hardware.
2. Post-practice FastAPI analysis endpoint executes within $\le 250 \text{ ms}$ for 150-frame sequences.

### E. Cultural Boundaries Criteria
1. UI text, feedback cards, and system documentation **strictly contain zero claims** of evaluating "cultural authenticity", "perfection", or "traditionally correct performance".
2. All feedback is framed strictly as measurable biomechanical similarity relative to expert reference samples.

---

## 22. Implementation Sequence (For Future Execution)

1. **Step 1 — Migration**: Implement non-destructive `V7__movement_coach.sql` migration script.
2. **Step 2 — FastAPI Engine**: Build `app/movement` package in `ai-service` for kinematics and FastDTW.
3. **Step 3 — Backend Gateway**: Build `com.marabagam.movement` package in Spring Boot with security & validation.
4. **Step 4 — Frontend Client**: Implement `@mediapipe/pose` hook, local visual guidance overlay, and camera consent modal.
5. **Step 5 — Frontend Pages**: Implement `/movement-coach` catalog, practice workspace, and evaluation dashboard.
6. **Step 6 — Test Verification**: Add comprehensive unit, integration, and regression tests.

---

## 23. Final Implementation Checklist (Post-Approval)

- [ ] Apply `V7__movement_coach.sql` Flyway schema migration.
- [ ] Implement FastAPI kinematics and FastDTW evaluation package.
- [ ] Implement Spring Boot domain, services, and REST APIs.
- [ ] Implement React MediaPipe Pose local guidance component.
- [ ] Add bilingual translation strings for English and Tamil.
- [ ] Execute full unit, integration, and security test suites.
- [ ] Verify zero regression against Phase 1–6 capabilities.

---
