#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# MARABAGAM POSTGRESQL ENCRYPTED LOGICAL BACKUP SCRIPT
# ============================================================
# Strategy: pg_dump custom archive format + GPG AES-256 encryption + S3 upload
# Note: This is a logical backup, not Point-In-Time Recovery (PITR).

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/tmp/marabagam_backups"
BACKUP_FILE="${BACKUP_DIR}/marabagam_db_${BACKUP_DATE}.sql.gz"
ENCRYPTED_FILE="${BACKUP_FILE}.gpg"
S3_BUCKET="${STORAGE_S3_BUCKET:-marabagam-backups}"
DB_CONTAINER="${DB_CONTAINER:-marabagam-postgres}"
DB_USER="${POSTGRES_USER:-marabagam_user}"
DB_NAME="${POSTGRES_DB:-marabagam_prod_db}"
GPG_PASSPHRASE="${GPG_PASSPHRASE:-${POSTGRES_PASSWORD:-marabagam_dev_password}}"

mkdir -p "${BACKUP_DIR}"

echo "[INFO] [$(date)] Starting Marabagam PostgreSQL encrypted logical backup..."

# Step 1: Execute pg_dump
if docker exec "${DB_CONTAINER}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" -Fc -Z 9 > "${BACKUP_FILE}"; then
    echo "[INFO] pg_dump completed successfully: ${BACKUP_FILE}"
else
    echo "[ERROR] pg_dump execution failed!" >&2
    exit 1
fi

# Step 2: GPG AES-256 Encryption
if gpg --batch --yes --passphrase "${GPG_PASSPHRASE}" --symmetric --cipher-algo AES256 -o "${ENCRYPTED_FILE}" "${BACKUP_FILE}"; then
    echo "[INFO] GPG encryption completed: ${ENCRYPTED_FILE}"
    rm -f "${BACKUP_FILE}"
else
    echo "[ERROR] GPG encryption failed!" >&2
    exit 1
fi

# Step 3: Compute SHA256 Checksum for Integrity Verification
CHECKSUM=$(sha256sum "${ENCRYPTED_FILE}" | awk '{print $1}')
echo "[INFO] SHA256 Checksum: ${CHECKSUM}"
echo "${CHECKSUM}  $(basename "${ENCRYPTED_FILE}")" > "${ENCRYPTED_FILE}.sha256"

# Step 4: Upload to S3 Object Storage (if AWS CLI is configured)
if command -v aws &> /dev/null; then
    echo "[INFO] Uploading backup archive to s3://${S3_BUCKET}/db/..."
    aws s3 cp "${ENCRYPTED_FILE}" "s3://${S3_BUCKET}/db/$(basename "${ENCRYPTED_FILE}")"
    aws s3 cp "${ENCRYPTED_FILE}.sha256" "s3://${S3_BUCKET}/db/$(basename "${ENCRYPTED_FILE}.sha256")"
    echo "[SUCCESS] Backup uploaded to S3 storage."
else
    echo "[WARN] AWS CLI not detected. Backup stored locally at ${ENCRYPTED_FILE}"
fi

# Step 5: Cleanup local temp files if uploaded to S3
if command -v aws &> /dev/null && [ "${KEEP_LOCAL_BACKUP:-false}" != "true" ]; then
    rm -f "${ENCRYPTED_FILE}" "${ENCRYPTED_FILE}.sha256"
else
    echo "[INFO] Retaining local backup archive: ${ENCRYPTED_FILE}"
fi
echo "[SUCCESS] [$(date)] Marabagam database logical backup procedure completed."
