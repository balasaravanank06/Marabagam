# Marabagam Phase 8 Implementation Plan
## Quizzes, Gamification & Certificates

**Phase**: Phase 8 — Technical Specification & Architecture Plan  
**Status**: **PROPOSED — PENDING FINAL APPROVAL**  
**Date**: September 8, 2026  
**Repository Target**: `/home/bala/Marabagam`  
**Dependencies**: Phase 1 (Foundation), Phase 2 (Auth/RBAC), Phase 3 (Art Forms), Phase 4 (E-Learning & Courses), Phase 5 (AI Guide/RAG), Phase 6 (Regional Mapping), Phase 7 (AI Movement Coach).  

---

> [!IMPORTANT]  
> **PLANNING ONLY**. No Java, React, Python source code modifications or Flyway database migrations have been executed.

---

## 1. EXECUTIVE SUMMARY & ARCHITECTURAL SCOPE

Phase 8 introduces an integrated educational assessment, gamification, and verifiable certification layer to the Marabagam Tamil traditional digital heritage preservation platform.

The end-to-end learning lifecycle established in Phase 8 follows a strict, server-verified pipeline:
```
  [Phase 4 / 7 Learning] 
           │
           ▼
     [Quiz System] ──(Learner Submit)──► [Server-Side Scoring Engine]
                                                  │
                ┌─────────────────────────────────┴─────────────────────────────────┐
                ▼                                                                   ▼
    [Idempotent XP Engine]                                                [Quiz Attempt Lifecycle]
     (XP, Levels, Streaks)                                                (PASSED / FAILED Record)
                │                                                                   │
                ▼                                                                   ▼
  [Badge Award Engine]                                                    [Course Completion Check]
  (Rule-Based Achievements)                                              (All Lessons + Quizzes Passed)
                │                                                                   │
                └─────────────────────────────────┬─────────────────────────────────┘
                                                  ▼
                                     [Certificate Engine]
                                 (Cryptographic HMAC-SHA256)
                                                  │
                                                  ▼
                                    [Public Verification API]
```

### Critical Architectural Guarantees
1. **Zero Client-Side Trust**: All quiz scores, XP allocations, level progressions, badge triggers, streak counts, and certificate issuance logic are executed **100% server-side**. Correct quiz answer keys are never transmitted to client browsers before or during an active attempt.
2. **Cryptographic Tamper-Resistance**: Certificates utilize cryptographically secure random identifier generation (`MBG-CERT-YYYY-<CSPRNG_12_HEX>`) and a canonical HMAC-SHA256 signature payload. Verification uses constant-time string comparison (`MessageDigest.isEqual`) to prevent timing attacks.
3. **Idempotency & Concurrency Safety**: XP transactions, badge awards, streak updates, and certificate issuances enforce strict database `UNIQUE` constraints and atomic transaction isolation to prevent race conditions, replay abuse, or duplicate rewards.
4. **Backward Compatibility**: Fully integrates with Phase 4 course, module, lesson, enrollment, and progress infrastructure without altering existing database schemas or breaking existing course completion history.
5. **Strict PII Shielding**: Public verification and leaderboard endpoints expose only the minimum necessary information (`displayName`, `courseTitle`, `issueDate`, `status`). Internal database UUIDs, email addresses, and private learning histories are strictly shielded.

---

## 2. EXISTING ARCHITECTURE REUSE & INTEGRATION

Phase 8 builds directly upon existing core abstractions established in Phases 1–7:
- **Authentication & RBAC**: Reuses Spring Security JWT filter and user roles (`GUEST`, `LEARNER`, `ARTIST`, `INSTRUCTOR`, `CULTURAL_EXPERT`, `ADMIN`).
- **Phase 4 Course Infrastructure**: Quizzes map directly to `courses`, `course_modules`, or `lessons` via foreign keys (`course_id`, `module_id`, `lesson_id`).
- **Phase 3 & 4 Media Subsystem**: Image identification quiz questions reference existing `media` metadata records (`media_id` / `url`) rather than storing raw binary data in PostgreSQL.
- **Bilingual Infrastructure**: All quiz text, option choices, explanations, badge names, achievement criteria, and certificate titles support dual-language representation (`en` / `ta`).
- **Audit Subsystem**: Hooks into Spring AOP `AuditAspect` to log security-sensitive events (`QUIZ_SUBMITTED`, `XP_AWARDED`, `BADGE_AWARDED`, `CERTIFICATE_ISSUED`, `CERTIFICATE_REVOKED`).

---

## 3. QUIZ SYSTEM & ASSOCIATION MODEL

