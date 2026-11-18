-- Phase 9 — Events, Recommendations & Community Migration
-- Authoritative database schema for Phase 9

-- 1. Extend cultural_events table (created in V6)
-- NOTE: is_date_approximate ALREADY exists in V6 cultural_events and is NOT re-added here.
ALTER TABLE cultural_events
    ADD COLUMN IF NOT EXISTS capacity INTEGER,
    ADD COLUMN IF NOT EXISTS registration_deadline TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS registration_required BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_online BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS meeting_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS organizer_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS instructor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS start_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS end_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS timezone_identifier VARCHAR(50) DEFAULT 'Asia/Kolkata',
    ADD CONSTRAINT chk_event_end_after_start CHECK (end_at IS NULL OR start_at IS NULL OR end_at > start_at),
    ADD CONSTRAINT chk_event_capacity_positive CHECK (capacity IS NULL OR capacity > 0),
    ADD CONSTRAINT chk_event_reg_deadline CHECK (registration_deadline IS NULL OR start_at IS NULL OR registration_deadline <= start_at);

-- 2. Create Table: event_registrations
CREATE TABLE IF NOT EXISTS event_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES cultural_events(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'REGISTERED', -- 'REGISTERED', 'CANCELLED'
    registered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    cancelled_at TIMESTAMP WITH TIME ZONE
);

-- Partial Unique Index enforcing single active registration while allowing historical cancelled rows
CREATE UNIQUE INDEX IF NOT EXISTS idx_uk_active_event_registration 
ON event_registrations (event_id, user_id) 
WHERE status = 'REGISTERED';

-- 3. Create Table: community_posts
CREATE TABLE IF NOT EXISTS community_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    art_form_id UUID REFERENCES art_forms(id) ON DELETE SET NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'DISCUSSION', -- 'DISCUSSION', 'QUESTION', 'LEARNING_JOURNEY', 'EVENT_DISCUSSION', 'ART_FORM_DISCUSSION'
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'VISIBLE', -- 'VISIBLE', 'HIDDEN', 'REMOVED', 'DELETED'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by UUID REFERENCES users(id) ON DELETE SET NULL
);

-- 4. Create Table: community_comments
CREATE TABLE IF NOT EXISTS community_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    parent_comment_id UUID REFERENCES community_comments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'VISIBLE', -- 'VISIBLE', 'HIDDEN', 'REMOVED', 'DELETED'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by UUID REFERENCES users(id) ON DELETE SET NULL
);

-- 5. Create Table: community_reports
CREATE TABLE IF NOT EXISTS community_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(50) NOT NULL, -- 'POST', 'COMMENT'
    target_id UUID NOT NULL,
    reason VARCHAR(100) NOT NULL,
    details TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'DISMISSED', 'ACTIONED'
    action_taken VARCHAR(50),
    actioned_by UUID REFERENCES users(id) ON DELETE SET NULL,
    actioned_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_reporter_target UNIQUE (reporter_id, target_type, target_id)
);

-- 6. Create Table: notifications
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    title_ta VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    message_ta TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    deduplication_key VARCHAR(255),
    related_entity_type VARCHAR(50),
    related_entity_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_recipient_deduplication UNIQUE (recipient_id, deduplication_key)
);

-- 7. Create Table: notification_preferences
CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    events_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    community_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    achievements_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 8. Performance Indexes
CREATE INDEX IF NOT EXISTS idx_cultural_events_status_start ON cultural_events(status, start_at);
CREATE INDEX IF NOT EXISTS idx_cultural_events_district_status ON cultural_events(district_id, status);
CREATE INDEX IF NOT EXISTS idx_cultural_events_artform_status ON cultural_events(art_form_id, status);
CREATE INDEX IF NOT EXISTS idx_event_reg_event_status ON event_registrations(event_id, status);
CREATE INDEX IF NOT EXISTS idx_event_reg_user_status ON event_registrations(user_id, status);
CREATE INDEX IF NOT EXISTS idx_posts_status_created ON community_posts(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_artform_status ON community_posts(art_form_id, status);
CREATE INDEX IF NOT EXISTS idx_comments_post_status ON community_comments(post_id, status, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_read ON notifications(recipient_id, is_read, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reports_status_created ON community_reports(status, created_at ASC);
