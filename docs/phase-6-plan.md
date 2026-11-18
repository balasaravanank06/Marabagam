# Phase 6 Implementation Plan — Regional Heritage Mapping & Geographic Discovery

**Project**: Marabagam — The Home of Tamil Heritage  
**Module**: Phase 6 — Regional Heritage Mapping & Geographic Discovery  
**Status**: **READY FOR REVIEW**  
**Date**: September 7, 2026  
**Document Version**: 2.0.0 (Amended per External Review)  

---

## 1. Executive Summary & Objective

Phase 6 introduces **Regional Heritage Mapping & Geographic Discovery** to Marabagam. This phase enables learners, researchers, and cultural enthusiasts to explore Tamil Nadu's traditional arts through an interactive, discovery-driven geographic interface.

The primary objective is to transform regional data into an active discovery layer that bridges physical locations with existing cultural documentation, structured e-learning courses, artists, and regional cultural events without duplicating existing entities, inventing unverified cultural facts, or degrading security controls.

### Key Capabilities
1. **Interactive Tamil Nadu Heritage Map**: Visual discovery interface rendering all 38 districts of Tamil Nadu with high-performance boundary overlays and marker clusters.
2. **Geographic Art Form Discovery**: Ability to explore verified art forms originating from or practiced within specific districts (e.g., Silambam in Madurai, Bharatanatyam in Thanjavur, Karakattam in Tirunelveli).
3. **Reverse Geographic Lookup**: Ability to inspect an art form and discover its spatial distribution across Tamil Nadu districts.
4. **Cultural Organizations & Training Centers**: Curated, public discovery of verified heritage schools, academies, and performance venues associated with specific districts and art forms.
5. **Flexible Regional Festivals & Events**: Discovery of traditional cultural festivals, annual heritage fairs, and seasonal celebrations supporting both recurring seasonal periods and exact calendar dates.
6. **Accessible List View Alternative**: Full non-map tabular and card discovery view providing 100% feature parity for screen readers, keyboard-only users, and mobile devices.

---

## 2. Scope & Strict Boundaries

### In-Scope (Phase 6)
- **Database Extension**: Extending the existing `districts` table (`V3`) via Flyway migration `V6__regional_heritage_mapping.sql` with geographic centroids, bilingual descriptions, and spatial attributes.
- **New Curated Domain Entities**: Introducing `heritage_locations` (training centers/academies) and `cultural_events` (traditional festivals) linked directly to existing `districts` and `art_forms`.
- **Backend Map APIs**: Spring Boot REST endpoints (`/api/v1/heritage-map`, `/api/v1/districts/**`, `/api/v1/art-forms/{slug}/districts`) with DTO projections, caching, and spatial parameter validation.
- **Frontend Interactive Map**: React + Leaflet map interface (`/heritage-map`) with GeoJSON district boundary polygons, marker clustering, hover tooltips, and interactive sidebar panels.
- **Accessible Non-Map View**: Keyboard-navigable, screen-reader-compliant filterable list interface.
- **Bilingual Localization**: Full English and Unicode Tamil (`ta`) support for district names, histories, location details, and UI controls.
- **Content Governance & Ownership**: Multi-role workflow (`DRAFT` → `PENDING_REVIEW` → `APPROVED` → `PUBLISHED` → `ARCHIVED`) with strict creator ownership enforcement and cross-contributor IDOR protection.
- **Automated Verification**: Backend JUnit 5 tests, cross-contributor ownership tests, GeoJSON integrity validation tests, Frontend Vitest tests, TypeScript validation, and Docker Compose runtime health checks.

### Explicitly Out-of-Scope (Deferred to Future Phases)
The following features are **STRICTLY EXCLUDED** from Phase 6 and must **NOT** be implemented:
- ❌ AI Movement Coach & MediaPipe pose estimation
- ❌ Computer vision / pose tracking
- ❌ Augmented Reality (AR) / Virtual Reality (VR) views
- ❌ Quizzes, exams, or automated testing modules
- ❌ Gamification, points, badges, or leaderboards
- ❌ Certificates or digital credential issuance
- ❌ Automated ML-driven recommendations engine
- ❌ Voice assistant or speech recognition
- ❌ Native mobile application code (iOS/Android)
- ❌ Custom ML model training or LLM fine-tuning
- ❌ Autonomous web scraping or unverified automated data ingestion
- ❌ Private artist residential mapping or personal address tracking

