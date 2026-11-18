-- ============================================================
-- MARABAGAM — PHASE 6 MIGRATION
-- REGIONAL HERITAGE MAPPING & GEOGRAPHIC DISCOVERY
-- ============================================================

-- 1. Extend existing 'districts' table with geographic and bilingual heritage fields
ALTER TABLE districts ADD COLUMN IF NOT EXISTS code VARCHAR(20) UNIQUE;
ALTER TABLE districts ADD COLUMN IF NOT EXISTS latitude NUMERIC(8, 5);
ALTER TABLE districts ADD COLUMN IF NOT EXISTS longitude NUMERIC(8, 5);
ALTER TABLE districts ADD COLUMN IF NOT EXISTS short_history TEXT;
ALTER TABLE districts ADD COLUMN IF NOT EXISTS short_history_ta TEXT;
ALTER TABLE districts ADD COLUMN IF NOT EXISTS cultural_significance TEXT;
ALTER TABLE districts ADD COLUMN IF NOT EXISTS cultural_significance_ta TEXT;
ALTER TABLE districts ADD COLUMN IF NOT EXISTS geojson_feature_code VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_districts_code ON districts(code);
CREATE INDEX IF NOT EXISTS idx_districts_geojson_code ON districts(geojson_feature_code);

-- Seed / Update all 38 Tamil Nadu Districts with Stable District Codes & Centroids
INSERT INTO districts (
    name, name_ta, state, code, latitude, longitude,
    short_history, short_history_ta, cultural_significance, cultural_significance_ta, geojson_feature_code
) VALUES
('Ariyalur', 'அரியலூர்', 'Tamil Nadu', 'TN-ARI', 11.14010, 79.07860, 'Fossil-rich district known for Gangaikonda Cholapuram temple architecture and Chola heritage.', 'கங்கை கொண்ட சோழபுரம் கோவில் மற்றும் சோழர் காலப் பாரம்பரியம் கொண்ட மாவட்டம்.', 'Chola architecture and fossil heritage.', 'சோழர் கட்டிடக்கலை மற்றும் தொல்லியல் சான்றுகள்.', 'TN-ARI'),
('Chengalpattu', 'செங்கல்பட்டு', 'Tamil Nadu', 'TN-CGL', 12.69390, 79.97570, 'Coastal district hosting UNESCO site Mamallapuram stone sculptures and shore temples.', 'மாமல்லபுரச் சிற்பங்கள் மற்றும் கடற்கரைக் கோவில்கள் கொண்ட மாவட்டம்.', 'Pallava rock-cut architecture.', 'பல்லவர் காலக் குடைவரைக் கோவில்கள்.', 'TN-CGL'),
('Chennai', 'சென்னை', 'Tamil Nadu', 'TN-CHE', 13.08270, 80.27070, 'Capital city and international cultural hub for Bharatanatyam and Carnatic music season.', 'பரதநாட்டியம் மற்றும் கர்நாடக இசை மார்கழி விழாவின் உலகளாவிய மையம்.', 'Carnatic music and classical dance.', 'மார்கழி இசை மற்றும் பாரம்பரிய நடனம்.', 'TN-CHE'),
('Coimbatore', 'கோயம்புத்தூர்', 'Tamil Nadu', 'TN-COI', 11.01680, 76.95580, 'Manchester of South India with rich Kongu region folk traditions and handloom weaving.', 'கொங்கு மண்டல நாட்டுப்புற மரபுகள் மற்றும் கைத்தறி நெசவுப் பாரம்பரியம்.', 'Kongu folk arts and weaving.', 'கொங்கு நாட்டுப்புற கலைகள்.', 'TN-COI'),
('Cuddalore', 'கடலூர்', 'Tamil Nadu', 'TN-CUD', 11.74800, 79.77140, 'Coastal heritage center known for Chidambaram Nataraja temple and classical dance postures.', 'சிதம்பரம் நடராஜர் கோவில் மற்றும் பரதக் கலைச் சிற்பங்கள்.', 'Nataraja temple and Karana sculptures.', 'நடராஜர் கோவில் கரணச் சிற்பங்கள்.', 'TN-CUD'),
('Dharmapuri', 'தர்மபுரி', 'Tamil Nadu', 'TN-DHA', 12.12110, 78.15820, 'Ancient Adigaman kingdom land famous for megalithic dolmens and border folk arts.', 'அதியமான் ஆட்சி செய்த தகடூர் பூமி, கற்திட்டைகள் மற்றும் கிராமியக் கலைகள்.', 'Megalithic sites and ancient Adigaman heritage.', 'அதியமான் வரலாறு மற்றும் தொல் எச்சங்கள்.', 'TN-DHA'),
('Dindigul', 'திண்டுக்கல்', 'Tamil Nadu', 'TN-DIN', 10.36730, 77.98030, 'Historic rockfort city known for lockcraft, leather craft, and Sirumalai folk songs.', 'திண்டுக்கல் மலைக்கோட்டை மற்றும் சிறுமலை நாட்டுப்புற பாடல்கள்.', 'Rockfort heritage and agrarian folk songs.', 'மலைக்கோட்டை பாரம்பரியம்.', 'TN-DIN'),
('Erode', 'ஈரோடு', 'Tamil Nadu', 'TN-ERO', 11.34100, 77.71720, 'Kaveri river valley district rich in Bhavani Jamakkalam weaving and Kummi folk dance.', 'பவானி ஜமக்காள நெசவு மற்றும் கும்மியாட்டக் கலைகள்.', 'Bhavani handloom and Kummi dance.', 'பவானி ஜமக்காள நெசவுக் கலை.', 'TN-ERO'),
('Kallakurichi', 'கள்ளக்குறிச்சி', 'Tamil Nadu', 'TN-KAL', 11.73840, 78.96390, 'Kalvarayan hills region rich in tribal folk songs and wood carving.', 'கல்வராயன் மலைப் பகுதி பழங்குடியினர் கலைகள்.', 'Tribal folk music and wood carving.', 'பழங்குடி இசை மற்றும் மரச்சிற்பங்கள்.', 'TN-KAL'),
('Kanchipuram', 'காஞ்சீபுரம்', 'Tamil Nadu', 'TN-KANCH', 12.83420, 79.70360, 'City of thousand temples renowned worldwide for Kanchipuram silk weaving and Pallava art.', 'காஞ்சிப் பட்டு நெசவு மற்றும் பல்லவர் காலக் கோவில் கட்டிடக்கலை.', 'Kanchi silk and temple sculpture.', 'காஞ்சி பட்டு மற்றும் கோவில் சிற்பங்கள்.', 'TN-KANCH'),
('Kanyakumari', 'கன்னியாகுமரி', 'Tamil Nadu', 'TN-KK', 8.08830, 77.53850, 'Southernmost tip of India known for Villuppattu bow song and Kathakali cultural influence.', 'வில்லுப்பாட்டு மற்றும் குமரி நாட்டுப்புற இசை மரபுகள்.', 'Villuppattu (Bow Song) tradition.', 'வில்லுப்பாட்டு நாட்டுப்புற இசை.', 'TN-KK'),
('Karur', 'கரூர்', 'Tamil Nadu', 'TN-KAR', 10.96010, 78.07660, 'Ancient Chera trading hub preserving traditional handloom weaving arts.', 'சேரர் கால வணிக மையம் மற்றும் கைத்தறி நெசவுக் கலை.', 'Handloom textiling tradition.', 'கைத்தறி நெசவுக் கலை.', 'TN-KAR'),
('Krishnagiri', 'கிருஷ்ணகிரி', 'Tamil Nadu', 'TN-KRI', 12.51860, 78.21370, 'Border district known for rock paintings and mixed Kannadiga-Tamil folk theater.', 'பாறை ஓவியங்கள் மற்றும் தெருக்கூத்து நாடகங்கள்.', 'Rock art and border folk theater.', 'பாறை ஓவியங்கள் மற்றும் கிராமிய நாடகம்.', 'TN-KRI'),
('Madurai', 'மதுரை', 'Tamil Nadu', 'TN-MAD', 9.92520, 78.11980, 'Cultural capital of Tamil Nadu, epicenter of Sangam Tamil, Silambam, and Therukoothu.', 'சங்கத் தமிழ் வளர்த்த கூடல் நகர், சிலம்பம் மற்றும் தெருக்கூத்துத் தொட்டில்.', 'Sangam literature and Silambam martial art.', 'சங்க இலக்கியம் மற்றும் சிலம்பக் கலை.', 'TN-MAD'),
('Mayiladuthurai', 'மயிலாடுதுறை', 'Tamil Nadu', 'TN-MAY', 11.10180, 79.65250, 'Kaveri delta town famous for Mayilattam peacock dance and temple music.', 'மயிலாட்டம் மற்றும் காவிரி ஆற்றங்கரை கோவில் கலைகள்.', 'Mayilattam (Peacock Dance).', 'மயிலாட்ட நாட்டுப்புற நடனம்.', 'TN-MAY'),
('Nagapattinam', 'நாகப்பட்டினம்', 'Tamil Nadu', 'TN-NAG', 10.76720, 79.84490, 'Maritime port hub preserving coastal folk singing and Buddhist heritage sculpture.', 'கடலோர நாட்டுப்புற பாடல்கள் மற்றும் சோழர் சிற்பக் கலைகள்.', 'Maritime songs and sculpture.', 'கடலோர நாட்டுப்புற இசை.', 'TN-NAG'),
('Namakkal', 'நாமக்கல்', 'Tamil Nadu', 'TN-NAM', 11.21890, 78.16740, 'Rockfort city known for traditional stone carving and Kolli Hills tribal music.', 'கொல்லிமலை பழங்குடி இசை மற்றும் சிற்பக் கலைகள்.', 'Kolli Hills tribal music.', 'கொல்லிமலை பழங்குடி இசை.', 'TN-NAM'),
('Nilgiris', 'நீலகிரி', 'Tamil Nadu', 'TN-NIL', 11.49160, 76.73370, 'Mountainous district home to Toda embroidery, Badaga dance, and Kota pottery.', 'தோடர் தையற்கலை, படகர் நடனம் மற்றும் கோத்தர் மட்பாண்டக் கலை.', 'Toda embroidery and Badaga dance.', 'தோடர் தையற்கலை மற்றும் படகர் நடனம்.', 'TN-NIL'),
('Perambalur', 'பெரம்பலூர்', 'Tamil Nadu', 'TN-PER', 11.23420, 78.88200, 'Central district preserving folk drumming and traditional terracotta sculpture.', 'சுடுமண் சிற்பக் கலை மற்றும் பறை இசை மரபு.', 'Terracotta sculpture tradition.', 'சுடுமண் சிற்பக் கலை.', 'TN-PER'),
('Pudukkottai', 'புதுக்கோட்டை', 'Tamil Nadu', 'TN-PUD', 10.37970, 78.82080, 'Princely state legacy known for Sittanavasal Jain cave paintings and Kudumiyanmalai music inscription.', 'சித்தன்னவாசல் ஓவியங்கள் மற்றும் குடுமியான்மலை இசைச் சாசனம்.', 'Cave paintings and ancient musical notations.', 'பழங்கால இசைச் சாசனங்கள்.', 'TN-PUD'),
('Ramanathapuram', 'இராமநாதபுரம்', 'Tamil Nadu', 'TN-RAM', 9.36390, 78.83950, 'Maravar kingdom coastal heartland known for Sethupathi maritime history and folk ballads.', 'சேதுபதி மன்னர் வரலாறு மற்றும் கடலோர நாட்டுப்புறக் கதைகள்.', 'Coastal ballads and maritime heritage.', 'கடலோர நாட்டுப்புற பாட்டு.', 'TN-RAM'),
('Ranipet', 'ராணிப்பேட்டை', 'Tamil Nadu', 'TN-RAN', 12.92720, 79.33310, 'Northern district known for leather puppet theater and folk storytelling.', 'தோல் பாவைக் கூத்து மற்றும் தெருக்கூத்து.', 'Leather puppetry (Thol Pavai Koothu).', 'தோல் பாவைக் கூத்து பாரம்பரியம்.', 'TN-RAN'),
('Salem', 'சேலம்', 'Tamil Nadu', 'TN-SAL', 11.66430, 78.14600, 'Mango city preserving Yercaud tribal music and silver filigree handcrafting.', 'சேலம் வெள்ளிக் கொலுசு கைவினை மற்றும் ஏற்காடு பழங்குடி இசை.', 'Silver filigree and tribal music.', 'வெள்ளிக் கைவினை மற்றும் பழங்குடி கலை.', 'TN-SAL'),
('Sivaganga', 'சிவகங்கை', 'Tamil Nadu', 'TN-SIV', 9.84330, 78.48090, 'Chettinad region famous for Chettinad architecture, tile art, and Silambam.', 'செட்டிநாட்டு கட்டிடக்கலை, ஆத்தங்குடி ஓடுகள் மற்றும் சிலம்பம்.', 'Chettinad heritage and Silambam.', 'செட்டிநாட்டு பாரம்பரியம் மற்றும் சிலம்பம்.', 'TN-SIV'),
('Tenkasi', 'தென்காசி', 'Tamil Nadu', 'TN-TEN', 8.95970, 77.31340, 'Western Ghats foothills famous for Courtallam literature and Villuppattu.', 'குற்றாலக் குறவஞ்சி இலக்கியம் மற்றும் வில்லுப்பாட்டு.', 'Courtallam literary arts.', 'குற்றால இலக்கிய மற்றும் நாட்டுப்புற கலைகள்.', 'TN-TEN'),
('Thanjavur', 'தஞ்சாவூர்', 'Tamil Nadu', 'TN-THA', 10.78700, 79.13780, 'Cradle of Chola arts, Brihadeeswarar Temple, Tanjore dolls, and Veena crafting.', 'பிரகதீஸ்வரர் கோவில், தஞ்சாவூர் தட்டு, வீணைத் தயாரிப்பு மற்றும் தஞ்சாவூர் ஓவியங்கள்.', 'Chola bronze, Tanjore painting, and Veena.', 'தஞ்சாவூர் ஓவியம் மற்றும் வீணை கலை.', 'TN-THA'),
('Theni', 'தேனி', 'Tamil Nadu', 'TN-THE', 10.01040, 77.47680, 'Cardamom hills district preserving Oyilattam and agrarian folk songs.', 'ஒயிலாட்டம் மற்றும் உழவர் நாட்டுப்புறப் பாடல்கள்.', 'Oyilattam folk dance.', 'ஒயிலாட்ட கிராமிய நடனம்.', 'TN-THE'),
('Thoothukudi', 'தூத்துக்குடி', 'Tamil Nadu', 'TN-THO', 8.76420, 78.13480, 'Pearl fishery coast known for pearl diving songs, Villuppattu, and seafaring music.', 'முத்துக்குளிப்பு பாடல்கள் மற்றும் கடலோர இசை மரபு.', 'Seafaring folk songs.', 'கடலோர நாட்டுப்புற பாடல்கள்.', 'TN-THO'),
('Tiruchirappalli', 'திருச்சிராப்பள்ளி', 'Tamil Nadu', 'TN-TPJ', 10.79050, 78.70470, 'Heartland of Kaveri region, famous for Rockfort temple and Srirangam temple arts.', 'மலைக்கோட்டை மற்றும் ஸ்ரீரங்கம் கோவில் கலைகளின் மையம்.', 'Carnatic music and temple festivals.', 'ஸ்ரீரங்கம் கோவில் கலைப் பாரம்பரியம்.', 'TN-TPJ'),
('Tirunelveli', 'திருநெல்வேலி', 'Tamil Nadu', 'TN-TIN', 8.71390, 77.75670, 'Halwa city famous for Nellaiappar musical pillars and Thamirabarani folk literature.', 'நெல்லையப்பர் கோவில் இசைத் தூண்கள் மற்றும் தாமிரபரணி நாட்டுப்புறப் பாடல்கள்.', 'Musical pillars and folk poetry.', 'இசைத் தூண்கள் மற்றும் கிராமியக் கலை.', 'TN-TIN'),
('Tirupathur', 'திருப்பத்தூர்', 'Tamil Nadu', 'TN-TPT', 12.49360, 78.56780, 'Javadi hills district preserving tribal music and forest folk songs.', 'ஜவாது மலை பழங்குடியினர் இசை மற்றும் நாட்டுப்புற பாடல்கள்.', 'Tribal music tradition.', 'பழங்குடி இசை மரபு.', 'TN-TPT'),
('Tiruppur', 'திருப்பூர்', 'Tamil Nadu', 'TN-TPR', 11.10850, 77.34110, 'Kongu heartland preserving Kummi dance and traditional textile arts.', 'கொங்கு கும்மியாட்டம் மற்றும் நெசவுக் கலைகள்.', 'Kummi dance tradition.', 'கும்மியாட்ட பாரம்பரியம்.', 'TN-TPR'),
('Tiruvallur', 'திருவள்ளூர்', 'Tamil Nadu', 'TN-TVL', 13.14320, 79.90660, 'Northern border district known for Therukoothu and temple festivals.', 'தெருக்கூத்து நாடகம் மற்றும் கோவில் திருவிழாக்கள்.', 'Therukoothu street drama.', 'தெருக்கூத்து நாடகப் பாராம்பரியம்.', 'TN-TVL'),
('Tiruvannamalai', 'திருவண்ணாமலை', 'Tamil Nadu', 'TN-TVM', 12.22530, 79.07470, 'Spiritual city of Annamalaiyar temple known for Karthigai Deepam festival and Therukoothu troupes.', 'அண்ணாமலையார் கோவில், கார்த்திகை தீபம் மற்றும் தெருக்கூத்துக் குழுக்கள்.', 'Therukoothu and spiritual arts.', 'தெருக்கூத்து மற்றும் பக்தி இலக்கியம்.', 'TN-TVM'),
('Tiruvarur', 'திருவாரூர்', 'Tamil Nadu', 'TN-TVR', 10.77080, 79.63660, 'Birthplace of Carnatic Music Trinity (Thyagaraja, Muthuswami Dikshitar, Syama Sastri).', 'கர்நாடக இசையின் மும்மூர்த்திகள் அவதரித்த புனித பூமி.', 'Carnatic Trinity heritage and Chariot festival.', 'கர்நாடக இசை மும்மூர்த்திகள் பாரம்பரியம்.', 'TN-TVR'),
('Vellore', 'வேலூர்', 'Tamil Nadu', 'TN-VEL', 12.91650, 79.13250, 'Historic fort city known for leather crafting, wood carving, and Kaniyan Koothu.', 'வேலூர் கோட்டை, கணியான் கூத்து மற்றும் மரச்சிற்பக் கலை.', 'Kaniyan Koothu and Fort architecture.', 'கணியான் கூத்து மற்றும் கோட்டைக் கலை.', 'TN-VEL'),
('Viluppuram', 'விழுப்புரம்', 'Tamil Nadu', 'TN-VIL', 11.94010, 79.48610, 'Gingee fort district preserving folk ballads, Therukoothu, and oral epics.', 'செஞ்சிக் கோட்டை, தெருக்கூத்து மற்றும் வாய்மொழி கதையாடல்.', 'Gingee fort history and Therukoothu.', 'செஞ்சிக்கோட்டை வாய்மொழி கதையாடல்.', 'TN-VIL'),
('Virudhunagar', 'விருதுநகர்', 'Tamil Nadu', 'TN-VIR', 9.58720, 77.95800, 'Southern district preserving Karagattam, Kamuthi folk arts, and brass metalwork.', 'கரகாட்டம், கமுதி கிராமியக் கலைகள் மற்றும் பித்தளைக் கைவினை.', 'Karagattam and brass crafting.', 'கரகாட்டம் மற்றும் பித்தளைக் கலை.', 'TN-VIR')
ON CONFLICT (name) DO UPDATE SET
    code = EXCLUDED.code,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    short_history = EXCLUDED.short_history,
    short_history_ta = EXCLUDED.short_history_ta,
    cultural_significance = EXCLUDED.cultural_significance,
    cultural_significance_ta = EXCLUDED.cultural_significance_ta,
    geojson_feature_code = EXCLUDED.geojson_feature_code;