### 3.1 Unambiguous Quiz Association Model
To prevent ambiguous simultaneous associations, a quiz has **EXACTLY ONE** primary learning scope:
$$\text{QUIZ\_SCOPE} \in \{\text{'COURSE'}, \text{'MODULE'}, \text{'LESSON'}\}$$

Enforced in PostgreSQL via `CHECK` constraint `chk_quiz_scope`:
```sql
CONSTRAINT chk_quiz_scope CHECK (
    (scope = 'COURSE' AND course_id IS NOT NULL AND module_id IS NULL AND lesson_id IS NULL) OR
    (scope = 'MODULE' AND module_id IS NOT NULL AND course_id IS NULL AND lesson_id IS NULL) OR
    (scope = 'LESSON' AND lesson_id IS NOT NULL AND course_id IS NULL AND module_id IS NULL)
)
```

### 3.2 Question Types & Integrity Constraints
- **`MULTIPLE_CHOICE`**: 4 option choices per question. For Phase 8 MVP, single correct choice (`is_correct = true`) is enforced.
- **`TRUE_FALSE`**: Exactly 2 option choices ("True" / "False").
- **`IMAGE_IDENTIFICATION`**: Displays a traditional art instrument, mudra, costume, or posture image referencing an existing Phase 4 `media` record (`media_id` FK).

#### Database & Service Integrity Rules
1. **Question Parent Integrity**: Every question belongs to exactly one quiz (`quiz_id NOT NULL REFERENCES quizzes(id)`). Unique order within quiz: `UNIQUE(quiz_id, display_order)`.
2. **Option Parent Integrity**: Every option belongs to exactly one question (`question_id NOT NULL REFERENCES quiz_questions(id)`). Unique order within question: `UNIQUE(question_id, display_order)`.
3. **Positive Points**: Question points must be positive (`points > 0`).
4. **Cross-Quiz Question/Option Injection Defense**: Upon submission, the server validates that `option.question.quiz_id == requested_quiz_id`. Submitting options from another quiz throws `400 Bad Request`.

---

## 4. QUIZ ATTEMPT & SERVER-SIDE SCORING ENGINE

### 4.1 Attempt Lifecycle State Machine
```
[LEARNER POST /quizzes/{id}/start] ──► [Record Attempt: STATUS = STARTED, started_at = NOW]
                                                        │
                                                        ▼
[LEARNER POST /quizzes/attempts/{id}/submit] ─► [Check Time Limit & Grace Period]
                                                        │
                                                        ▼
                                       [Calculate Score Server-Side]
                                                        │
                                                        ▼
                                 [Check Pass Percentage (e.g. >= 80%)]
                                                        │
                         ┌──────────────────────────────┴──────────────────────────────┐
                         ▼                                                             ▼
             [STATUS = PASSED]                                             [STATUS = FAILED]
     - Award XP & Check Badges                                     - Record Attempt
     - Trigger Course Completion Check                             - Increment Attempt Count
```

### 4.2 Server-Authoritative Time Limit & Grace Period
- **Server-Controlled Deadline**: $\text{Deadline} = \text{started\_at} + \text{time\_limit\_minutes}$. Frontend timer is purely cosmetic.
- **30-Second Grace Period**: Submissions received within $\text{Deadline} + 30\text{s}$ are accepted.
- **Timeout Handling**: Submissions received after $\text{Deadline} + 30\text{s}$ are marked `status = 'TIMED_OUT'` with `score_earned = 0.0` and `passed = false`.

### 4.3 Attempt Transaction Integrity & Immutability
- **Attempt Uniqueness**: PostgreSQL enforces `CONSTRAINT uk_quiz_user_attempt UNIQUE (quiz_id, user_id, attempt_number)`.
- **State Transition Immutability**: Attempt scoring runs inside a `@Transactional` block. Once an attempt transitions to `SUBMITTED`, `PASSED`, `FAILED`, or `TIMED_OUT`, subsequent submission attempts throw `409 Conflict`.

---

## 5. GAMIFICATION ENGINE (XP, LEVELS, STREAKS, BADGES)

### 5.1 Initial XP Reward Matrix & Idempotency Rules

