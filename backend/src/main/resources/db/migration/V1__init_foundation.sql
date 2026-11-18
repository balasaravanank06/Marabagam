-- ============================================================
-- MARABAGAM DIGITAL HERITAGE PLATFORM
-- V1: Foundation Schema Migration Baseline
-- ============================================================

CREATE TABLE IF NOT EXISTS schema_version_check (
    id SERIAL PRIMARY KEY,
    component_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    initialized_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO schema_version_check (component_name, status)
VALUES ('Marabagam Foundation', 'INITIALIZED')
ON CONFLICT DO NOTHING;
