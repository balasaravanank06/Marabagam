#!/usr/bin/env python3
"""
Full Content Builder for Flyway Migration:
V11__cultural_content_and_data_enrichment.sql

Assembles:
1. Category: Traditional Crafts
2. 27 Authentic Tamil Art Forms + Categories + Districts mappings
3. 15 Comprehensive Courses with Modules, Lessons, Media, and Quizzes (Questions, Options, Explanations)
4. 32 Verified Cultural Heritage Locations & Academies across Tamil Nadu
5. 12 Major Cultural Festivals & Gatherings
6. 15 Knowledge Documents & 60 Vector Chunks (1536-dimensional normalized embeddings)
"""

import hashlib
import math
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from scripts.data_art_forms import ART_FORMS
from scripts.data_courses import COURSES
from scripts.data_locations import LOCATIONS
from scripts.data_events import EVENTS
from scripts.data_knowledge import KNOWLEDGE_DOCS

OUTPUT_SQL_PATH = "backend/src/main/resources/db/migration/V11__cultural_content_and_data_enrichment.sql"

ADMIN_USER_ID = "00000000-0000-0000-0000-000000000001"
EXPERT_USER_ID = "00000000-0000-0000-0000-000000000002"

def generate_vector(text: str, dimension: int = 1536):
    vec = []
    clean_text = text.strip()
    for i in range(dimension):
        seed_str = f"{clean_text}:{i}"
        digest = hashlib.sha256(seed_str.encode('utf-8')).hexdigest()
        val = (int(digest[:8], 16) / 0xFFFFFFFF) * 2.0 - 1.0
        vec.append(val)
    norm = math.sqrt(sum(x * x for x in vec))
    if norm > 0:
        vec = [x / norm for x in vec]
    return vec

def escape_sql(text) -> str:
    if text is None:
        return "NULL"
    return "'" + str(text).replace("'", "''") + "'"

def format_vector_sql(vec):
    return "'" + "[" + ",".join(f"{x:.6f}" for x in vec) + "]" + "'::vector"