| Activity | XP Amount | Repeatable | Idempotency Key Structure |
| :--- | :--- | :--- | :--- |
| **Lesson Completed** | +20 XP | No (Per lesson) | `LESSON_COMPLETE:{user_id}:{lesson_id}` |
| **Mandatory Quiz Passed (1st Try)** | +50 XP | No (Per quiz) | `QUIZ_PASS_FIRST:{user_id}:{quiz_id}` |
| **Mandatory Quiz Passed (Retake)** | +25 XP | No (Per quiz) | `QUIZ_PASS_RETAKE:{user_id}:{quiz_id}:{attempt_id}` |
| **Perfect Quiz Score (100%)** | +30 XP Bonus | No (Per attempt) | `QUIZ_PERFECT:{user_id}:{quiz_id}:{attempt_id}` |
| **Movement Practice Completed** | +30 XP | Yes (Per session)| `MOVEMENT_PRACTICE:{user_id}:{session_id}` |
| **Course Completed** | +200 XP | No (Per course) | `COURSE_COMPLETE:{user_id}:{course_id}` |
| **7-Day Learning Streak** | +100 XP Bonus | Yes (Weekly) | `STREAK_BONUS_7:{user_id}:{date_utc}` |

*Zero XP is awarded for passive page views, logins, or API polling.*

### 5.2 Level Progression & Deterministic Title Mapping
Formula: $\text{Level}(XP) = \left\lfloor \sqrt{\frac{XP}{100}} \right\rfloor + 1$

| Level | Required XP | Title (English) | Title (Tamil) |
| :--- | :--- | :--- | :--- |
| **Level 1** | 0 XP | Novice Student | ஆரம்பநிலை மாணவர் |
| **Level 2** | 100 XP | Dedicated Practitioner | தீவிர பயிற்சிமையாளர் |
| **Level 3** | 400 XP | Heritage Explorer | பாரம்பரிய ஆய்வாளர் |
| **Level 4** | 900 XP | Skilled Artiste | கலை வல்லுநர் |
| **Level 5** | 1,600 XP | Heritage Guardian | பாரம்பரியக் காவலர் |
| **Level 10** | 8,100 XP | Senior Master | கலைப் பேரசான் |

### 5.3 UTC Learning Streaks
- **UTC Daily Boundary**: A learning day is logged in `user_streaks` using UTC calendar date (`last_activity_date DATE`).
- **Idempotent Daily Activity**: Completing multiple activities on the same UTC calendar day updates `last_activity_date` but does **NOT** artificially increment `current_streak`.
- **Streak Continuity**: If `last_activity_date == today_utc - 1`, `current_streak` increments by 1. If `last_activity_date < today_utc - 1`, `current_streak` resets to 1.

### 5.4 Backend Badge Rules

| Badge Code | Badge Title (EN / TA) | Triggering Event | Eligibility Rule | One-Time |
| :--- | :--- | :--- | :--- | :--- |
| `FIRST_STEP` | First Step / முதல் படி | `LESSON_COMPLETED` | Complete 1st lesson | Yes |
| `FIRST_QUIZ_PASS` | First Victory / முதல் வெற்றி | `QUIZ_PASSED` | Pass 1st quiz | Yes |
| `PERFECT_SCORE` | Flawless / பிழையற்ற அறிவு | `QUIZ_PASSED` | Score 100% on any quiz | Yes |
| `SILAMBAM_WARRIOR` | Silambam Warrior / சிலம்ப வீரன் | `COURSE_COMPLETED` | Complete Silambam Foundations course | Yes |
| `WEEKLY_STREAK_7` | Weekly Dedicated / வாராந்திர உழைப்பாளி | `STREAK_UPDATED` | Reach 7-day learning streak | Yes |
| `HERITAGE_GUARDIAN_3` | Heritage Guardian / பாரம்பரியக் காவலர் | `COURSE_COMPLETED` | Complete 3 full courses | Yes |

---

## 6. LEADERBOARD SYSTEM & PRIVACY CONTROL

### 6.1 Weekly & All-Time Leaderboard Calculation
- **Weekly XP Calculation**: Dynamically computed via indexed SQL aggregation over `xp_transactions`:
  $$\text{WeeklyXP}(user\_id) = \sum_{t \in \text{Transactions}} t.amount \quad \text{where } t.created\_at \ge \text{week\_start\_utc} \text{ AND } t.created\_at < \text{week\_end\_utc}$$
- **Timezone Boundary**: Strictly UTC. Week starts Monday 00:00:00 UTC and ends Sunday 23:59:59 UTC (`week_key = YYYY-Www`).
- **Tie Handling**: Learners with equal XP are ordered by earliest date achieving the score (`ORDER BY weekly_xp DESC, min(created_at) ASC`).

### 6.2 Learner Privacy & Opt-Out Model
- **User Preference Storage**: Added `leaderboard_visible BOOLEAN NOT NULL DEFAULT TRUE` to `users` table.
- **Opt-Out Behavior**: When `leaderboard_visible = false`, the user is filtered out of all public leaderboard responses.
- **Data Shielding**: Leaderboard API returns **ONLY**: `rank`, `displayName`, `level`, `totalXp`/`weeklyXp`, `badgesCount`. Emails, internal UUIDs, and learning history are strictly omitted.

