-- ============================================================
-- MARABAGAM DIGITAL HERITAGE PLATFORM
-- V10: Repair Demo Seed Users & Passwords (Development & Demonstration)
-- ============================================================

-- BCrypt hash for password: Password123!
-- Hash: $2a$12$wWsHTsbawhpZkNxOfBATzu8jOuK7HnoiSEf0stXwxeOT53DtR./L2

-- 1. Admin Demo User
INSERT INTO users (id, email, username, password_hash, display_name, role, account_status, preferred_language)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@marabagam.org',
    'admin_demo',
    '$2a$12$wWsHTsbawhpZkNxOfBATzu8jOuK7HnoiSEf0stXwxeOT53DtR./L2',
    'Marabagam Admin',
    'ADMIN',
    'ACTIVE',
    'en'
)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    account_status = 'ACTIVE',
    role = 'ADMIN';

-- Also repair admin_seed username conflict if present
UPDATE users SET username = 'admin_demo' WHERE email = 'admin@marabagam.org';

-- 2. Cultural Expert Demo User
INSERT INTO users (id, email, username, password_hash, display_name, role, account_status, preferred_language)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'expert@marabagam.org',
    'expert_demo',
    '$2a$12$wWsHTsbawhpZkNxOfBATzu8jOuK7HnoiSEf0stXwxeOT53DtR./L2',
    'Dr. Arumugam Cultural Expert',
    'CULTURAL_EXPERT',
    'ACTIVE',
    'ta'
)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    account_status = 'ACTIVE',
    role = 'CULTURAL_EXPERT';

-- 3. Instructor Demo User
INSERT INTO users (id, email, username, password_hash, display_name, role, account_status, preferred_language)
VALUES (
    '00000000-0000-0000-0000-000000000003',
    'instructor@marabagam.org',
    'instructor_demo',
    '$2a$12$wWsHTsbawhpZkNxOfBATzu8jOuK7HnoiSEf0stXwxeOT53DtR./L2',
    'Master Selvam Silambam Instructor',
    'INSTRUCTOR',
    'ACTIVE',
    'en'
)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    account_status = 'ACTIVE',
    role = 'INSTRUCTOR';

-- 4. Artist Demo User
INSERT INTO users (id, email, username, password_hash, display_name, role, account_status, preferred_language)
VALUES (
    '00000000-0000-0000-0000-000000000004',
    'artist@marabagam.org',
    'artist_demo',
    '$2a$12$wWsHTsbawhpZkNxOfBATzu8jOuK7HnoiSEf0stXwxeOT53DtR./L2',
    'Kavitha Bharatanatyam Artist',
    'ARTIST',
    'ACTIVE',
    'ta'
)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    account_status = 'ACTIVE',
    role = 'ARTIST';

-- 5. Learner Demo User
INSERT INTO users (id, email, username, password_hash, display_name, role, account_status, preferred_language)
VALUES (
    '00000000-0000-0000-0000-000000000005',
    'learner@marabagam.org',
    'learner_demo',
    '$2a$12$wWsHTsbawhpZkNxOfBATzu8jOuK7HnoiSEf0stXwxeOT53DtR./L2',
    'Anbu Heritage Learner',
    'LEARNER',
    'ACTIVE',
    'en'
)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    account_status = 'ACTIVE',
    role = 'LEARNER';