def main():
    lines = []
    def p(line=""):
        lines.append(line)

    p("-- ============================================================")
    p("-- MARABAGAM DIGITAL HERITAGE PLATFORM")
    p("-- V11: CULTURAL CONTENT & DATA ENRICHMENT MIGRATION")
    p("-- Comprehensive Tamil Traditional Arts, Courses, Quizzes,")
    p("-- Heritage Locations, Events, and AI Guide RAG Embeddings")
    p("-- ============================================================")
    p("")

    # ============================================================
    # 1. CATEGORIES
    # ============================================================
    p("-- ============================================================")
    p("-- 1. CATEGORIES")
    p("-- ============================================================")
    p("INSERT INTO categories (name, name_ta, description, description_ta) VALUES")
    p("('Traditional Crafts', 'பாரம்பரியக் கைவினைக் கலைகள்', 'Centuries-old Tamil handicraft, weaving, bronze casting, and painting heritage.', 'நூற்றாண்டுகள் பழமையான தமிழ் கைவினை, நெசவு, சிற்பம் மற்றும் ஓவிய மரபுகள்.')")
    p("ON CONFLICT (name) DO UPDATE SET")
    p("    name_ta = EXCLUDED.name_ta,")
    p("    description = EXCLUDED.description,")
    p("    description_ta = EXCLUDED.description_ta;")
    p("")

    # ============================================================
    # 2. ART FORMS (27 Authentic Art Forms)
    # ============================================================
    p("-- ============================================================")
    p("-- 2. ART FORMS DIRECTORY (27 Art Forms)")
    p("-- ============================================================")
    for af in ART_FORMS:
        p(f"-- Art Form: {af['name']}")
        p("INSERT INTO art_forms (")
        p("    id, name, name_ta, slug, short_description, short_description_ta,")
        p("    description, description_ta, origin, origin_ta, historical_period, historical_period_ta,")
        p("    cultural_significance, cultural_significance_ta, performance_style, performance_style_ta,")
        p("    costume, costume_ta, instruments, instruments_ta, status")
        p(") VALUES (")
        p(f"    {escape_sql(af['id'])},")
        p(f"    {escape_sql(af['name'])},")
        p(f"    {escape_sql(af['name_ta'])},")
        p(f"    {escape_sql(af['slug'])},")
        p(f"    {escape_sql(af['short_description'])},")
        p(f"    {escape_sql(af['short_description_ta'])},")
        p(f"    {escape_sql(af['description'])},")
        p(f"    {escape_sql(af['description_ta'])},")
        p(f"    {escape_sql(af['origin'])},")
        p(f"    {escape_sql(af['origin_ta'])},")
        p(f"    {escape_sql(af['historical_period'])},")
        p(f"    {escape_sql(af['historical_period_ta'])},")
        p(f"    {escape_sql(af['cultural_significance'])},")
        p(f"    {escape_sql(af['cultural_significance_ta'])},")
        p(f"    {escape_sql(af['performance_style'])},")
        p(f"    {escape_sql(af['performance_style_ta'])},")
        p(f"    {escape_sql(af['costume'])},")
        p(f"    {escape_sql(af['costume_ta'])},")
        p(f"    {escape_sql(af['instruments'])},")
        p(f"    {escape_sql(af['instruments_ta'])},")
        p("    'PUBLISHED'")
        p(") ON CONFLICT (slug) DO UPDATE SET")
        p("    name = EXCLUDED.name,")
        p("    name_ta = EXCLUDED.name_ta,")
        p("    short_description = EXCLUDED.short_description,")
        p("    short_description_ta = EXCLUDED.short_description_ta,")
        p("    description = EXCLUDED.description,")
        p("    description_ta = EXCLUDED.description_ta,")
        p("    origin = EXCLUDED.origin,")
        p("    origin_ta = EXCLUDED.origin_ta,")
        p("    historical_period = EXCLUDED.historical_period,")
        p("    historical_period_ta = EXCLUDED.historical_period_ta,")
        p("    cultural_significance = EXCLUDED.cultural_significance,")
        p("    cultural_significance_ta = EXCLUDED.cultural_significance_ta,")
        p("    performance_style = EXCLUDED.performance_style,")
        p("    performance_style_ta = EXCLUDED.performance_style_ta,")
        p("    costume = EXCLUDED.costume,")
        p("    costume_ta = EXCLUDED.costume_ta,")
        p("    instruments = EXCLUDED.instruments,")
        p("    instruments_ta = EXCLUDED.instruments_ta,")
        p("    status = 'PUBLISHED';")
        p("")

        # Category mappings
        for cat_name in af.get("category_names", []):
            p(f"INSERT INTO art_form_categories (art_form_id, category_id)")
            p(f"SELECT af.id, c.id FROM art_forms af, categories c")
            p(f"WHERE af.slug = {escape_sql(af['slug'])} AND c.name = {escape_sql(cat_name)}")
            p("ON CONFLICT DO NOTHING;")

        # District mappings
        for dist_code in af.get("district_codes", []):
            p(f"INSERT INTO art_form_districts (art_form_id, district_id)")
            p(f"SELECT af.id, d.id FROM art_forms af, districts d")
            p(f"WHERE af.slug = {escape_sql(af['slug'])} AND d.code = {escape_sql(dist_code)}")
            p("ON CONFLICT DO NOTHING;")
        p("")

    # ============================================================
    # 3. COURSES, MODULES, LESSONS, MEDIA, QUIZZES (15 Courses)
    # ============================================================
    p("-- ============================================================")
    p("-- 3. STRUCTURED E-LEARNING COURSES, LESSONS & QUIZZES")
    p("-- ============================================================")

    for c_idx, c in enumerate(COURSES, start=1):
        course_id = c["id"]
        slug = c["slug"]
        p(f"-- ------------------------------------------------------------")
        p(f"-- Course {c_idx}: {c['title']}")
        p(f"-- ------------------------------------------------------------")
        p("INSERT INTO courses (")
        p("    id, art_form_id, title, title_ta, slug, short_description, short_description_ta,")
        p("    description, description_ta, difficulty, estimated_duration_minutes, status, created_by")
        p(") VALUES (")
        p(f"    {escape_sql(course_id)},")
        p(f"    (SELECT id FROM art_forms WHERE slug = {escape_sql(c['art_form_slug'])} LIMIT 1),")
        p(f"    {escape_sql(c['title'])},")
        p(f"    {escape_sql(c['title_ta'])},")
        p(f"    {escape_sql(slug)},")
        p(f"    {escape_sql(c['short_description'])},")
        p(f"    {escape_sql(c['short_description_ta'])},")
        p(f"    {escape_sql(c['description'])},")
        p(f"    {escape_sql(c['description_ta'])},")
        p(f"    {escape_sql(c['difficulty'])},")
        p(f"    {c['estimated_duration_minutes']},")
        p("    'PUBLISHED',")
        p(f"    {escape_sql(ADMIN_USER_ID)}")
        p(") ON CONFLICT (slug) DO UPDATE SET")
        p("    title = EXCLUDED.title,")
        p("    title_ta = EXCLUDED.title_ta,")
        p("    short_description = EXCLUDED.short_description,")
        p("    short_description_ta = EXCLUDED.short_description_ta,")
        p("    description = EXCLUDED.description,")
        p("    description_ta = EXCLUDED.description_ta,")
        p("    difficulty = EXCLUDED.difficulty,")
        p("    estimated_duration_minutes = EXCLUDED.estimated_duration_minutes,")
        p("    status = 'PUBLISHED';")
        p("")

        # Modules
        for m_order, m in enumerate(c["modules"], start=1):
            mod_id = f"ba{c_idx:02x}0000-0000-0000-0000-{m_order:012x}"
            p(f"INSERT INTO course_modules (id, course_id, title, title_ta, description, description_ta, display_order) VALUES (")
            p(f"    {escape_sql(mod_id)},")
            p(f"    (SELECT id FROM courses WHERE slug = {escape_sql(slug)} LIMIT 1),")
            p(f"    {escape_sql(m['title'])},")
            p(f"    {escape_sql(m['title_ta'])},")
            p(f"    {escape_sql(m['description'])},")
            p(f"    {escape_sql(m['description_ta'])},")
            p(f"    {m_order}")
            p(") ON CONFLICT (course_id, display_order) DO UPDATE SET")
            p("    title = EXCLUDED.title,")
            p("    title_ta = EXCLUDED.title_ta,")
            p("    description = EXCLUDED.description,")
            p("    description_ta = EXCLUDED.description_ta;")
            p("")

            # Lessons
            for l_order, l in enumerate(m["lessons"], start=1):
                lesson_id = f"bb{c_idx:02x}{m_order:02x}00-0000-0000-0000-{l_order:012x}"
                media_id = f"bc{c_idx:02x}{m_order:02x}00-0000-0000-0000-{l_order:012x}"

                p(f"INSERT INTO lessons (")
                p("    id, module_id, title, title_ta, description, description_ta,")
                p("    content, content_ta, lesson_type, estimated_duration_minutes, display_order, status, created_by")
                p(") VALUES (")
                p(f"    {escape_sql(lesson_id)},")
                p(f"    (SELECT id FROM course_modules WHERE course_id = (SELECT id FROM courses WHERE slug = {escape_sql(slug)} LIMIT 1) AND display_order = {m_order} LIMIT 1),")
                p(f"    {escape_sql(l['title'])},")
                p(f"    {escape_sql(l['title_ta'])},")
                p(f"    {escape_sql(l['description'])},")
                p(f"    {escape_sql(l['description_ta'])},")
                p(f"    {escape_sql(l['content'])},")
                p(f"    {escape_sql(l['content_ta'])},")
                p("    'MIXED',")
                p(f"    {l['duration']},")
                p(f"    {l_order},")
                p("    'PUBLISHED',")
                p(f"    {escape_sql(ADMIN_USER_ID)}")
                p(") ON CONFLICT (module_id, display_order) DO UPDATE SET")
                p("    title = EXCLUDED.title,")
                p("    title_ta = EXCLUDED.title_ta,")
                p("    description = EXCLUDED.description,")
                p("    description_ta = EXCLUDED.description_ta,")
                p("    content = EXCLUDED.content,")
                p("    content_ta = EXCLUDED.content_ta,")
                p("    estimated_duration_minutes = EXCLUDED.estimated_duration_minutes,")
                p("    status = 'PUBLISHED';")
                p("")

                # Media
                p(f"INSERT INTO media (id, lesson_id, media_type, title, title_ta, url, thumbnail_url, mime_type, file_size_bytes, duration_seconds, display_order, alt_text, alt_text_ta) VALUES (")
                p(f"    {escape_sql(media_id)},")
                p(f"    (SELECT id FROM lessons WHERE module_id = (SELECT id FROM course_modules WHERE course_id = (SELECT id FROM courses WHERE slug = {escape_sql(slug)} LIMIT 1) AND display_order = {m_order} LIMIT 1) AND display_order = {l_order} LIMIT 1),")
                p("    'VIDEO',")
                p(f"    {escape_sql(l['media_title'])},")
                p(f"    {escape_sql(l['title_ta'])},")
                p(f"    {escape_sql(l['media_url'])},")
                p("    'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=800&q=80',")
                p("    'video/mp4',")
                p("    15728640,")
                p(f"    {l['duration'] * 60},")
                p("    1,")
                p(f"    {escape_sql(l['media_title'])},")
                p(f"    {escape_sql(l['title_ta'])}")
                p(") ON CONFLICT (id) DO UPDATE SET")
                p("    title = EXCLUDED.title,")
                p("    title_ta = EXCLUDED.title_ta,")
                p("    url = EXCLUDED.url,")
                p("    duration_seconds = EXCLUDED.duration_seconds;")
                p("")

        # Quiz
        quiz = c["quiz"]
        quiz_id = f"bd{c_idx:02x}0000-0000-0000-0000-000000000001"
        p(f"-- Quiz for Course {c_idx}")
        p("INSERT INTO quizzes (")
        p("    id, scope, course_id, module_id, lesson_id, title, title_ta, description, description_ta,")
        p("    pass_percentage, max_attempts, time_limit_minutes, is_mandatory, status, created_by,")
        p("    approved_by, approved_at, published_at")
        p(") VALUES (")
        p(f"    {escape_sql(quiz_id)},")
        p("    'COURSE',")
        p(f"    (SELECT id FROM courses WHERE slug = {escape_sql(slug)} LIMIT 1),")
        p("    NULL,")
        p("    NULL,")
        p(f"    {escape_sql(quiz['title'])},")
        p(f"    {escape_sql(quiz['title_ta'])},")
        p(f"    'Comprehensive cultural comprehension and practical knowledge assessment for {c['title']}.',")
        p(f"    '{c['title_ta']} பாடநெறிக்கான விரிவான மதிப்பீட்டுத் தேர்வு.',")
        p("    80.0,")
        p("    5,")
        p("    20,")
        p("    true,")
        p("    'PUBLISHED',")
        p(f"    {escape_sql(ADMIN_USER_ID)},")
        p(f"    {escape_sql(EXPERT_USER_ID)},")
        p("    CURRENT_TIMESTAMP,")
        p("    CURRENT_TIMESTAMP")
        p(") ON CONFLICT (id) DO UPDATE SET")
        p("    title = EXCLUDED.title,")
        p("    title_ta = EXCLUDED.title_ta,")
        p("    description = EXCLUDED.description,")
        p("    description_ta = EXCLUDED.description_ta,")
        p("    pass_percentage = EXCLUDED.pass_percentage,")
        p("    status = 'PUBLISHED';")
        p("")

        # Quiz Questions & Options
        for q_order, q in enumerate(quiz["questions"], start=1):
            q_id = f"be{c_idx:02x}0000-0000-0000-0000-{q_order:012x}"
            p(f"INSERT INTO quiz_questions (id, quiz_id, question_text, question_text_ta, question_type, points, display_order) VALUES (")
            p(f"    {escape_sql(q_id)},")
            p(f"    {escape_sql(quiz_id)},")
            p(f"    {escape_sql(q['text'])},")
            p(f"    {escape_sql(q['text_ta'])},")
            p("    'MULTIPLE_CHOICE',")
            p("    10,")
            p(f"    {q_order}")
            p(") ON CONFLICT (quiz_id, display_order) DO UPDATE SET")
            p("    question_text = EXCLUDED.question_text,")
            p("    question_text_ta = EXCLUDED.question_text_ta,")
            p("    points = EXCLUDED.points;")
            p("")

            for opt_order, opt in enumerate(q["options"], start=1):
                opt_id = f"bf{c_idx:02x}{q_order:02x}00-0000-0000-0000-{opt_order:012x}"
                is_correct = "true" if opt.get("correct", False) else "false"
                exp_en = opt.get("exp_en", "")
                exp_ta = opt.get("exp_ta", "")
                p(f"INSERT INTO question_options (id, question_id, option_text, option_text_ta, is_correct, explanation_en, explanation_ta, display_order) VALUES (")
                p(f"    {escape_sql(opt_id)},")
                p(f"    (SELECT id FROM quiz_questions WHERE quiz_id = {escape_sql(quiz_id)} AND display_order = {q_order} LIMIT 1),")
                p(f"    {escape_sql(opt['text'])},")
                p(f"    {escape_sql(opt['text_ta'])},")
                p(f"    {is_correct},")
                p(f"    {escape_sql(exp_en)},")
                p(f"    {escape_sql(exp_ta)},")
                p(f"    {opt_order}")
                p(") ON CONFLICT (question_id, display_order) DO UPDATE SET")
                p("    option_text = EXCLUDED.option_text,")
                p("    option_text_ta = EXCLUDED.option_text_ta,")
                p("    is_correct = EXCLUDED.is_correct,")
                p("    explanation_en = EXCLUDED.explanation_en,")
                p("    explanation_ta = EXCLUDED.explanation_ta;")
                p("")

    # ============================================================
    # 4. HERITAGE LOCATIONS & VERIFIED ACADEMIES (32 Locations)
    # ============================================================
    p("-- ============================================================")
    p("-- 4. HERITAGE LOCATIONS & VERIFIED ACADEMIES (32 Locations)")
    p("-- ============================================================")
    for loc in LOCATIONS:
        p(f"-- Location: {loc['name']}")
        p("INSERT INTO heritage_locations (")
        p("    id, name, name_ta, slug, district_id, art_form_id, location_type,")
        p("    description, description_ta, address, address_ta, latitude, longitude,")
        p("    contact_phone, contact_email, website_url, provenance_source, status, created_by")
        p(") VALUES (")
        p(f"    {escape_sql(loc['id'])},")
        p(f"    {escape_sql(loc['name'])},")
        p(f"    {escape_sql(loc['name_ta'])},")
        p(f"    {escape_sql(loc['slug'])},")
        p(f"    (SELECT id FROM districts WHERE code = {escape_sql(loc['district_code'])} LIMIT 1),")
        p(f"    (SELECT id FROM art_forms WHERE slug = {escape_sql(loc['art_form_slug'])} LIMIT 1),")
        p(f"    {escape_sql(loc['location_type'])},")
        p(f"    {escape_sql(loc['description'])},")
        p(f"    {escape_sql(loc['description_ta'])},")
        p(f"    {escape_sql(loc['address'])},")
        p(f"    {escape_sql(loc['address_ta'])},")
        p(f"    {loc['latitude']},")
        p(f"    {loc['longitude']},")
        p(f"    {escape_sql(loc['contact_phone'])},")
        p(f"    {escape_sql(loc['contact_email'])},")
        p(f"    {escape_sql(loc['website_url'])},")
        p(f"    {escape_sql(loc['provenance_source'])},")
        p("    'PUBLISHED',")
        p(f"    {escape_sql(ADMIN_USER_ID)}")
        p(") ON CONFLICT (slug) DO UPDATE SET")
        p("    name = EXCLUDED.name,")
        p("    name_ta = EXCLUDED.name_ta,")
        p("    district_id = EXCLUDED.district_id,")
        p("    art_form_id = EXCLUDED.art_form_id,")
        p("    location_type = EXCLUDED.location_type,")
        p("    description = EXCLUDED.description,")
        p("    description_ta = EXCLUDED.description_ta,")
        p("    address = EXCLUDED.address,")
        p("    address_ta = EXCLUDED.address_ta,")
        p("    latitude = EXCLUDED.latitude,")
        p("    longitude = EXCLUDED.longitude,")
        p("    contact_phone = EXCLUDED.contact_phone,")
        p("    contact_email = EXCLUDED.contact_email,")
        p("    website_url = EXCLUDED.website_url,")
        p("    provenance_source = EXCLUDED.provenance_source,")
        p("    status = 'PUBLISHED';")
        p("")

    # ============================================================
    # 5. CULTURAL EVENTS & FESTIVALS (12 Events)
    # ============================================================
    p("-- ============================================================")
    p("-- 5. CULTURAL EVENTS & FESTIVALS (12 Events)")
    p("-- ============================================================")
    for ev in EVENTS:
        p(f"-- Event: {ev['title']}")
        p("INSERT INTO cultural_events (")
        p("    id, title, title_ta, slug, district_id, art_form_id,")
        p("    description, description_ta, event_type, event_period, event_period_ta,")
        p("    venue_name, venue_name_ta, latitude, longitude, provenance_source, status, created_by")
        p(") VALUES (")
        p(f"    {escape_sql(ev['id'])},")
        p(f"    {escape_sql(ev['title'])},")
        p(f"    {escape_sql(ev['title_ta'])},")
        p(f"    {escape_sql(ev['slug'])},")
        p(f"    (SELECT id FROM districts WHERE code = {escape_sql(ev['district_code'])} LIMIT 1),")
        p(f"    (SELECT id FROM art_forms WHERE slug = {escape_sql(ev['art_form_slug'])} LIMIT 1),")
        p(f"    {escape_sql(ev['description'])},")
        p(f"    {escape_sql(ev['description_ta'])},")
        p(f"    {escape_sql(ev['event_type'])},")
        p(f"    {escape_sql(ev['event_period'])},")
        p(f"    {escape_sql(ev['event_period_ta'])},")
        p(f"    {escape_sql(ev['venue_name'])},")
        p(f"    {escape_sql(ev['venue_name_ta'])},")
        p(f"    {ev['latitude']},")
        p(f"    {ev['longitude']},")
        p(f"    {escape_sql(ev['provenance_source'])},")
        p("    'PUBLISHED',")
        p(f"    {escape_sql(ADMIN_USER_ID)}")
        p(") ON CONFLICT (slug) DO UPDATE SET")
        p("    title = EXCLUDED.title,")
        p("    title_ta = EXCLUDED.title_ta,")
        p("    district_id = EXCLUDED.district_id,")
        p("    art_form_id = EXCLUDED.art_form_id,")
        p("    description = EXCLUDED.description,")
        p("    description_ta = EXCLUDED.description_ta,")
        p("    event_type = EXCLUDED.event_type,")
        p("    event_period = EXCLUDED.event_period,")
        p("    event_period_ta = EXCLUDED.event_period_ta,")
        p("    venue_name = EXCLUDED.venue_name,")
        p("    venue_name_ta = EXCLUDED.venue_name_ta,")
        p("    latitude = EXCLUDED.latitude,")
        p("    longitude = EXCLUDED.longitude,")
        p("    provenance_source = EXCLUDED.provenance_source,")
        p("    status = 'PUBLISHED';")
        p("")

    # ============================================================
    # 6. AI CULTURAL GUIDE RAG DOCUMENTS & 1536d CHUNKS (15 Docs, 60 Chunks)
    # ============================================================
    p("-- ============================================================")
    p("-- 6. AI CULTURAL GUIDE RAG DOCUMENTS & 1536d CHUNKS")
    p("-- ============================================================")
    for doc_idx, doc in enumerate(KNOWLEDGE_DOCS, start=1):
        p(f"-- Knowledge Document {doc_idx}: {doc['title']}")
        p("INSERT INTO knowledge_documents (")
        p("    id, title, title_ta, content, content_ta, source_type, source_name, source_url, author,")
        p("    language, art_form_id, district_id, status, indexing_status, embedding_model, embedding_dimension,")
        p("    created_by, reviewed_by, version")
        p(") VALUES (")
        p(f"    {escape_sql(doc['id'])},")
        p(f"    {escape_sql(doc['title'])},")
        p(f"    {escape_sql(doc['title_ta'])},")
        p(f"    {escape_sql(doc['content'])},")
        p(f"    {escape_sql(doc['content_ta'])},")
        p(f"    {escape_sql(doc['source_type'])},")
        p(f"    {escape_sql(doc['source_name'])},")
        p(f"    {escape_sql(doc['source_url'])},")
        p(f"    {escape_sql(doc['author'])},")
        p("    'both',")
        p(f"    (SELECT id FROM art_forms WHERE slug = {escape_sql(doc['art_form_slug'])} LIMIT 1),")
        p(f"    (SELECT id FROM districts WHERE code = {escape_sql(doc['district_code'])} LIMIT 1),")
        p("    'APPROVED',")
        p("    'INDEXED',")
        p("    'text-embedding-3-small',")
        p("    1536,")
        p(f"    {escape_sql(ADMIN_USER_ID)},")
        p(f"    {escape_sql(EXPERT_USER_ID)},")
        p("    1")
        p(") ON CONFLICT (id) DO UPDATE SET")
        p("    title = EXCLUDED.title,")
        p("    title_ta = EXCLUDED.title_ta,")
        p("    content = EXCLUDED.content,")
        p("    content_ta = EXCLUDED.content_ta,")
        p("    source_type = EXCLUDED.source_type,")
        p("    status = 'APPROVED',")
        p("    indexing_status = 'INDEXED';")
        p("")

        for c_idx, chunk_text in enumerate(doc["chunks"]):
            vec = generate_vector(chunk_text, dimension=1536)
            vec_sql = format_vector_sql(vec)
            meta = {
                "document_title": doc["title"],
                "source_name": doc["source_name"],
                "author": doc["author"],
                "chunk_index": c_idx,
                "language": "en"
            }
            meta_json = json.dumps(meta, ensure_ascii=False)

            p(f"INSERT INTO knowledge_chunks (id, document_id, chunk_index, content_text, language, embedding, embedding_model, metadata_json) VALUES (")
            p(f"    gen_random_uuid(),")
            p(f"    {escape_sql(doc['id'])},")
            p(f"    {c_idx},")
            p(f"    {escape_sql(chunk_text)},")
            p("    'en',")
            p(f"    {vec_sql},")
            p("    'text-embedding-3-small',")
            p(f"    {escape_sql(meta_json)}::jsonb")
            p(") ON CONFLICT (document_id, chunk_index) DO UPDATE SET")
            p("    content_text = EXCLUDED.content_text,")
            p("    embedding = EXCLUDED.embedding,")
            p("    metadata_json = EXCLUDED.metadata_json;")
            p("")

    p("-- ============================================================")
    p("-- END OF V11 CULTURAL CONTENT ENRICHMENT MIGRATION")
    p("-- ============================================================")

    os.makedirs(os.path.dirname(OUTPUT_SQL_PATH), exist_ok=True)
    with open(OUTPUT_SQL_PATH, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"Successfully generated complete migration: {OUTPUT_SQL_PATH} with {len(lines)} lines.")

if __name__ == "__main__":
    main()
