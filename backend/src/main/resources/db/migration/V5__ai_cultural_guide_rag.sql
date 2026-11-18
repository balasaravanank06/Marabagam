-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Knowledge Documents Table
CREATE TABLE knowledge_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    title_ta VARCHAR(255),
    content TEXT NOT NULL,
    content_ta TEXT,
    source_type VARCHAR(50) NOT NULL,
    source_name VARCHAR(255) NOT NULL,
    source_url VARCHAR(2048),
    author VARCHAR(255),
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    art_form_id UUID REFERENCES art_forms(id) ON DELETE SET NULL,
    district_id BIGINT REFERENCES districts(id) ON DELETE SET NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    indexing_status VARCHAR(30) NOT NULL DEFAULT 'NOT_INDEXED',
    embedding_model VARCHAR(100),
    embedding_dimension INT,
    created_by UUID NOT NULL REFERENCES users(id),
    reviewed_by UUID REFERENCES users(id),
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Knowledge Chunks Table
CREATE TABLE knowledge_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content_text TEXT NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    embedding vector(1536) NOT NULL,
    embedding_model VARCHAR(100) NOT NULL DEFAULT 'text-embedding-3-small',
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_knowledge_chunks_doc_index UNIQUE(document_id, chunk_index)
);

-- AI Conversations Table
CREATE TABLE ai_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- AI Messages Table
CREATE TABLE ai_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    sender VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    sources_json JSONB,
    relevance_score DOUBLE PRECISION,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_knowledge_docs_status ON knowledge_documents(status, indexing_status);
CREATE INDEX idx_knowledge_docs_art_form ON knowledge_documents(art_form_id);
CREATE INDEX idx_knowledge_chunks_doc_id ON knowledge_chunks(document_id);
CREATE INDEX idx_ai_conversations_user ON ai_conversations(user_id);
CREATE INDEX idx_ai_messages_conversation ON ai_messages(conversation_id);

-- HNSW Cosine Index for pgvector
CREATE INDEX idx_knowledge_chunks_embedding_hnsw 
ON knowledge_chunks USING hnsw (embedding vector_cosine_ops);

-- Seed Initial Verified Cultural Knowledge Document (Silambam History & Techniques)
INSERT INTO knowledge_documents (
    id, title, title_ta, content, content_ta, source_type, source_name, source_url, author, language,
    status, indexing_status, embedding_model, embedding_dimension, created_by, created_at, updated_at
)
SELECT 
    'f1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c',
    'Silambam Martial Art Foundations',
    'சிலம்பம் தற்காப்புக் கலை அடிப்படை',
    'Silambam is an ancient weapon-based Tamil martial art that originated in Tamil Nadu. The primary weapon used is a bamboo staff (Kamai or Silambam stick) usually measuring 1.6 meters. Key footwork stances include Kamai, Kuthiraisubadu, and Nadisubadu. Historically practiced during the Sangam period, Silambam improves posture, reflex coordination, and endurance.',
    'சிலம்பம் தமிழ்நாட்டின் பழமையான தற்காப்புக் கலை. மூங்கில் கம்பை முதன்மை ஆயுதமாகக் கொண்டு ஆடப்படும் கலை. நிலைப்பாடுகள் கம்பங் கையாளுதல் மற்றும் கால்வரிசையை முதன்மையாகக் கொண்டவை.',
    'HISTORICAL',
    'Tamil Heritage Preservation Foundation Archives',
    'https://marabagam.org/heritage/silambam-foundations',
    'Dr. A. Kovendhan',
    'both',
    'APPROVED',
    'NOT_INDEXED',
    'text-embedding-3-small',
    1536,
    u.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.role = 'ADMIN' LIMIT 1;
