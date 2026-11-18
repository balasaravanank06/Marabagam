# Marabagam Database Architecture & Schema Documentation

## Database System
- Engine: PostgreSQL 16
- Migration Tool: Flyway

## Executable Migrations Location
Per project architecture rules, the **single canonical location** for executable Flyway SQL migrations is:

`backend/src/main/resources/db/migration/`

## Database Directory Usage
This `database/` directory is reserved for:
- Database architecture diagrams (`database/diagrams/`)
- Data dictionary documentation (`database/docs/`)
- ERD design files

Do NOT place duplicate `.sql` migration files in this directory.
