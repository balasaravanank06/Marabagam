-- ============================================================
-- MARABAGAM DIGITAL HERITAGE PLATFORM
-- V3: Art Form Directory & Cultural Content Management Schema
-- ============================================================

-- Categories Table (e.g., Martial Arts, Folk Dance, Street Theatre, Classical Dance, Musical Arts)
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    name_ta VARCHAR(100) NOT NULL,
    description TEXT,
    description_ta TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Districts / Regions Table (e.g., Madurai, Thanjavur, Tirunelveli, Kanchipuram)
CREATE TABLE IF NOT EXISTS districts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    name_ta VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL DEFAULT 'Tamil Nadu',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Art Forms Table
CREATE TABLE IF NOT EXISTS art_forms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    name_ta VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL UNIQUE,
    short_description TEXT NOT NULL,
    short_description_ta TEXT NOT NULL,
    description TEXT NOT NULL,
    description_ta TEXT NOT NULL,
    origin VARCHAR(150),
    origin_ta VARCHAR(150),
    historical_period VARCHAR(100),
    historical_period_ta VARCHAR(100),
    cultural_significance TEXT,
    cultural_significance_ta TEXT,
    performance_style TEXT,
    performance_style_ta TEXT,
    costume TEXT,
    costume_ta TEXT,
    instruments TEXT,
    instruments_ta TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Many-to-Many: Art Forms <-> Categories