---

## 3. Existing Architecture & System Integration

Phase 6 integrates seamlessly into Marabagam's approved multi-tier decoupled architecture:

```
[ React 18 + TypeScript + Vite + Tailwind CSS + Leaflet ]
                          │
                          ▼ (HTTP / REST + JWT Bearer)
[ Spring Boot 3.3.3 + Java 21 Backend Layer ]
  ├── SecurityConfig (JWT Bearer Auth & Role Checks)
  ├── Public / Authenticated Map Controllers (/api/v1/districts, /api/v1/heritage-map)
  ├── Content Governance & Creator Ownership Validation
  └── AuditAspect (SECURITY_AUDIT Logger)
                          │
                          ▼ (SQL / JPA / Hibernate)
[ PostgreSQL 16 + Flyway Migrations (V1–V6) ]
  ├── districts (Extended in V6 with lat/lng & metadata)
  ├── art_form_districts (Phase 3 relationship)
  ├── heritage_locations (New in V6: Training centers / academies)
  └── cultural_events (New in V6: Festivals / regional events)
```

### Integration Points with Existing Phases
- **Phase 1 (Foundation)**: Reuses `ApiResponse<T>`, global error handlers, and UTF-8 response encoding.
- **Phase 2 (Security)**: Reuses JWT Bearer authentication, Spring Security filters, `RateLimitingFilter`, and 6-role RBAC matrix.
- **Phase 3 (Art Form Directory)**: Reuses `districts` entity, `art_forms` entity, and `art_form_districts` many-to-many mapping.
- **Phase 4 (E-Learning)**: Links district heritage cards to published e-learning `courses` and `lessons`.
- **Phase 5 (AI Cultural Guide / RAG)**: Leverages `knowledge_documents` attached to specific districts (`district_id` introduced in V5) to contextualize AI responses.

---

## 4. Current Schema Reuse & Entity Audit

### Inspection of Existing Phase 3 Schema
Phase 3 (`V3__art_form_directory.sql`) introduced the foundational geographic table:
```sql
CREATE TABLE IF NOT EXISTS districts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    name_ta VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL DEFAULT 'Tamil Nadu',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS art_form_districts (
    art_form_id UUID NOT NULL REFERENCES art_forms(id) ON DELETE CASCADE,
    district_id BIGINT NOT NULL REFERENCES districts(id) ON DELETE CASCADE,
    PRIMARY KEY (art_form_id, district_id)
);
```

### Mandated Schema Reuse Principles
1. **No Duplicate District Table**: Creating a secondary `regional_districts` or `map_districts` table is **STRICTLY PROHIBITED**. The existing `districts` table is the single source of truth for all 38 districts of Tamil Nadu.
2. **Reusing `art_form_districts`**: The existing join table `art_form_districts` already models the many-to-many relationship between traditional art forms and their native/documented districts. Phase 6 map endpoints will query this existing join table directly via JPA entity relationships rather than duplicating relationships.
3. **Backward Migration Compatibility**: Historical migrations (`V1` through `V5`) will remain **UNTOUCHED**. All Phase 6 schema extensions will be executed strictly inside `V6__regional_heritage_mapping.sql`.

---

## 5. Proposed Database Design (`V6__regional_heritage_mapping.sql`)

### Schema Alterations & New Tables

```
                               ┌───────────────────┐
                               │     districts     │
                               │  (Extended V6)    │
                               └─────────┬─────────┘
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        │ 1:N                            │ 1:N                            │ N:M (art_form_districts)
        ▼                                ▼                                ▼
┌───────────────┐               ┌─────────────────┐               ┌───────────────┐
│heritage_locs  │               │ cultural_events │               │   art_forms   │
│(Centres/Hubs) │               │   (Festivals)   │               │   (Phase 3)   │
└───────────────┘               └─────────────────┘               └───────────────┘
```

