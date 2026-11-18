-- ============================================================
-- MARABAGAM DIGITAL HERITAGE PLATFORM
-- V4: Multimedia Content & Structured E-Learning Foundation Schema
-- ============================================================

-- Courses Table
CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    art_form_id UUID NOT NULL REFERENCES art_forms(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    title_ta VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    short_description VARCHAR(500) NOT NULL,
    short_description_ta VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    description_ta TEXT NOT NULL,
    difficulty VARCHAR(50) NOT NULL DEFAULT 'BEGINNER' CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    estimated_duration_minutes INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED')),
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Course Modules Table
CREATE TABLE IF NOT EXISTS course_modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    title_ta VARCHAR(255) NOT NULL,
    description TEXT,
    description_ta TEXT,
    display_order INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_course_modules_display_order UNIQUE(course_id, display_order)
);

-- Lessons Table
CREATE TABLE IF NOT EXISTS lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id UUID NOT NULL REFERENCES course_modules(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    title_ta VARCHAR(255) NOT NULL,
    description TEXT,
    description_ta TEXT,
    content TEXT,
    content_ta TEXT,
    lesson_type VARCHAR(50) NOT NULL DEFAULT 'TEXT' CHECK (lesson_type IN ('TEXT', 'VIDEO', 'AUDIO', 'IMAGE', 'DOCUMENT', 'MIXED')),
    estimated_duration_minutes INT NOT NULL DEFAULT 0,
    display_order INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_by UUID REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_lessons_display_order UNIQUE(module_id, display_order)
);

-- Media Metadata Table
CREATE TABLE IF NOT EXISTS media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID REFERENCES lessons(id) ON DELETE CASCADE,
    media_type VARCHAR(50) NOT NULL CHECK (media_type IN ('VIDEO', 'AUDIO', 'IMAGE', 'DOCUMENT')),
    title VARCHAR(255) NOT NULL,
    title_ta VARCHAR(255),
    url VARCHAR(2048) NOT NULL,
    thumbnail_url VARCHAR(2048),
    mime_type VARCHAR(100),
    file_size_bytes BIGINT,
    duration_seconds INT,
    display_order INT NOT NULL DEFAULT 1,
    alt_text VARCHAR(500),
    alt_text_ta VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Enrollments Table
CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT uk_enrollments_user_course UNIQUE(user_id, course_id)
);

-- Lesson Progress Table
CREATE TABLE IF NOT EXISTS lesson_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    progress_percent INT NOT NULL DEFAULT 0 CHECK (progress_percent >= 0 AND progress_percent <= 100),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    last_accessed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_lesson_progress_user_lesson UNIQUE(user_id, lesson_id)
);

-- Indexes for Query Performance
CREATE INDEX IF NOT EXISTS idx_courses_art_form_id ON courses(art_form_id);
CREATE INDEX IF NOT EXISTS idx_courses_status ON courses(status);
CREATE INDEX IF NOT EXISTS idx_courses_slug ON courses(slug);
CREATE INDEX IF NOT EXISTS idx_courses_created_by ON courses(created_by);
CREATE INDEX IF NOT EXISTS idx_course_modules_course_id ON course_modules(course_id);
CREATE INDEX IF NOT EXISTS idx_lessons_module_id ON lessons(module_id);
CREATE INDEX IF NOT EXISTS idx_lessons_status ON lessons(status);
CREATE INDEX IF NOT EXISTS idx_media_lesson_id ON media(lesson_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_user_id ON enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_user_id ON lesson_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_lesson_id ON lesson_progress(lesson_id);


-- ============================================================
-- SEED REPRESENTATIVE PUBLISHED COURSE DATA
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, role, account_status)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@marabagam.org',
    'admin_seed',
    '$2a$12$K1r6K/40k4yHq.b5YqD/4.7E9G7G0N/pY0z0g0e0r0o0u0t0i0n0e',
    'Marabagam Admin',
    'ADMIN',
    'ACTIVE'
) ON CONFLICT (email) DO NOTHING;