---

## 7. CERTIFICATE SYSTEM & CRYPTOGRAPHIC SECURITY

### 7.1 Certificate Eligibility Semantics
A certificate is issued **AUTOMATICALLY** when all the following criteria are met:
1. Learner is actively enrolled in the course (`enrollments.status = 'ACTIVE'`).
2. All published lessons in the course have `completed = true`.
3. All mandatory published quizzes assigned to the course/modules/lessons have `passed = true`.
4. Average score across mandatory published quizzes $\ge 80\%$.
*(Optional quizzes do NOT block certification or lower the average score. For retaken quizzes, the highest valid score is used).*

### 7.2 Cryptographic HMAC-SHA256 Tamper Resistance

#### Secure Random Certificate Code
Code format: `MBG-CERT-YYYY-<CSPRNG_12_HEX>` (e.g. `MBG-CERT-2026-F89A12B3C4D5`). Generated using `java.security.SecureRandom`. Enforced `UNIQUE(certificate_code)` in DB.

#### Canonical Payload Serialization Format
The HMAC-SHA256 signature is calculated over the canonical string format:
$$\text{Payload} = \text{certificate\_code} \mid \text{user\_id} \mid \text{course\_id} \mid \text{recipient\_name\_snapshot} \mid \text{course\_title\_snapshot} \mid \text{final\_score\_percentage} \mid \text{issued\_at}$$

```
MBG-CERT-2026-F89A12B3C4D5|ea06c522-ec88-48fd-83f3-c2b65e0622a9|c1000000-0000-0000-0000-000000000001|Verification Learner|Silambam Foundations: Staff Fencing Essentials|92.5|2026-09-08T15:00:00Z
```

#### Signature Rules
- Secret Key loaded from environment variable `MARABAGAM_CERTIFICATE_SECRET` (never hard-coded or logged).
- Constant-Time Comparison: Verifying signatures uses `java.security.MessageDigest.isEqual()` to prevent timing side-channel attacks.
- Signature Immutability & Key Versioning: Signed payload includes `key_version` column in `certificates` table to support future secret key rotation.

### 7.3 Public Verification & Artifact Specification
- **Public API**: `GET /api/v1/certificates/verify/{certificateCode}` (Public, No Auth required). Returns recipient name snapshot, course title, issue date, status (`ISSUED`/`REVOKED`), and `isAuthentic` (boolean).
- **Public Base URL Configuration**: `CERTIFICATE_PUBLIC_BASE_URL` (configured via env: `http://localhost:5173` in dev, `https://marabagam.org` in prod).
- **PDF & QR Code Artifact**: Certificate UI and downloadable PDF render a QR code containing `CERTIFICATE_PUBLIC_BASE_URL + "/certificate/" + certificateCode + "/verify"`. No personal PII (emails or quiz answers) is embedded in the QR code.

---

## 8. DATABASE DESIGN (PROPOSED V8 MIGRATION SPECIFICATION)

Below is the complete DDL specification for `/home/bala/Marabagam/backend/src/main/resources/db/migration/V8__quizzes_gamification_certificates.sql`:

```sql
-- ============================================================
-- MARABAGAM DIGITAL HERITAGE PLATFORM
-- V8: Quizzes, Gamification & Certificates Schema
-- ============================================================

-- 0. Extend Users Table for Leaderboard Privacy Toggling
ALTER TABLE users ADD COLUMN IF NOT EXISTS leaderboard_visible BOOLEAN NOT NULL DEFAULT TRUE;

-- 1. Quizzes Table
CREATE TABLE IF NOT EXISTS quizzes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scope VARCHAR(50) NOT NULL CHECK (scope IN ('COURSE', 'MODULE', 'LESSON')),
    course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
    module_id UUID REFERENCES course_modules(id) ON DELETE CASCADE,
    lesson_id UUID REFERENCES lessons(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    title_ta VARCHAR(255) NOT NULL,
    description TEXT,
    description_ta TEXT,
    pass_percentage DOUBLE PRECISION NOT NULL DEFAULT 80.0 CHECK (pass_percentage >= 0.0 AND pass_percentage <= 100.0),
    max_attempts INT NOT NULL DEFAULT 3 CHECK (max_attempts >= 1),
    time_limit_minutes INT DEFAULT NULL CHECK (time_limit_minutes IS NULL OR time_limit_minutes >= 1),
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED')),
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    approved_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    archived_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_quiz_scope CHECK (
        (scope = 'COURSE' AND course_id IS NOT NULL AND module_id IS NULL AND lesson_id IS NULL) OR
        (scope = 'MODULE' AND module_id IS NOT NULL AND course_id IS NULL AND lesson_id IS NULL) OR
        (scope = 'LESSON' AND lesson_id IS NOT NULL AND course_id IS NULL AND module_id IS NULL)
    ),
    CONSTRAINT chk_quiz_no_self_approval CHECK (approved_by IS NULL OR approved_by <> created_by)
);

-- 2. Quiz Questions Table
CREATE TABLE IF NOT EXISTS quiz_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_text_ta TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL DEFAULT 'MULTIPLE_CHOICE' CHECK (question_type IN ('MULTIPLE_CHOICE', 'TRUE_FALSE', 'IMAGE_IDENTIFICATION')),
    media_id UUID REFERENCES media(id) ON DELETE SET NULL,
    points INT NOT NULL DEFAULT 10 CHECK (points >= 1),
    display_order INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_quiz_questions_order UNIQUE (quiz_id, display_order)
);

-- 3. Question Options Table
CREATE TABLE IF NOT EXISTS question_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    option_text_ta TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    explanation_en TEXT,
    explanation_ta TEXT,
    display_order INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_question_options_order UNIQUE (question_id, display_order)
);

-- 4. Quiz Attempts Table
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    attempt_number INT NOT NULL CHECK (attempt_number >= 1),
    score_earned DOUBLE PRECISION DEFAULT NULL,
    total_possible_score DOUBLE PRECISION DEFAULT NULL,
    percentage DOUBLE PRECISION DEFAULT NULL CHECK (percentage IS NULL OR (percentage >= 0.0 AND percentage <= 100.0)),
    passed BOOLEAN DEFAULT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'STARTED' CHECK (status IN ('STARTED', 'SUBMITTED', 'TIMED_OUT', 'ABANDONED')),
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_quiz_user_attempt UNIQUE (quiz_id, user_id, attempt_number)
);

-- 5. Quiz Attempt Answers Table
CREATE TABLE IF NOT EXISTS quiz_attempt_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    selected_option_id UUID REFERENCES question_options(id) ON DELETE CASCADE,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    points_awarded INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_attempt_question UNIQUE (attempt_id, question_id)
);

-- 6. XP Transactions Table (Idempotent XP Ledger)
CREATE TABLE IF NOT EXISTS xp_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INT NOT NULL CHECK (amount > 0),
    source_activity VARCHAR(50) NOT NULL CHECK (source_activity IN ('LESSON_COMPLETED', 'QUIZ_PASSED', 'PERFECT_QUIZ_BONUS', 'MOVEMENT_PRACTICE', 'COURSE_COMPLETED', 'STREAK_BONUS')),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 7. Badges Table
CREATE TABLE IF NOT EXISTS badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name_en VARCHAR(150) NOT NULL,
    name_ta VARCHAR(150) NOT NULL,
    description_en TEXT NOT NULL,
    description_ta TEXT NOT NULL,
    icon_url VARCHAR(2048) NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'ACHIEVEMENT',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 8. User Badges Table
CREATE TABLE IF NOT EXISTS user_badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_id UUID NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
    awarded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_badge UNIQUE (user_id, badge_id)
);

-- 9. User Streaks Table
CREATE TABLE IF NOT EXISTS user_streaks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    current_streak INT NOT NULL DEFAULT 0 CHECK (current_streak >= 0),
    longest_streak INT NOT NULL DEFAULT 0 CHECK (longest_streak >= 0),
    last_activity_date DATE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 10. Certificates Table
CREATE TABLE IF NOT EXISTS certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_code VARCHAR(100) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    recipient_name_snapshot VARCHAR(255) NOT NULL,
    course_title_snapshot VARCHAR(255) NOT NULL,
    course_title_ta_snapshot VARCHAR(255) NOT NULL,
    final_score_percentage DOUBLE PRECISION NOT NULL CHECK (final_score_percentage >= 0.0 AND final_score_percentage <= 100.0),
    issued_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    signature_hmac VARCHAR(255) NOT NULL,
    key_version INT NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'ISSUED' CHECK (status IN ('ISSUED', 'REVOKED')),
    revocation_reason TEXT,
    revoked_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_user_course_certificate UNIQUE (user_id, course_id)
);

-- Performance Indexes
CREATE INDEX idx_quizzes_course ON quizzes(course_id);
CREATE INDEX idx_quizzes_module ON quizzes(module_id);
CREATE INDEX idx_quizzes_lesson ON quizzes(lesson_id);
CREATE INDEX idx_quiz_questions_quiz ON quiz_questions(quiz_id);
CREATE INDEX idx_question_options_question ON question_options(question_id);
CREATE INDEX idx_quiz_attempts_user_quiz ON quiz_attempts(user_id, quiz_id);
CREATE INDEX idx_xp_transactions_user ON xp_transactions(user_id);
CREATE INDEX idx_xp_transactions_created ON xp_transactions(created_at, user_id);
CREATE INDEX idx_user_badges_user ON user_badges(user_id);
CREATE INDEX idx_certificates_code ON certificates(certificate_code);
CREATE INDEX idx_certificates_user ON certificates(user_id);
```

