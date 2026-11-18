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

-- SEED BASELINE BADGES
INSERT INTO badges (id, code, name_en, name_ta, description_en, description_ta, icon_url, category)
VALUES
('d1000000-0000-0000-0000-000000000001', 'FIRST_STEP', 'First Step', 'முதல் படி', 'Completed your first traditional art lesson.', 'உங்கள் முதல் பாரம்பரியப் பாடத்தை நிறைவு செய்துள்ளீர்கள்.', 'https://assets.marabagam.org/badges/first-step.png', 'LEARNING'),
('d1000000-0000-0000-0000-000000000002', 'FIRST_QUIZ_PASS', 'First Victory', 'முதல் வெற்றி', 'Passed your first heritage knowledge quiz.', 'உங்கள் முதல் பாரம்பரிய வினாடி வினாவில் தேர்ச்சி பெற்றீர்கள்.', 'https://assets.marabagam.org/badges/first-quiz.png', 'ASSESSMENT'),
('d1000000-0000-0000-0000-000000000003', 'PERFECT_SCORE', 'Flawless Knowledge', 'பிழையற்ற அறிவு', 'Scored 100% on a heritage quiz.', 'வினாடி வினாவில் 100% மதிப்பெண் பெற்றீர்கள்.', 'https://assets.marabagam.org/badges/perfect-score.png', 'ASSESSMENT'),
('d1000000-0000-0000-0000-000000000004', 'SILAMBAM_WARRIOR', 'Silambam Warrior', 'சிலம்ப வீரன்', 'Completed Silambam Foundations course and posture evaluations.', 'சிலம்பம் அடிப்படைப் படிப்பை வெற்றிகரமாக நிறைவு செய்தீர்கள்.', 'https://assets.marabagam.org/badges/silambam-warrior.png', 'HERITAGE'),
('d1000000-0000-0000-0000-000000000005', 'WEEKLY_STREAK_7', 'Weekly Dedicated', 'வாராந்திர உழைப்பாளி', 'Maintained a 7-day active learning streak.', 'தொடர்ந்து 7 நாட்கள் கற்றல் பயிற்சியில் ஈடுபட்டீர்கள்.', 'https://assets.marabagam.org/badges/streak-7.png', 'STREAK'),
('d1000000-0000-0000-0000-000000000006', 'HERITAGE_GUARDIAN_3', 'Heritage Guardian', 'பாரம்பரியக் காவலர்', 'Completed 3 complete Tamil traditional art courses.', '3 முழு பாரம்பரியக் கலைப் படிப்புகளை நிறைவு செய்தீர்கள்.', 'https://assets.marabagam.org/badges/guardian.png', 'COMPLETION')
ON CONFLICT (code) DO NOTHING;

-- SEED BASELINE SAMPLE QUIZ FOR SILAMBAM LESSON 1
INSERT INTO quizzes (id, scope, course_id, module_id, lesson_id, title, title_ta, description, description_ta, pass_percentage, max_attempts, time_limit_minutes, is_mandatory, status, created_by)
SELECT
    'f1000000-0000-0000-0000-000000000001',
    'LESSON',
    NULL,
    NULL,
    'a1000000-0000-0000-0000-000000000001', -- Lesson 1
    'Silambam Heritage Foundation Quiz',
    'சிலம்பப் பாரம்பரிய அடிப்படை வினாடி வினா',
    'Test your knowledge of ancient Sangam texts and traditional staff fencing principles.',
    'சங்க இலக்கிய நூல்கள் மற்றும் சிலம்பக் கலை மரபுகள் குறித்த உங்கள் அறிவைச் சோதிக்கவும்.',
    80.0,
    3,
    15,
    true,
    'PUBLISHED',
    id
FROM users WHERE role = 'ADMIN' LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO quiz_questions (id, quiz_id, question_text, question_text_ta, question_type, points, display_order)
VALUES
(
    'e1000000-0000-0000-0000-000000000001',
    'f1000000-0000-0000-0000-000000000001',
    'Which ancient Tamil Sangam literary work mentions staff martial arts and Silambam heritage?',
    'சிலம்பம் மற்றும் தற்காப்பு மரபுகளைக் குறிப்பிடும் பண்டைய தமிழ் சங்க இலக்கிய நூல் எது?',
    'MULTIPLE_CHOICE',
    10,
    1
),
(
    'e1000000-0000-0000-0000-000000000002',
    'f1000000-0000-0000-0000-000000000001',
    'Traditional Silambam practice begins with offering respect to the staff (Silambam Kambu).',
    'பாரம்பரிய சிலம்பப் பயிற்சி பிரம்புத் தடிக்கு மரியாதை செலுத்துவதுடன் தொடங்குகிறது.',
    'TRUE_FALSE',
    10,
    2
)
ON CONFLICT DO NOTHING;

INSERT INTO question_options (id, question_id, option_text, option_text_ta, is_correct, explanation_en, explanation_ta, display_order)
VALUES
(
    'c1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000001',
    'Silappatikaram',
    'சிலப்பதிகாரம்',
    true,
    'Silappatikaram explicitly describes martial staff combat techniques in ancient Tamilakam.',
    'பண்டைய தமிழகத்தில் சிலம்ப தற்காப்பு கலைக் காட்சிகளை சிலப்பதிகாரம் விவரிக்கிறது.',
    1
),
(
    'c1000000-0000-0000-0000-000000000002',
    'e1000000-0000-0000-0000-000000000001',
    'Tirukkural',
    'திருக்குறள்',
    false,
    'Tirukkural is an ethical philosophical treatise, not specifically focused on staff combat.',
    'திருக்குறள் அறநெறி நூல், தற்காப்புக் கலை விவரிப்பு அல்ல.',
    2
),
(
    'c1000000-0000-0000-0000-000000000003',
    'e1000000-0000-0000-0000-000000000001',
    'Manimekalai',
    'மணிமேகலை',
    false,
    'Manimekalai focuses on Buddhist philosophy and epic storytelling.',
    'மணிமேகலை பௌத்த தத்துவக் காப்பியமாகும்.',
    3
),
(
    'c1000000-0000-0000-0000-000000000004',
    'e1000000-0000-0000-0000-000000000001',
    'Kambaramayanam',
    'கம்பராமாயணம்',
    false,
    'Kambaramayanam is a epic poem written in a later medieval period.',
    'கம்பராமாயணம் பிற்கால இதிகாசக் காப்பியமாகும்.',
    4
),
(
    'c1000000-0000-0000-0000-000000000005',
    'e1000000-0000-0000-0000-000000000002',
    'True',
    'உண்மை',
    true,
    'Traditional masters emphasize discipline, spiritual reverence, and respect for the Kambu before training.',
    'பயிற்சிக்கு முன் தடிக்கும் ஆசிரியருக்கும் மரியாதை செலுத்துவது மரபு.',
    1
),
(
    'c1000000-0000-0000-0000-000000000006',
    'e1000000-0000-0000-0000-000000000002',
    'False',
    'தவறு',
    false,
    'Reverence for the staff is a non-negotiable cultural protocol in traditional Tamil martial arts.',
    'தடி வழிபாடு தமிழ் தற்காப்புக் கலையின் இன்றியமையாத பகுதியாகும்.',
    2
)
ON CONFLICT DO NOTHING;