INSERT INTO courses (
    id, art_form_id, title, title_ta, slug, short_description, short_description_ta,
    description, description_ta, difficulty, estimated_duration_minutes, status, created_by
) SELECT
    'c1000000-0000-0000-0000-000000000001',
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', -- Silambam
    'Silambam Foundations: Staff Fencing Essentials',
    'சிலம்பம் அடிப்படை: தடி வரிசைக் கலை',
    'silambam-foundations',
    'Learn fundamental footwork (Kalvarisai), staff grip techniques, and core defensive spins.',
    'அடிப்படை கால்வரிசை, தடி பிடிக்கும் முறைகள் மற்றும் சுழற்றல் நுட்பங்களை கற்றுக்கொள்ளுங்கள்.',
    'Comprehensive introductory course covering the sacred heritage of Silambam, grip techniques, stance stability, basic spins (Kamai), and footwork coordination.',
    'சிலம்பத்தின் புனிதமான பாரம்பரியம், தடி பிடிப்பு, நிலைத்தன்மை, அடிப்படை சுழற்றல்கள் மற்றும் கால்வரிசைகளை உள்ளடக்கிய விரிவான தொடக்க நிலைப் பாடம்.',
    'BEGINNER',
    120,
    'PUBLISHED',
    id
FROM users WHERE role = 'ADMIN' LIMIT 1
ON CONFLICT (slug) DO NOTHING;

INSERT INTO course_modules (
    id, course_id, title, title_ta, description, description_ta, display_order
) VALUES (
    'b1000000-0000-0000-0000-000000000001',
    'c1000000-0000-0000-0000-000000000001',
    'Module 1: History, Heritage & Stances',
    'அலகு 1: வரலாறு,பாரம்பரியம் மற்றும் நிலைகள்',
    'Introduction to Sangam lineage, staff selection, and posture safety.',
    'சங்க காலப் பாரம்பரியம், தடி தேர்வு மற்றும் நிலைகள்.',
    1
) ON CONFLICT (course_id, display_order) DO NOTHING;

INSERT INTO lessons (
    id, module_id, title, title_ta, description, description_ta, content, content_ta,
    lesson_type, estimated_duration_minutes, display_order, status, created_by
) SELECT
    'a1000000-0000-0000-0000-000000000001',
    'b1000000-0000-0000-0000-000000000001',
    'Lesson 1: Introduction to Silambam Heritage',
    'பாடம் 1: சிலம்பப் பாரம்பரிய அறிமுகம்',
    'Overview of historical Sangam texts and staff martial traditions.',
    'சங்க இலக்கிய நூல்கள் மற்றும் தற்காப்பு மரபுகள் பற்றிய பார்வை.',
    'Silambam is referenced in ancient Tamil literature including Silappatikaram. Practice begins with respect to the staff (Silambam Kambu) and understanding body balance.',
    'சிலப்பதிகாரம் உள்ளிட்ட பண்டைய தமிழ் இலக்கியங்களில் சிலம்பம் பற்றி குறிப்பிடப்பட்டுள்ளது. பிரம்புத் தடிக்கு மரியாதை செலுத்துவதுடன் பயிற்சி தொடங்குகிறது.',
    'MIXED',
    30,
    1,
    'PUBLISHED',
    id
FROM users WHERE role = 'ADMIN' LIMIT 1
ON CONFLICT (module_id, display_order) DO NOTHING;

INSERT INTO media (
    id, lesson_id, media_type, title, title_ta, url, mime_type, duration_seconds, display_order, alt_text, alt_text_ta
) VALUES (
    'e1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000001',
    'VIDEO',
    'Silambam Heritage Overview',
    'சிலம்பப் பாரம்பரிய மேலோட்டம்',
    'https://assets.marabagam.org/videos/silambam-intro.mp4',
    'video/mp4',
    300,
    1,
    'Master demonstrating Silambam stance and staff grip',
    'சிலம்ப ஆசான் கால்வரிசை மற்றும் தடி பிடிப்பை விளக்கும் காட்சி'
) ON CONFLICT DO NOTHING;