#### 1. Alter Table `districts` (Extension)
Add spatial centroids and bilingual rich metadata to the existing `districts` table:
```sql
ALTER TABLE districts
    ADD COLUMN IF NOT EXISTS code VARCHAR(50) UNIQUE,
    ADD COLUMN IF NOT EXISTS latitude NUMERIC(8, 5),
    ADD COLUMN IF NOT EXISTS longitude NUMERIC(8, 5),
    ADD COLUMN IF NOT EXISTS short_history TEXT,
    ADD COLUMN IF NOT EXISTS short_history_ta TEXT,
    ADD COLUMN IF NOT EXISTS cultural_significance TEXT,
    ADD COLUMN IF NOT EXISTS cultural_significance_ta TEXT,
    ADD COLUMN IF NOT EXISTS geojson_feature_code VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_districts_code ON districts(code);
```

#### 2. Create Table `heritage_locations` (Cultural Training Centers & Academies)
Stores verified public institutions, traditional *Gurukulam* academies, municipal cultural centers, and historic performance grounds. Includes creator ownership tracking and archival lifecycle support.
```sql
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
    provenance_source TEXT, -- Source attribution/citation
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
```

#### 3. Create Table `cultural_events` (Regional Heritage Festivals & Fairs)
Stores traditional cultural festivals, temple art fairs, and annual heritage events supporting both recurring seasonal periods and exact calendar dates.
```sql
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
    event_period VARCHAR(100) NOT NULL, -- e.g., 'January (Thai)', 'December-January (Margazhi)'
    event_period_ta VARCHAR(100) NOT NULL,
    start_date DATE, -- Optional exact start date if known
    end_date DATE,   -- Optional exact end date if known
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
```

---

## 6. Cultural Data Verification & Provenance

### Strict Governance over Cultural Statements
1. **Curated Relationships Only**: Art-form to district relationships must reflect verified, documented cultural traditions (e.g., Silambam in Madurai, Bharatanatyam in Thanjavur, Villuppattu in Tirunelveli). Unverified associations must **NOT** be created merely to make the map visually uniform.
2. **Provenance Metadata**: `heritage_locations` and `cultural_events` entities include a mandatory `provenance_source` text field recording the documentary, academic, or expert source supporting the entry.
3. **Explicit Seed Data Marking**: Seed data inserted for initial development/testing that has not completed formal Cultural Expert review must be explicitly flagged in description/provenance metadata as `[DEVELOPMENT SEED — PENDING CULTURAL REVIEW]`.

---

## 7. Geographic Strategy, Map Technology & OSM Attribution

### Map Technology Justification (Qualitative)
- **Selected Library**: **React + Leaflet** (`leaflet`, `react-leaflet`).
- **Technical Justification**:
  - **Open Source**: Permissive BSD-2-Clause license with zero API metering or commercial usage restrictions.
  - **Native React Integration**: Mature ecosystem (`react-leaflet`) wrapping Leaflet components as declarative React hooks and elements.
  - **Native GeoJSON Support**: Built-in SVG vector rendering for polygon boundaries, tooltips, and interactive click events.
  - **Lightweight Framework**: Substantially lower footprint and memory usage compared to heavy WebGL rendering SDKs, making it highly suitable for low-power mobile browsers and modest hardware.
  - **Zero Secret Key Dependency**: Eliminates risks associated with exposed vendor API keys, billing limits, or unexpected cloud map costs.

### District ↔ GeoJSON Integrity Mapping
To prevent hidden coupling between arbitrary frontend files and database IDs:
1. **Stable Identifier Mapping**: The backend exposes `district.code` (e.g., `TN-MAD`, `TN-TAN`, `TN-TIN`). The GeoJSON feature collection uses identical property keys: `feature.properties.code == district.code`.
2. **Integrity Validation Rules**:
   - Backend/frontend startup validation verifies that **every** one of the 38 Tamil Nadu districts in the database maps to **exactly one** matching GeoJSON polygon feature.
   - **No Duplicate Codes**: Duplicate polygon codes fail validation.
   - **No Unknown Polygon Codes**: GeoJSON features with codes missing from the `districts` database table fail validation.
   - **Validation Failure Behavior**: If a code mismatch occurs, the application logs a strict validation error and disables affected boundary polygons rather than rendering misaligned or deceptive geographic information.