-- 2. Create Table: heritage_locations (Cultural Training Centers & Academies)
CREATE TABLE IF NOT EXISTS heritage_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    name_ta VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    district_id BIGINT NOT NULL REFERENCES districts(id) ON DELETE CASCADE,
    art_form_id UUID REFERENCES art_forms(id) ON DELETE SET NULL,
    location_type VARCHAR(50) NOT NULL, -- 'TRAINING_CENTER', 'CULTURAL_ORGANIZATION', 'HISTORICAL_SITE', 'PERFORMANCE_VENUE'
    description TEXT NOT NULL,
    description_ta TEXT NOT NULL,
    address TEXT NOT NULL,
    address_ta TEXT NOT NULL,
    latitude NUMERIC(8, 5) NOT NULL,
    longitude NUMERIC(8, 5) NOT NULL,
    contact_phone VARCHAR(50),
    contact_email VARCHAR(120),
    website_url VARCHAR(255),
    provenance_source TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- 'DRAFT', 'PENDING_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED'
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    archived_by UUID REFERENCES users(id) ON DELETE SET NULL,
    archived_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_heritage_locations_district ON heritage_locations(district_id);
CREATE INDEX IF NOT EXISTS idx_heritage_locations_art_form ON heritage_locations(art_form_id);
CREATE INDEX IF NOT EXISTS idx_heritage_locations_status ON heritage_locations(status);
CREATE INDEX IF NOT EXISTS idx_heritage_locations_slug ON heritage_locations(slug);
CREATE INDEX IF NOT EXISTS idx_heritage_locations_creator ON heritage_locations(created_by);

