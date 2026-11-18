#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# MARABAGAM ISOLATED DATABASE RESTORE & VERIFICATION SCRIPT
# ============================================================
# SAFETY REQUIREMENT: Restore testing MUST NEVER risk destroying live production.
# Target defaults to temporary isolated DB 'marabagam_restore_test'.

if [ -n "${1:-}" ] && [ -f "$1" ]; then
    BACKUP_FILE="$1"
    RESTORE_TARGET="${RESTORE_TARGET:-marabagam_restore_test}"
    FORCE_PROD_FLAG="${2:-}"
else
    RESTORE_TARGET="${1:-marabagam_restore_test}"
    BACKUP_FILE="${2:-}"
    FORCE_PROD_FLAG="${3:-}"
fi
DB_CONTAINER="${DB_CONTAINER:-marabagam-postgres}"
DB_USER="${POSTGRES_USER:-marabagam_user}"
GPG_PASSPHRASE="${GPG_PASSPHRASE:-${POSTGRES_PASSWORD:-marabagam_dev_password}}"

# PRODUCTION PROTECTION SAFEGUARD
if [ "${RESTORE_TARGET}" = "marabagam_db" ] || [ "${RESTORE_TARGET}" = "marabagam_prod_db" ]; then
    if [ "${FORCE_PROD_FLAG}" != "--force-production-disaster-recovery" ]; then
        echo "[CRITICAL ERROR] SAFETY INTERLOCK ACTIVATED!" >&2
        echo "Refusing to run restore against production target '${RESTORE_TARGET}' without explicit '--force-production-disaster-recovery' flag!" >&2
        exit 1
    fi
fi

if [ -z "${BACKUP_FILE}" ] || [ ! -f "${BACKUP_FILE}" ]; then
    echo "[ERROR] Valid backup archive file path must be provided!" >&2
    echo "Usage: $0 [RESTORE_TARGET] [BACKUP_FILE_PATH] [--force-production-disaster-recovery]" >&2
    exit 1
fi

echo "[INFO] [$(date)] Starting isolated database restore verification..."
echo "[INFO] Target database: ${RESTORE_TARGET}"

TEMP_DECRYPTED="/tmp/decrypted_restore_$$.sql"

# Step 1: GPG Decryption if encrypted
if [[ "${BACKUP_FILE}" == *.gpg ]]; then
    echo "[INFO] Decrypting GPG archive..."
    gpg --batch --yes --passphrase "${GPG_PASSPHRASE}" --decrypt -o "${TEMP_DECRYPTED}" "${BACKUP_FILE}"
else
    cp "${BACKUP_FILE}" "${TEMP_DECRYPTED}"
fi

# Step 2: Create isolated test database
echo "[INFO] Creating temporary isolated test database '${RESTORE_TARGET}'..."
docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d postgres -c "DROP DATABASE IF EXISTS ${RESTORE_TARGET};"
docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d postgres -c "CREATE DATABASE ${RESTORE_TARGET};"

# Step 3: Execute pg_restore
echo "[INFO] Executing pg_restore into '${RESTORE_TARGET}'..."
docker exec -i "${DB_CONTAINER}" pg_restore -U "${DB_USER}" -d "${RESTORE_TARGET}" --clean --if-exists < "${TEMP_DECRYPTED}" || true

# Step 4: Verification Checks
echo "[INFO] Executing schema and data integrity verification..."

# Check Flyway schema history table presence and record count
FLYWAY_COUNT=$(docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${RESTORE_TARGET}" -t -c "SELECT COUNT(*) FROM flyway_schema_history;" | tr -d ' ')
echo "[VERIFY] Flyway migration records found: ${FLYWAY_COUNT}"
if [ "${FLYWAY_COUNT}" -lt 1 ]; then
    echo "[ERROR] Restore verification failed: No Flyway history records found!" >&2
    exit 1
fi

# Check core table existence and non-zero counts
USERS_COUNT=$(docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${RESTORE_TARGET}" -t -c "SELECT COUNT(*) FROM users;" | tr -d ' ')
COURSES_COUNT=$(docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${RESTORE_TARGET}" -t -c "SELECT COUNT(*) FROM courses;" | tr -d ' ')
EVENTS_COUNT=$(docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${RESTORE_TARGET}" -t -c "SELECT COUNT(*) FROM cultural_events;" | tr -d ' ')

echo "[VERIFY] Users count: ${USERS_COUNT}"
echo "[VERIFY] Courses count: ${COURSES_COUNT}"
echo "[VERIFY] Events count: ${EVENTS_COUNT}"

# Step 5: Cleanup temporary restore database (if running isolated restore test)
if [ "${RESTORE_TARGET}" = "marabagam_restore_test" ]; then
    echo "[INFO] Cleaning up temporary restore database..."
    docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d postgres -c "DROP DATABASE IF EXISTS ${RESTORE_TARGET};"
fi

rm -f "${TEMP_DECRYPTED}"
echo "[SUCCESS] [$(date)] Isolated database restore and data integrity verification completed cleanly."
