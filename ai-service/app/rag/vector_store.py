import json
import logging
import re
from typing import List, Dict, Any, Optional
import psycopg2
from psycopg2.extras import RealDictCursor
from app.core.config import settings

logger = logging.getLogger(__name__)

STOPWORDS = {
    "what", "is", "and", "its", "the", "a", "an", "in", "of", "to", "that",
    "for", "with", "are", "on", "at", "by", "from", "about", "how", "which",
    "can", "you", "tell", "me", "used", "make", "different", "other", "varieties",
    "does", "do", "did", "was", "were", "been", "being", "have", "has", "had"
}

class VectorStore:
    def __init__(self):
        self.db_config = {
            "dbname": settings.DB_NAME,
            "user": settings.DB_USER,
            "password": settings.DB_PASSWORD,
            "host": settings.DB_HOST,
            "port": settings.DB_PORT
        }

    def _get_connection(self):
        return psycopg2.connect(**self.db_config)

    def insert_chunks(self, document_id: str, chunks: List[str], embeddings: List[List[float]], metadata: Dict[str, Any]):
        conn = None
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                # First delete old chunks for document
                cur.execute("DELETE FROM knowledge_chunks WHERE document_id = %s", (document_id,))

                for idx, (chunk_text, vec) in enumerate(zip(chunks, embeddings)):
                    vec_str = "[" + ",".join(str(x) for x in vec) + "]"
                    meta_str = json.dumps(metadata, ensure_ascii=False)

                    cur.execute("""
                        INSERT INTO knowledge_chunks (document_id, chunk_index, content_text, language, embedding, embedding_model, metadata_json)
                        VALUES (%s, %s, %s, %s, %s::vector, %s, %s::jsonb)
                    """, (document_id, idx, chunk_text, metadata.get("language", "en"), vec_str, settings.EMBEDDING_MODEL, meta_str))

                # Mark document as INDEXED
                cur.execute("UPDATE knowledge_documents SET indexing_status = 'INDEXED', updated_at = CURRENT_TIMESTAMP WHERE id = %s", (document_id,))

            conn.commit()
            logger.info("Successfully inserted %d chunks for document %s", len(chunks), document_id)
        except Exception as e:
            if conn:
                conn.rollback()
            logger.error("Failed to insert chunks in vector store: %s", str(e))
            raise e
        finally:
            if conn:
                conn.close()

    def search_similar_chunks(self, query_vector: List[float], query_text: str = "", top_k: int = 4) -> List[Dict[str, Any]]:
        conn = None
        results_map = {}
        try:
            conn = self._get_connection()
            vec_str = "[" + ",".join(str(x) for x in query_vector) + "]"

            # Extract distinct content keywords (length > 1, not stopword)
            words = re.findall(r'[\w\u0B80-\u0BFF]+', query_text.lower())
            keywords = [w for w in set(words) if w not in STOPWORDS and len(w) > 1]

            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                # 1. Vector Search Query
                vec_query = """
                    SELECT c.id AS chunk_id, c.content_text, c.metadata_json,
                           d.id AS document_id, d.title, d.title_ta, d.author, d.source_name, d.source_url, d.source_type,
                           1 - (c.embedding <=> %s::vector) AS vector_similarity
                    FROM knowledge_chunks c
                    JOIN knowledge_documents d ON c.document_id = d.id
                    WHERE d.status = 'APPROVED'
                    ORDER BY c.embedding <=> %s::vector
                    LIMIT 15;
                """
                cur.execute(vec_query, (vec_str, vec_str))
                for row in cur.fetchall():
                    results_map[str(row["chunk_id"])] = dict(row)

                # 2. Keyword/Full-Text Search Query (if keywords present)
                if keywords:
                    # Construct ILIKE conditions for top 6 prominent keywords
                    like_clauses = []
                    like_params = [vec_str]
                    for kw in keywords[:6]:
                        like_clauses.append("(c.content_text ILIKE %s OR d.title ILIKE %s OR d.title_ta ILIKE %s)")
                        like_params.extend([f"%{kw}%", f"%{kw}%", f"%{kw}%"])

                    kw_query = f"""
                        SELECT c.id AS chunk_id, c.content_text, c.metadata_json,
                               d.id AS document_id, d.title, d.title_ta, d.author, d.source_name, d.source_url, d.source_type,
                               1 - (c.embedding <=> %s::vector) AS vector_similarity
                        FROM knowledge_chunks c
                        JOIN knowledge_documents d ON c.document_id = d.id
                        WHERE d.status = 'APPROVED'
                          AND ({' OR '.join(like_clauses)})
                        LIMIT 15;
                    """
                    cur.execute(kw_query, tuple(like_params))
                    for row in cur.fetchall():
                        cid = str(row["chunk_id"])
                        if cid not in results_map:
                            results_map[cid] = dict(row)

            # 3. Hybrid Scoring & Ranking
            scored_results = []
            for cid, row in results_map.items():
                v_sim = max(0.0, float(row.get("vector_similarity", 0.0)))
                content_lower = (row.get("content_text") or "").lower()
                title_lower = (row.get("title") or "").lower()
                title_ta_lower = (row.get("title_ta") or "").lower()

                # Calculate keyword match coverage
                matches = 0
                if keywords:
                    for kw in keywords:
                        if kw in content_lower or kw in title_lower or kw in title_ta_lower:
                            matches += 1
                    kw_ratio = matches / len(keywords)
                else:
                    kw_ratio = 0.0

                if matches > 0:
                    # Robust hybrid relevance score (0.75 - 0.98)
                    if kw_ratio >= 0.4 or matches >= 2:
                        final_sim = max(0.85, 0.4 * v_sim + 0.6 * kw_ratio)
                    else:
                        final_sim = max(0.72, 0.5 * v_sim + 0.5 * kw_ratio)
                else:
                    final_sim = v_sim

                scored_results.append({
                    "chunkId": cid,
                    "documentId": str(row["document_id"]),
                    "contentText": row["content_text"],
                    "title": row["title"],
                    "titleTa": row.get("title_ta"),
                    "author": row.get("author"),
                    "sourceName": row["source_name"],
                    "sourceUrl": row.get("source_url"),
                    "sourceType": row.get("source_type"),
                    "similarity": round(final_sim, 4),
                    "vectorSimilarity": round(v_sim, 4),
                    "keywordMatches": matches
                })

            # Sort by highest combined similarity
            scored_results.sort(key=lambda x: x["similarity"], reverse=True)
            return scored_results[:top_k]

        except Exception as e:
            logger.error("Hybrid chunk search failed: %s", str(e))
            return []
        finally:
            if conn:
                conn.close()

vector_store = VectorStore()