### Offline Capability & Basemap Wording
- **Offline Operational Policy**:
  > *"The application remains fully functional without external map tiles by using locally stored GeoJSON boundaries and the accessible list view. External basemap tiles are optional."*
- **Tile Usage Protocol**: OpenStreetMap raster tiles (`https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png`) are fetched dynamically over standard HTTP/HTTPS. No unauthorized bulk tile scraping, offline tile caching servers, or policy-violating pre-downloads will be implemented.

### OpenStreetMap Attribution
When OpenStreetMap basemap tiles are rendered, the Leaflet map container must display standard mandatory attribution:
```html
Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors
```

---

## 8. Location Model & Privacy Protection

### Definition by Location Type
1. **District Centroids**: Stored in `districts.latitude` and `districts.longitude` using standard geographic centroid coordinates representing the approximate geographical center of the district.
2. **Public Organizations, Academies & Historical Sites**: Stored in `heritage_locations` when public institutional coordinates are legitimately available from official public directories, municipal records, or verified organization consent.
3. **Private Artist / Instructor Personal Residential Addresses**:
   - **STRICT PROHIBITION**: Individual artists' or instructors' personal residential addresses, private home coordinates, or personal living locations **MUST NEVER** be stored, collected, or displayed on the map.
   - Professional institutional affiliations (e.g., a public music college or registered academy where an instructor teaches) may be mapped only with official organizational details.

---

## 9. REST API Specifications (`/api/v1/...`)

### Strict Public Status Semantics
- **PUBLIC APIs** (`permitAll()`): Return **ONLY** records with `status = 'PUBLISHED'`. Records in `DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`, or `ARCHIVED` status are strictly hidden. `APPROVED` records do **NOT** automatically become publicly visible until an Admin explicitly sets status to `PUBLISHED`.
- **ADMIN / CULTURAL EXPERT APIs**: May query non-published lifecycle states according to RBAC.

### Endpoints Summary

#### 1. `GET /api/v1/heritage-map`
- **Auth**: Public (`permitAll()`).
- **Semantics**: Returns aggregated district summaries and published marker counts. Filters: `status = 'PUBLISHED'`.

#### 2. `GET /api/v1/districts`
- **Auth**: Public (`permitAll()`).
- **Semantics**: Returns paginated district summaries (`id`, `name`, `nameTa`, `code`, `latitude`, `longitude`).

#### 3. `GET /api/v1/districts/{id}`
- **Auth**: Public (`permitAll()`).
- **Semantics**: Returns district profile including associated `PUBLISHED` art forms, `PUBLISHED` training centers, and `PUBLISHED` cultural events.

#### 4. `GET /api/v1/art-forms/{slug}/districts`
- **Auth**: Public (`permitAll()`).
- **Semantics**: Returns list of districts associated with the requested art form via `art_form_districts`.

#### 5. `POST /api/v1/heritage-locations`
- **Auth**: Authenticated (`ARTIST`, `INSTRUCTOR`, `CULTURAL_EXPERT`, `ADMIN`).
- **Behavior**: Sets `created_by = current_user_id` and initial `status = 'DRAFT'`.

#### 6. `PUT /api/v1/heritage-locations/{id}`
- **Auth**: Authenticated.
- **Creator Ownership Rule**: `ARTIST` or `INSTRUCTOR` can edit **ONLY** drafts where `created_by == current_user_id`. Attempting to edit another user's draft returns HTTP `403 Forbidden`.

#### 7. `PUT /api/v1/heritage-locations/{id}/status`
- **Auth**: Restricted (`CULTURAL_EXPERT`, `ADMIN`).
- **Behavior**: Allows status transitions (`PENDING_REVIEW` → `APPROVED` / `REJECTED`, `APPROVED` → `PUBLISHED`).

#### 8. `PUT /api/v1/heritage-locations/{id}/archive`
- **Auth**: Restricted (`CULTURAL_EXPERT`, `ADMIN`).
- **Behavior**: Sets `status = 'ARCHIVED'`, `archived_by = current_user_id`, `archived_at = CURRENT_TIMESTAMP`.

---

## 10. Creator Ownership & Server-Side RBAC Rules

### Explicit Governance Matrix