-- 3. Create Table: cultural_events (Regional Heritage Festivals & Fairs)
CREATE TABLE IF NOT EXISTS cultural_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    title_ta VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    district_id BIGINT NOT NULL REFERENCES districts(id) ON DELETE CASCADE,
    art_form_id UUID REFERENCES art_forms(id) ON DELETE SET NULL,
    description TEXT NOT NULL,
    description_ta TEXT NOT NULL,
    event_type VARCHAR(50) NOT NULL DEFAULT 'RECURRING_PERIOD', -- 'RECURRING_PERIOD', 'SPECIFIC_DATES', 'DATE_RANGE', 'SEASONAL'
    event_period VARCHAR(100) NOT NULL,
    event_period_ta VARCHAR(100) NOT NULL,
    start_date DATE,
    end_date DATE,
    is_date_approximate BOOLEAN NOT NULL DEFAULT TRUE,
    venue_name VARCHAR(200),
    venue_name_ta VARCHAR(200),
    latitude NUMERIC(8, 5),
    longitude NUMERIC(8, 5),
    provenance_source TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- 'DRAFT', 'PENDING_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED'
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    archived_by UUID REFERENCES users(id) ON DELETE SET NULL,
    archived_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cultural_events_district ON cultural_events(district_id);
CREATE INDEX IF NOT EXISTS idx_cultural_events_art_form ON cultural_events(art_form_id);
CREATE INDEX IF NOT EXISTS idx_cultural_events_status ON cultural_events(status);
CREATE INDEX IF NOT EXISTS idx_cultural_events_creator ON cultural_events(created_by);