---

## 9. API SPECIFICATION (/api/v1/)

### 9.1 Learner Quiz & Attempt APIs
- `GET /api/v1/quizzes/{id}`: Retrieves quiz metadata & question choices (**EXCLUDES** `is_correct` and explanations). `LEARNER` role.
- `POST /api/v1/quizzes/{id}/start`: Initiates a new quiz attempt. Checks `max_attempts` limit. `LEARNER` role.
- `POST /api/v1/quizzes/attempts/{attemptId}/submit`: Submits answer choices. Server scores attempt, computes percentage, records `PASSED`/`FAILED`, allocates XP, and returns feedback. `LEARNER` role.
- `GET /api/v1/quizzes/attempts/{attemptId}`: Retrieves completed attempt results and explanations. `LEARNER` role (Owner only).

### 9.2 Gamification & Profile APIs
- `GET /api/v1/gamification/profile`: Retrieves current user's XP, level, level title, current streak, longest streak, and earned badges. `LEARNER` role.
- `GET /api/v1/gamification/leaderboard`: Returns paginated global or weekly XP leaderboard (Sanitized output: `rank`, `displayName`, `totalXp`, `level`, `badgesCount`). Public/Learner access.
- `GET /api/v1/badges`: Returns all active badge definitions and user progress. `LEARNER` role.

### 9.3 Certificate & Verification APIs
- `GET /api/v1/certificates`: Returns all issued certificates for current learner. `LEARNER` role.
- `GET /api/v1/certificates/{id}`: Returns certificate details & verification signature. `LEARNER` role (Owner only).
- `GET /api/v1/certificates/verify/{certificateCode}`: Public verification endpoint. Returns recipient name snapshot, course title, issue date, status (`ISSUED`/`REVOKED`), and HMAC verification status. **PUBLIC** (No Auth required).

### 9.4 Admin & Governance Content APIs
- `POST /api/v1/admin/quizzes`: Create draft quiz. `ARTIST`, `INSTRUCTOR`, `CULTURAL_EXPERT`, `ADMIN`.
- `PUT /api/v1/admin/quizzes/{id}/submit`: Submit quiz for review.
- `PUT /api/v1/admin/quizzes/{id}/approve`: Approve quiz (`CULTURAL_EXPERT`, `ADMIN` - Enforces `approved_by <> created_by`).
- `PUT /api/v1/admin/quizzes/{id}/publish`: Publish approved quiz.
- `DELETE /api/v1/admin/certificates/{id}/revoke`: Revoke certificate. `ADMIN` role.

---

## 10. FRONTEND UI & ROUTING PLAN

### 10.1 Routes & Views (`frontend/src/`)
- `/quizzes`: Quiz Catalog & Assigned Assessments.
- `/quiz/:id`: Active Quiz Player with timer, progress bar, accessibility radio/checkbox controls.
- `/quiz/:id/result`: Result Dashboard (Score percentage, pass/fail badge, XP earned animation, question explanations).
- `/my-achievements`: Gamification Hub (XP Progress bar, Level card, Streaks, Badge Showcase).
- `/leaderboard`: Global & Weekly Leaderboard Table with search and pagination.
- `/certificates`: Learner Certificate Gallery.
- `/certificate/:code/verify`: Public Certificate Verification Page (Renders official certificate template, QR code, HMAC verification badge, PDF download button).

### 10.2 Component Hierarchy (`frontend/src/components/`)
- `components/quiz/QuizCard.tsx`
- `components/quiz/QuizPlayer.tsx`
- `components/quiz/QuestionView.tsx`
- `components/quiz/QuizResultSummary.tsx`
- `components/gamification/XpProgressBar.tsx`
- `components/gamification/LevelBadge.tsx`
- `components/gamification/StreakWidget.tsx`
- `components/gamification/BadgeCard.tsx`
- `components/gamification/LeaderboardTable.tsx`
- `components/certificate/CertificateCard.tsx`
- `components/certificate/CertificateTemplate.tsx`
- `components/certificate/QrCodeWidget.tsx`

---

## 11. EXHAUSTIVE SECURITY REVIEW & THREAT MATRIX