| Role | Create Draft | Edit Own Draft | Edit Other's Draft | Submit for Review | Cultural Approve/Reject | Publish | Archive | Delete (Hard) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **GUEST** | ❌ (401) | ❌ (401) | ❌ (401) | ❌ (401) | ❌ (401) | ❌ (401) | ❌ (401) | ❌ (401) |
| **LEARNER** | ❌ (403) | ❌ (403) | ❌ (403) | ❌ (403) | ❌ (403) | ❌ (403) | ❌ (403) | ❌ (403) |
| **ARTIST / INSTRUCTOR** | ✅ | ✅ (Own) | ❌ (403 IDOR) | ✅ (Own) | ❌ (403) | ❌ (403) | ❌ (403) | ❌ (403) |
| **CULTURAL_EXPERT** | ✅ | ✅ (Own) | ❌ (403) | ✅ | ✅ | ❌ (403) | ✅ | ❌ (403) |
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

### Server-Side IDOR Enforcement
When an `ARTIST` or `INSTRUCTOR` issues `PUT /api/v1/heritage-locations/{id}`, the service layer performs an explicit ownership check:
```java
if (!location.getCreatedBy().equals(currentUser.getId()) && !currentUser.isAdmin()) {
    throw new AccessDeniedException("You do not have permission to modify this location proposal.");
}
```

---

## 11. Archival & Deletion Policy

Published heritage records contain historical value and should **NOT** be physically deleted under standard operation.

### Lifecycle Policy
`DRAFT` $\rightarrow$ `PENDING_REVIEW` $\rightarrow$ `APPROVED` $\rightarrow$ `PUBLISHED` $\rightarrow$ `ARCHIVED`

1. **Archival (`ARCHIVED`)**:
   - Authorized Roles: `CULTURAL_EXPERT`, `ADMIN`.
   - Behavior: Sets `status = 'ARCHIVED'`, removing the item from public API responses (`PUBLISHED`-only filter). The record remains in PostgreSQL for audit and historical reference.
   - Restoration: An `ADMIN` may restore an `ARCHIVED` record back to `APPROVED` or `PUBLISHED`.
2. **Physical Hard Deletion**:
   - Authorized Roles: `ADMIN` only.
   - Usage: Reserved for accidental duplicate entries or privacy compliance requests.
3. **Audit Logging**: All archival, restoration, and hard deletion actions are audited via `SECURITY_AUDIT` logger (`EVENT=HERITAGE_LOCATION_ARCHIVED`, `EVENT=HERITAGE_LOCATION_DELETED`).

---

## 12. Flexible Cultural Event Date Model

Cultural events exhibit diverse timing patterns (e.g., annual Tamil month festivals vs. specific dated symposia). The schema avoids fabricating arbitrary calendar dates when only traditional periods are known.

### Event Type Classification
- `RECURRING_PERIOD`: Traditional events tied to Tamil months/seasons (e.g., *"Margazhi Music Season (Dec-Jan)"*, *"Thai Pongal Festivals (January)"*). Exact `start_date` and `end_date` remain NULL; `event_period` holds descriptive period.
- `SPECIFIC_DATES`: Fixed single-day events. `start_date` populated; `is_date_approximate = false`.
- `DATE_RANGE`: Events spanning exact start and end dates. `start_date` and `end_date` populated.
- `SEASONAL`: Broad seasonal art celebrations where exact calendar dates vary annually.

---

## 13. Frontend UI/UX & Accessible List Alternative

### Dual-View Interface (`/heritage-map`)
```
 ┌────────────────────────────────────────────────────────────────────────┐
 │ 🗺️ Tamil Nadu Heritage Discovery                                       │
 │ [ View Mode: ( 🗺️ Map View | 📋 Accessible List View ) ]               │
 ├───────────────────────────────────┬────────────────────────────────────┤
 │ MAP VIEW MODE                     │ ACCESSIBLE LIST VIEW MODE          │
 │ Interactive Leaflet Container     │ Search & Filter Controls           │
 │ - 38 District GeoJSON Polygons    │ Tabular / Card Grid of 38 Districts│
 │ - OSM Basemap Tiles + Attribution │ ARIA-labeled Keyboard Navigation   │
 │ - Heritage Location Clusters      │ Art Form Badges & Event Lists      │
 └───────────────────────────────────┴────────────────────────────────────┘
```