-- ============================================================
-- DEVELOPMENT SEED DATA (EXPLICITLY MARKED FOR CULTURAL REVIEW)
-- ============================================================

-- Seed Representative Heritage Location: Madurai Silambam Gurukulam (PUBLISHED)
INSERT INTO heritage_locations (
    id, name, name_ta, slug, district_id, art_form_id, location_type,
    description, description_ta, address, address_ta, latitude, longitude,
    contact_phone, contact_email, website_url, provenance_source, status, created_by
) VALUES (
    'f8e7d6c5-b4a3-2109-8765-43210fedcba9',
    'Madurai Traditional Silambam Academy',
    'மதுரை பாரம்பரிய சிலம்பக் கழகம்',
    'madurai-silambam-academy',
    (SELECT id FROM districts WHERE name = 'Madurai'),
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', -- Silambam UUID from V3
    'TRAINING_CENTER',
    ' [DEVELOPMENT SEED — PENDING CULTURAL REVIEW] Traditional staff fencing academy offering authentic Nagam-16 and Kallapathu posture training under certified masters.',
    ' [DEVELOPMENT SEED — PENDING CULTURAL REVIEW] ஆசான்கள் வழிகாட்டுதலில் நாகம்-16 மற்றும் கள்ளப்பத்து முறைகளில் தடிவரிசைப் பயிற்சி அளிக்கும் பாரம்பரிய மையம்.',
    'Tallakulam Main Road, Madurai',
    'தல்லாகுளம் முதன்மைச் சாலை, மதுரை',
    9.92520,
    78.11980,
    '+91 452 2530000',
    'contact@maduraisilambam.org',
    'https://maduraisilambam.org',
    'Development Reference Seed Data — Tamil Traditional Sports Federation Directory 2025',
    'PUBLISHED',
    COALESCE((SELECT id FROM users WHERE username = 'admin' OR username = 'admin_seed' OR email = 'admin@marabagam.org' LIMIT 1), '00000000-0000-0000-0000-000000000001'::uuid)
) ON CONFLICT (slug) DO NOTHING;

