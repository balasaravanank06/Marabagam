-- Migration: V7__movement_coach.sql
-- Description: Phase 7 Database Schema for AI Movement Coach / Computer Vision Module

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

-- Indexes for Query Performance & Ownership Lookups
CREATE INDEX idx_movements_art_form ON movements(art_form_id);
CREATE INDEX idx_reference_movements_movement ON reference_movements(movement_id, status);
CREATE INDEX idx_practice_sessions_user ON practice_sessions(user_id, created_at DESC);
CREATE INDEX idx_practice_sessions_movement ON practice_sessions(movement_id);
CREATE INDEX idx_practice_feedbacks_session ON practice_feedbacks(practice_session_id);

-- SEED DATA: Silambam Movements (Clearly marked [DEVELOPMENT SEED — PENDING CULTURAL REVIEW])
INSERT INTO movements (id, art_form_id, code, name_en, name_ta, description_en, description_ta, category, difficulty_level, is_active)
SELECT 
    'a1111111-1111-1111-1111-111111111111',
    af.id,
    'SILAMBAM_MAANKOMBU_SUBADU',
    'Maankombu Subadu (Deer-Horn Stance)',
    'மான்கொம்பு சுவடு',
    '[DEVELOPMENT SEED — PENDING CULTURAL REVIEW] Fundamental Silambam low stance focusing on hip height ratio, knee flexion, and balance.',
    '[DEVELOPMENT SEED — PENDING CULTURAL REVIEW] சிலம்பம் மான்கொம்பு சுவடு நிலை.',
    'STANCE',
    'BEGINNER',
    true
FROM art_forms af WHERE af.slug = 'silambam'
ON CONFLICT (code) DO NOTHING;

INSERT INTO movements (id, art_form_id, code, name_en, name_ta, description_en, description_ta, category, difficulty_level, is_active)
SELECT 
    'a2222222-2222-2222-2222-222222222222',
    af.id,
    'SILAMBAM_STRAIGHT_GUARD',
    'Straight Stance Guard',
    'நேர் தற்காப்பு நிலை',
    '[DEVELOPMENT SEED — PENDING CULTURAL REVIEW] Basic Silambam protective staff posture evaluating chest-to-hip alignment and arm height.',
    '[DEVELOPMENT SEED — PENDING CULTURAL REVIEW] சிலம்பம் நேர் தற்காப்பு நிலை.',
    'STANCE',
    'BEGINNER',
    true
FROM art_forms af WHERE af.slug = 'silambam'
ON CONFLICT (code) DO NOTHING;

INSERT INTO movements (id, art_form_id, code, name_en, name_ta, description_en, description_ta, category, difficulty_level, is_active)
SELECT 
    'a3333333-3333-3333-3333-333333333333',
    af.id,
    'SILAMBAM_ERRATTAI_CHUTTU',
    'Errattai Chuttu (Double Rotation Basic Phase)',
    'இரட்டைச் சுற்று',
    '[DEVELOPMENT SEED — PENDING CULTURAL REVIEW] Dynamic Silambam staff rotation swing sequence evaluating shoulder rotation timing.',
    '[DEVELOPMENT SEED — PENDING CULTURAL REVIEW] சிலம்பம் இரட்டைச் சுற்று வீச்சு நிலை.',
    'SWING',
    'INTERMEDIATE',
    true
FROM art_forms af WHERE af.slug = 'silambam'
ON CONFLICT (code) DO NOTHING;

-- Seed Movement Versions
INSERT INTO movement_versions (id, movement_id, version_number, joint_rules_json, is_current)
VALUES 
(
    'b1111111-1111-1111-1111-111111111111',
    'a1111111-1111-1111-1111-111111111111',
    'v1.0.0',
    '{"type":"STATIC","rules":{"knee_angle":{"min":115.0,"max":140.0,"weight":0.35},"torso_upright":{"min":0.0,"max":15.0,"weight":0.35},"stance_width_ratio":{"min":1.2,"max":1.8,"weight":0.30}}}',
    true
),
(
    'b2222222-2222-2222-2222-222222222222',
    'a2222222-2222-2222-2222-222222222222',
    'v1.0.0',
    '{"type":"STATIC","rules":{"elbow_angle":{"min":80.0,"max":110.0,"weight":0.40},"torso_upright":{"min":0.0,"max":12.0,"weight":0.35},"guard_elevation_ratio":{"min":0.4,"max":0.7,"weight":0.25}}}',
    true
),
(
    'b3333333-3333-3333-3333-333333333333',
    'a3333333-3333-3333-3333-333333333333',
    'v1.0.0',
    '{"type":"DYNAMIC","rules":{"dtw_max_distance":25.0,"phases":["PREPARATION","PEAK_ROTATION","FOLLOW_THROUGH"]}}',
    true
)
ON CONFLICT (movement_id, version_number) DO NOTHING;