CREATE TABLE IF NOT EXISTS art_form_categories (
    art_form_id UUID NOT NULL REFERENCES art_forms(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (art_form_id, category_id)
);

-- Many-to-Many: Art Forms <-> Districts
CREATE TABLE IF NOT EXISTS art_form_districts (
    art_form_id UUID NOT NULL REFERENCES art_forms(id) ON DELETE CASCADE,
    district_id BIGINT NOT NULL REFERENCES districts(id) ON DELETE CASCADE,
    PRIMARY KEY (art_form_id, district_id)
);

-- High-performance Search & Filter Indexes
CREATE INDEX IF NOT EXISTS idx_art_forms_slug ON art_forms(slug);
CREATE INDEX IF NOT EXISTS idx_art_forms_status ON art_forms(status);
CREATE INDEX IF NOT EXISTS idx_art_forms_name ON art_forms(name);
CREATE INDEX IF NOT EXISTS idx_art_forms_name_ta ON art_forms(name_ta);
CREATE INDEX IF NOT EXISTS idx_art_form_categories_cat ON art_form_categories(category_id);
CREATE INDEX IF NOT EXISTS idx_art_form_districts_dist ON art_form_districts(district_id);


-- ============================================================
-- DEVELOPMENT SEED DATA (REPRESENTATIVE TAMIL TRADITIONAL ARTS)
-- ============================================================

INSERT INTO categories (name, name_ta, description, description_ta) VALUES
('Martial Arts', 'தற்காப்புக் கலைகள்', 'Traditional Tamil combat practices, staff fencing, and body balance arts.', 'பாரம்பரிய தமிழ் தற்காப்பு மற்றும் தடி வரிசைக் கலைகள்.'),
('Folk Dance', 'கிராமிய நடனங்கள்', 'Vibrant folk dances performed during temple festivals, harvests, and celebrations.', 'கோவில் திருவிழாக்கள் மற்றும் அறுவடை விழாக்களில் ஆடப்படும் நாட்டுப்புற நடனங்கள்.'),
('Street Theatre', 'தெருக்கூத்து & நாடகம்', 'Dramatic traditional street plays enacting epic tales from Tamil literature.', 'தமிழ் இலக்கிய காவியங்களை வீதி நாடகங்களாக நிகழ்த்தும் கலைகள்.'),
('Musical Arts', 'இசை மற்றும் வாத்தியக் கலைகள்', 'Rhythmic percussion, folk singing, and storytelling music.', 'தமிழர் பாரம்பரிய பறை, வில்லுப்பாட்டு போன்ற இசைவடிவங்கள்.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO districts (name, name_ta, state) VALUES
('Madurai', 'மதுரை', 'Tamil Nadu'),
('Thanjavur', 'தஞ்சாவூர்', 'Tamil Nadu'),
('Tirunelveli', 'திருநெல்வேலி', 'Tamil Nadu'),
('Kanchipuram', 'காஞ்சிபுரம்', 'Tamil Nadu'),
('Salem', 'சேலம்', 'Tamil Nadu')
ON CONFLICT (name) DO NOTHING;

-- Seed Representative Art Form: Silambam (PUBLISHED)
INSERT INTO art_forms (
    id, name, name_ta, slug, short_description, short_description_ta,
    description, description_ta, origin, origin_ta, historical_period, historical_period_ta,
    cultural_significance, cultural_significance_ta, performance_style, performance_style_ta,
    costume, costume_ta, instruments, instruments_ta, status
) VALUES (
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'Silambam', 'சிலம்பம்', 'silambam',
    'Ancient Tamil staff fencing martial art emphasizing speed, agility, and footwork.',
    'வேகம், நெகிழ்வுத்தன்மை மற்றும் கால்வரிசையை மையமாகக் கொண்ட பண்டைய தமிழ் தற்காப்புக் கலை.',
    'Silambam is an ancient weapon-based martial art originating in Tamilakam. Mentioned in Tamil Sangam literature such as Silappatikaram, it involves master staff control, footwork stances (Kalvarisai), and body defensive techniques.',
    'சிலம்பம் என்பது தமிழகத்தின் மிகப்பண்டைய தற்காப்புக் கலைகளில் ஒன்றாகும். சிலப்பதிகாரம் போன்ற சங்க இலக்கியங்களில் சிலம்பப் போர் பற்றிய குறிப்புகள் உள்ளன. இது தடி வரிசை, கால் வரிசை மற்றும் தற்காப்பு முறைகளை உள்ளடக்கியது.',
    'Tamil Nadu (Kurinji Region)', 'தமிழ்நாடு (குறிஞ்சி நிலப்பகுதி)',
    'Sangam Era (c. 300 BCE)', 'சங்க காலம் (சுமார் கி.மு. 300)',
    'Symbol of valour, bodily discipline, and self-defence heritage in Tamil culture.',
    'தமிழர் வீரத்தின் அடையாளம், உடல் கட்டுப்பாடு மற்றும் தற்காப்புப் பாரம்பரியத்தின் சின்னம்.',
    'Dynamic footwork, rapid stick spins, and strike evasions in rhythm.',
    'வேகமான தடி சுழற்றல்கள், கால்வரிசை நகர்வுகள் மற்றும் எதிர்தாக்குதல் தற்காப்பு.',
    'Lightweight cotton dhoti, waist sash (Kachai), and comfortable bare-chest or vest attire.',
    'இலேசான வேட்டி, இடுப்புக் கச்சை மற்றும் சௌகரியமான உடை.',
    'Bamboo staff (Silambam Kambu), Maru (deer horns), Kethayam (shield).',
    'பிரம்புச் சிலம்புத் தடி, மான் கொம்பு (மழு), கேடயம்.',
    'PUBLISHED'
) ON CONFLICT (slug) DO NOTHING;

-- Seed Representative Art Form: Karagattam (PUBLISHED)
INSERT INTO art_forms (
    id, name, name_ta, slug, short_description, short_description_ta,
    description, description_ta, origin, origin_ta, historical_period, historical_period_ta,
    cultural_significance, cultural_significance_ta, performance_style, performance_style_ta,
    costume, costume_ta, instruments, instruments_ta, status
) VALUES (
    'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
    'Karagattam', 'கரகாட்டம்', 'karagattam',
    'Traditional Tamil folk dance balancing decorated brass pots filled with rice on the head.',
    'தலையில் அலங்கரிக்கப்பட்ட சொம்பை வைத்துச் சமநிலை தவறாமல் ஆடப்படும் புகழ்பெற்ற நாட்டுப்புற நடனம்.',
    'Karagattam is a classical Tamil folk dance dedicated to Mariamman, the goddess of rain. Performers balance decorated pots (Karagam) on their heads while dancing to fast-paced Nadaswaram and Thavil rhythms.',
    'கரகாட்டம் என்பது மழைக்கடவுளான மாரியம்மனை வழிபட்டு ஆடப்படும் பாரம்பரிய கிராமிய நடனம். தலையில் அலங்கரிக்கப்பட்ட குடத்தைச் சுமந்து தவில் மற்றும் நாதஸ்வர இசைக்கு ஏற்ப கலைஞர்கள் சமநிலையுடன் ஆடுவர்.',
    'Thanjavur & Madurai Regions', 'தஞ்சாவூர் மற்றும் மதுரை மண்டலங்கள்',
    'Ancient Folk Tradition', 'பண்டைய கிராமியக் பாரம்பரியம்',
    'Associated with rain worship, village temple festivals, and agricultural prosperity.',
    'மாரியம்மன் வழிபாடு, கிராமக் கோவில் திருவிழாக்கள் மற்றும் விவசாய செழிப்புடன் தொடர்புடையது.',
    'Acrobatic balance moves, body bending, and rhythmic stepping without dropping the Karagam.',
    'கரகம் விழாமல் உடலை வளைத்து, சாகசங்களைச் செய்து தாளத்திற்கு ஏற்ப ஆடுதல்.',
    'Brightly coloured silk costumes, flower garlands, and anklets (Salangai).',
    'வண்ணமயமான பட்டு உடைகள், பூமாலைகள் மற்றும் சலங்கை.',
    'Nadaswaram, Thavil, Pambai, Urumi.',
    'நாதஸ்வரம், தவில், பம்பை, உறுமி.',
    'PUBLISHED'
) ON CONFLICT (slug) DO NOTHING;

-- Seed Representative Art Form: Therukoothu (PUBLISHED)
INSERT INTO art_forms (
    id, name, name_ta, slug, short_description, short_description_ta,
    description, description_ta, origin, origin_ta, historical_period, historical_period_ta,
    cultural_significance, cultural_significance_ta, performance_style, performance_style_ta,
    costume, costume_ta, instruments, instruments_ta, status
) VALUES (
    'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
    'Therukoothu', 'தெருக்கூத்து', 'therukoothu',
    'Vibrant Tamil open-air street theatre combining music, dance, dialogue, and ornate makeup.',
    'இசை, நடனம், வசனம் மற்றும் பிரம்மாண்ட ஒப்பனையுடன் திறந்தவெளியில் நிகழ்த்தப்படும் தெருக்கூத்து.',
    'Therukoothu (literally street play) is an open-air Tamil folk theatre form traditionally enacted in village squares during temple festivals like Draupadi Amman Vizha.',
    'தெருக்கூத்து என்பது கிராமப்புறக் கோவில் திருவிழாக்களில் இரவு முழுதும் திறந்தவெளியில் நடக்கும் திறம்படக் கூடிய இசை நாடகக் கலையாகும்.',
    'Kanchipuram & Northern Tamil Nadu', 'காஞ்சிபுரம் மற்றும் வட தமிழகம்',
    '16th Century CE', '16-ஆம் நூற்றாண்டு',
    'Preserves oral epics (Mahabharata, Ramayana) and provides community moral storytelling.',
    'மகாபாரதம் மற்றும் இராமாயணக் கதைகளை மக்களுக்குக் கொண்டு சேர்க்கும் வழிமுறை.',
    'Powerful declamation, high-pitched singing, dramatic spins, and audience interaction.',
    'உரத்த வசன உச்சரிப்பு, பாட்டு, சுழன்று ஆடுதல் மற்றும் மக்களுடன் நேரடியாக உரையாடுதல்.',
    'Elaborate wooden shoulder ornaments (Kattakkattai), colourful face paint, and massive headgear (Kireetam).',
    'மார்புக் கவசம், தோள் அணிகள், வண்ணம் தீட்டிய பெரிய கிரீடம் மற்றும் விரிந்த ஆடைகள்.',
    'Mukhaveena, Harmonium, Dholak, Cymbals (Jalra).',
    'முகவீணை, ஹார்மோனியம், டோலக், ஜால்ரா.',
    'PUBLISHED'
) ON CONFLICT (slug) DO NOTHING;

-- Seed Junction Data
INSERT INTO art_form_categories (art_form_id, category_id) VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', (SELECT id FROM categories WHERE name='Martial Arts')),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', (SELECT id FROM categories WHERE name='Folk Dance')),
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', (SELECT id FROM categories WHERE name='Street Theatre'))
ON CONFLICT DO NOTHING;

INSERT INTO art_form_districts (art_form_id, district_id) VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', (SELECT id FROM districts WHERE name='Madurai')),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', (SELECT id FROM districts WHERE name='Thanjavur')),
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', (SELECT id FROM districts WHERE name='Kanchipuram'))
ON CONFLICT DO NOTHING;