-- Seed Representative Cultural Event: Chithirai Silambam Festival (PUBLISHED)
INSERT INTO cultural_events (
    id, title, title_ta, slug, district_id, art_form_id, description, description_ta,
    event_type, event_period, event_period_ta, venue_name, venue_name_ta, latitude, longitude,
    provenance_source, status, created_by
) VALUES (
    'e1e2e3e4-e5e6-7e8e-9e0e-1e2e3e4e5e6e',
    'Chithirai Traditional Martial Arts Festival',
    'சித்திரைத் திருவிழா பாரம்பரிய தற்காப்புக் கலை நிகழ்ச்சி',
    'chithirai-silambam-festival',
    (SELECT id FROM districts WHERE name = 'Madurai'),
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    ' [DEVELOPMENT SEED — PENDING CULTURAL REVIEW] Annual cultural assembly showcasing stick fencing, sword play, and fire staff demonstrations during the Chithirai festival month.',
    ' [DEVELOPMENT SEED — PENDING CULTURAL REVIEW] சித்திரைத் திருவிழாவின் போது நடைபெறும் சிலம்பம், வாள்வீச்சு மற்றும் தீப்பந்தக் கலைக் காட்சி.',
    'RECURRING_PERIOD',
    'April - May (Chithirai)',
    'ஏப்ரல் - மே (சித்திரை)',
    'Tamukkam Ground, Madurai',
    'தமுக்கம் மைதானம், மதுரை',
    9.92520,
    78.11980,
    'Development Reference Seed Data — Tamil Nadu Tourism Annual Event Calendar 2025',
    'PUBLISHED',
    COALESCE((SELECT id FROM users WHERE username = 'admin' OR username = 'admin_seed' OR email = 'admin@marabagam.org' LIMIT 1), '00000000-0000-0000-0000-000000000001'::uuid)
) ON CONFLICT (slug) DO NOTHING;