| Threat Vector | Severity | Vulnerability Mechanism | Server-Side Defense & Mitigation |
| :--- | :--- | :--- | :--- |
| **Answer Key Scraping** | CRITICAL | Client inspects network response | DTO projections explicitly exclude `is_correct` and explanations during active attempts. |
| **Score Manipulation** | CRITICAL | Client modifies score in payload | Scores calculated **100% server-side** in Spring Boot service; client submits option IDs only. |
| **IDOR Attack** | HIGH | User A submits answers for User B | Attempt queries validate `WHERE id = :attemptId AND user_id = :currentUserId`. |
| **XP Farming & Replays** | HIGH | Replaying submission API calls | `xp_transactions` uses UNIQUE `idempotency_key` constraint (`QUIZ_PASS_FIRST:{user_id}:{quiz_id}`). Duplicate inserts are safely ignored. |
| **Concurrent Submissions** | HIGH | Clicking submit twice simultaneously | Database `UNIQUE(quiz_id, user_id, attempt_number)` and atomic `@Transactional` state transitions (`STARTED -> SUBMITTED`). |
| **Certificate Forgery** | CRITICAL | Editing DOM/PDF to fake certificate | Canonical payload signed with HMAC-SHA256; verified against DB and constant-time string comparison (`MessageDigest.isEqual`). |
| **Leaderboard PII Exposure**| MEDIUM | Scraping emails/UUIDs from rankings | Leaderboard API returns sanitized projection (`rank`, `displayName`, `level`, `totalXp`). Emails and UUIDs omitted. |
| **Cross-Quiz Injection** | HIGH | Submitting options from Quiz B to Quiz A | Server verifies `option.question.quiz_id == requested_quiz_id` before processing. |
| **Contributor Self-Approval**| HIGH | Creator approving their own quiz | DB constraint `chk_quiz_no_self_approval` and service check enforce `approved_by <> created_by`. |

---

## 12. CONCURRENCY & TRANSACTION STRATEGY

### Concurrency Acceptance Test Scenarios
1. **Simultaneous Double-Submit**: Submitting two requests for the same active attempt ID concurrently results in exactly **one** successful evaluation; the second request receives `409 Conflict`.
2. **Concurrent XP Triggers**: Firing 2 completion events for the same lesson concurrently results in exactly **one** `xp_transactions` row inserted (+20 XP), preventing double XP.
3. **Simultaneous Certificate Generation**: Two concurrent completion checks for the same user and course execute inside a `@Transactional` block with `uk_user_course_certificate` constraint, resulting in exactly **one** certificate issued.

---

## 13. TEST STRATEGY & REGRESSION PLAN

### 13.1 Backend Tests (`backend/src/test/java/com/marabagam/`)
- `QuizControllerTest.java`: Validate public/learner/admin endpoint permissions, IDOR defense, and 401/403 handling.
- `QuizServiceTest.java`: Verify server-side score calculation, attempt limit checks, and time limit timeouts.
- `GamificationServiceTest.java`: Test XP idempotency, level threshold math, and streak increment/reset rules.
- `CertificateServiceTest.java`: Test automatic issue eligibility check, HMAC-SHA256 signature verification, and revocation logic.

### 13.2 Microservice Tests (`ai-service/tests/`)
- Confirm existing Phase 5 & Phase 7 Pytest suites remain 100% passing.

### 13.3 Frontend Tests (`frontend/src/__tests__/`)
- `QuizSystem.test.tsx`: Test quiz player rendering, timer countdown, submission, and result screen display.
- `Gamification.test.tsx`: Test XP progress bar, badge grid, and leaderboard rendering.
- `CertificateVerification.test.tsx`: Test certificate view, public verification lookup, and QR rendering.
- **Regression**: Ensure all existing Vitest test files (`AuthFlow`, `ArtFormDirectory`, `CourseDirectory`, `AiCulturalGuide`, `HeritageMap`, `MovementCoach`) remain 100% passing.

---

## 14. OBJECTIVE ACCEPTANCE CRITERIA

### A. Functional Correctness
- [ ] Learners can take published quizzes assigned to lessons, modules, or courses.
- [ ] Answers are scored accurately server-side with instant bilingual feedback.
- [ ] XP, levels, badges, and streaks update automatically upon activity.
- [ ] Certificates issue automatically when all course completion criteria are satisfied.
- [ ] Public verification URL successfully validates authentic certificates.

### B. Security & Privacy
- [ ] Question answer keys (`is_correct`) are never leaked in client-facing quiz DTOs.
- [ ] Cross-user access (IDOR) to quiz attempts or private certificates is blocked (`404`/`403`).
- [ ] Duplicate XP awards or attempt replays are prevented by database idempotency keys.
- [ ] Self-approval of quizzes by contributors is blocked (`approved_by <> created_by`).
- [ ] Leaderboard API exposes zero sensitive user PII (emails or internal UUIDs).