- **WCAG 2.1 AA Accessibility**: List View provides 100% equivalent discovery functionality for keyboard users and screen readers (`role="region"`, `aria-label="District Heritage Directory"`).

---

## 14. Bilingual Localization Strategy

- **Database Fields**: Dual columns (`name` / `name_ta`, `description` / `description_ta`, `short_history` / `short_history_ta`).
- **Frontend Locales**: `en.json` and `ta.json` translation keys.
- **Tamil Unicode**: Preserves Tamil characters (`U+0B80` to `U+0BFF`) across REST API JSON payloads, Spring Boot controllers, and PostgreSQL database UTF-8 storage.

---

## 15. Performance Benchmark Specification

### Response Time Benchmark Definition
- **Target Benchmark**: `<200ms`
- **Definition & Scope**: Measured as the **server-side processing execution duration** at the Spring Boot application layer for public map endpoints (`GET /api/v1/heritage-map`, `GET /api/v1/districts`), **excluding** client Internet latency, DNS lookup, or browser rendering time.
- **Test Environment & Workload**: Evaluated under a standard local container/host development environment (Spring Boot + PostgreSQL 16 on Docker) with a representative dataset of 38 Tamil Nadu districts, ~100 indexed art form relationships, and ~50 published heritage locations.
- **Measurement Method**: Tracked using Spring Boot Micrometer `@Timed` metrics and server log execution timestamps.

---

## 16. Comprehensive Testing Strategy

### Automated Test Coverage
1. **Cross-Contributor Ownership / IDOR Tests**:
   - `ARTIST_A` cannot edit `ARTIST_B`'s draft location (`403 Forbidden`).
   - `ARTIST_A` can edit own draft location (`200 OK`).
2. **Public Status Semantics Tests**:
   - Public GET `/api/v1/districts/{id}` returns ONLY `PUBLISHED` locations and events.
   - `APPROVED` locations NOT yet `PUBLISHED` are absent from public response.
3. **GeoJSON District Integrity Tests**:
   - Unit test verifying all 38 database district codes match GeoJSON feature properties 1-to-1 without orphans or duplicates.
4. **Coordinate Bounding Box Tests**:
   - Out-of-bounds latitude/longitude submissions rejected with `400 Bad Request`.
5. **Archival Behavior Tests**:
   - `ARCHIVED` locations removed from public discovery API response.
6. **Frontend Tests (Vitest)**:
   - Map rendering fallback, List View toggle, bilingual switching, ARIA labels.

---

## 17. Observability & Audit Logging

Structured events logged to `SECURITY_AUDIT`:
- `EVENT=HERITAGE_LOCATION_PROPOSED LOCATION_ID={} CREATED_BY={}`
- `EVENT=HERITAGE_LOCATION_STATUS_CHANGED LOCATION_ID={} NEW_STATUS={} BY_USER_ID={}`
- `EVENT=HERITAGE_LOCATION_ARCHIVED LOCATION_ID={} ARCHIVED_BY={}`
- `EVENT=HERITAGE_LOCATION_DELETED LOCATION_ID={} DELETED_BY={}`
- `EVENT=CROSS_CONTRIBUTOR_EDIT_BLOCKED LOCATION_ID={} ATTEMPTED_BY={}`

---

## 18. Docker & Infrastructure Integration

Phase 6 requires **ZERO** changes to the existing Docker topology:
- `marabagam-postgres` (PostgreSQL 16 + pgvector)
- `marabagam-backend` (Spring Boot 3.3.3)
- `marabagam-ai-service` (FastAPI)
- `marabagam-frontend` (React + Nginx)

Flyway automatically applies `V6__regional_heritage_mapping.sql` on backend container startup.

---

## 19. Development Reference Data & Provenance Disclaimers

Development seed data included in `V6` for testing and initial UI verification that has not completed formal Cultural Expert review will be explicitly tagged:
- `provenance_source`: `"Development Reference Seed Data"`
- `description`: `" [DEVELOPMENT SEED — PENDING CULTURAL REVIEW] ..."`

Unverified cultural associations will **NOT** be inserted into seed migrations.

---

## 20. Risk Analysis & Mitigation Matrix