### C. Accessibility & Localization
- [ ] Quiz player controls support 100% keyboard navigation and visible focus rings.
- [ ] All quiz, gamification, and certificate UI elements support bilingual English and Tamil text.

---

## 15. SAFE IMPLEMENTATION SEQUENCE

```
Step 1: Create Flyway migration V8__quizzes_gamification_certificates.sql
Step 2: Implement Quiz JPA Entities, Repositories, and DTOs
Step 3: Implement Quiz Scoring Engine & Attempt Service
Step 4: Implement Quiz REST Controllers (Learner & Admin Governance)
Step 5: Implement Gamification XP Ledger & Level Service
Step 6: Implement Badge Evaluator & Streak Tracker
Step 7: Implement Certificate Engine & Cryptographic HMAC Signer
Step 8: Implement Public Certificate Verification API
Step 9: Implement Frontend Quiz Player & Result Components
Step 10: Implement Frontend Gamification Hub & Leaderboard Views
Step 11: Implement Frontend Certificate Gallery & Public Verification Page
Step 12: Write Backend & Frontend Unit/Integration Test Suites
Step 13: Execute Full Regression Suite (Phases 1–7)
Step 14: Update Docker & Deploy Services for Validation
Step 15: Generate Final Phase 8 Report
```

---

## 16. EXPECTED FILES TO BE CREATED / MODIFIED

### Backend (`backend/`)
- `[NEW]` `src/main/resources/db/migration/V8__quizzes_gamification_certificates.sql`
- `[NEW]` `src/main/java/com/marabagam/quiz/domain/*` (Quiz, QuizQuestion, QuestionOption, QuizAttempt, QuizAttemptAnswer)
- `[NEW]` `src/main/java/com/marabagam/gamification/domain/*` (XpTransaction, Badge, UserBadge, UserStreak)
- `[NEW]` `src/main/java/com/marabagam/certificate/domain/Certificate.java`
- `[NEW]` `src/main/java/com/marabagam/quiz/repository/*`
- `[NEW]` `src/main/java/com/marabagam/gamification/repository/*`
- `[NEW]` `src/main/java/com/marabagam/certificate/repository/CertificateRepository.java`
- `[NEW]` `src/main/java/com/marabagam/quiz/service/*`
- `[NEW]` `src/main/java/com/marabagam/gamification/service/*`
- `[NEW]` `src/main/java/com/marabagam/certificate/service/CertificateService.java`
- `[NEW]` `src/main/java/com/marabagam/quiz/controller/*`
- `[NEW]` `src/main/java/com/marabagam/gamification/controller/*`
- `[NEW]` `src/main/java/com/marabagam/certificate/controller/*`
- `[NEW]` `src/test/java/com/marabagam/quiz/*`
- `[NEW]` `src/test/java/com/marabagam/gamification/*`
- `[NEW]` `src/test/java/com/marabagam/certificate/*`

### Frontend (`frontend/`)
- `[NEW]` `src/types/quiz.ts`, `src/types/gamification.ts`, `src/types/certificate.ts`
- `[NEW]` `src/services/quizService.ts`, `src/services/gamificationService.ts`, `src/services/certificateService.ts`
- `[NEW]` `src/components/quiz/*`
- `[NEW]` `src/components/gamification/*`
- `[NEW]` `src/components/certificate/*`
- `[NEW]` `src/pages/QuizCatalogPage.tsx`, `src/pages/QuizPlayerPage.tsx`, `src/pages/AchievementsPage.tsx`, `src/pages/LeaderboardPage.tsx`, `src/pages/CertificatesPage.tsx`, `src/pages/CertificateVerifyPage.tsx`
- `[NEW]` `src/__tests__/QuizSystem.test.tsx`, `src/__tests__/Gamification.test.tsx`, `src/__tests__/CertificateVerification.test.tsx`
- `[MODIFY]` `src/app/App.tsx` (Route registration)
- `[MODIFY]` `src/locales/en.json` & `src/locales/ta.json`

---

## 17. REMAINING DECISIONS REQUIRING HUMAN APPROVAL

1. **Certificate Secret Key Management**: The plan specifies loading `MARABAGAM_CERTIFICATE_SECRET` from environment/secret manager. In local development Docker setup, a default development secret is configured in `docker-compose.yml`.
2. **Weekly Leaderboard Start Day**: The plan adopts UTC Monday 00:00:00 as the weekly boundary.