| Identified Risk | Risk Severity | Proposed Mitigation Strategy |
| :--- | :---: | :--- |
| **Cultural Misinformation** | High | Multi-role governance (`DRAFT` $\rightarrow$ `APPROVED` $\rightarrow$ `PUBLISHED`), mandatory provenance tracking, explicit tagging of unreviewed seed data. |
| **Incorrect District/Art-Form Relationships** | High | Reusing verified Phase 3 `art_form_districts` join table; prohibiting unverified map-filling entries. |
| **GeoJSON District Code Mismatch** | Medium | Automated startup validation enforcing 1-to-1 matching between database `district.code` and GeoJSON feature codes. Mismatches disable affected polygon rendering safely. |
| **External Basemap Tile Limits/Downtime** | Low | Application operates fully offline using local GeoJSON boundaries and Accessible List View. OpenStreetMap attribution and usage policy strictly followed. |
| **Private Location Privacy Leak** | Critical | Strict data model prohibition blocking residential address entries; point markers permitted only for verified public institutions/academies. |
| **Cross-Contributor Draft IDOR** | High | Server-side ownership validation (`created_by == current_user_id`) enforced in service layer; comprehensive JUnit tests for IDOR isolation. |
| **Stale Geographic Data** | Medium | Archival lifecycle (`ARCHIVED` status) preserves historical context while removing obsolete records from public discovery. |
| **Fabricated Event Dates** | Medium | Flexible event date schema (`event_type`: `RECURRING_PERIOD`, `SEASONAL`, etc.) allowing period descriptions without fake calendar dates. |

---

## 21. Measurable Phase 6 Acceptance Criteria

- [ ] Verified/curated cultural data used for all district/art-form associations; unreviewed seed data explicitly flagged.
- [ ] GeoJSON district code integrity validation verified 1-to-1 against database `districts.code`.
- [ ] Cross-contributor ownership enforced; IDOR unit test confirms `ARTIST_A` cannot edit `ARTIST_B`'s draft (`403 Forbidden`).
- [ ] Public APIs return **ONLY** `PUBLISHED` records; `APPROVED` items remain hidden until published.
- [ ] Residential artist addresses strictly excluded; public institutional coordinates validated against Tamil Nadu bounding box.
- [ ] Archival workflow (`ARCHIVED` status) verified; destructive actions audited in `SECURITY_AUDIT`.
- [ ] OpenStreetMap attribution displayed on basemap; offline mode verified using local GeoJSON and List View.
- [ ] Cultural event date model handles recurring seasonal periods without fake dates.
- [ ] Server-side backend processing benchmark (<200ms) verified on local test workload.
- [ ] All pre-existing Phase 1–5 backend (**36/36**), AI (**8/8**), and frontend (**11/11**) tests remain 100% **GREEN**.
- [ ] TypeScript `npx tsc --noEmit` and Vite build pass with 0 errors.

---

## 22. Staged Implementation Sequence

```
Stage 1: Existing Schema Audit & GeoJSON Boundary Asset Assembly
   ↓
Stage 2: Database Migration Script V6__regional_heritage_mapping.sql
   ↓
Stage 3: Spring Boot Domain Entities (District extension, HeritageLocation, CulturalEvent)
   ↓
Stage 4: Creator Ownership & Governance Service Layer (IDOR check & Archival handling)
   ↓
Stage 5: REST Controllers & Public Status Filtering (/api/v1/districts, /api/v1/heritage-map)
   ↓
Stage 6: Leaflet Map Integration & OSM Attribution (react-leaflet, GeoJSON Layer)
   ↓
Stage 7: Dual-View Frontend UI (/heritage-map, DistrictSidebar, AccessibleListView)
   ↓
Stage 8: Bilingual Localization (i18n en/ta translations & Tamil Unicode testing)
   ↓
Stage 9: Automated Unit, IDOR, GeoJSON Integrity, & Security Test Suites
   ↓
Stage 10: Docker Compose Runtime Verification & Final Phase 6 Report
```

---

## 23. Status Statement

### PHASE 6 PLAN STATUS

**READY FOR REVIEW**

This amended technical implementation plan is complete, fully specified, and awaiting external review and approval before any implementation begins.
