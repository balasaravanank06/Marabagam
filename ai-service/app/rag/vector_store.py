import json
import logging
from typing import List, Dict, Any, Optional
import psycopg2
from psycopg2.extras import RealDictCursor
from app.core.config import settings

logger = logging.getLogger(__name__)

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

    def search_similar_chunks(self, query_vector: List[float], top_k: int = 4) -> List[Dict[str, Any]]:
        conn = None
        results = []
        try:
            conn = self._get_connection()
            vec_str = "[" + ",".join(str(x) for x in query_vector) + "]"

            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                # Strict Parent Validation Query: Only APPROVED and INDEXED documents
                query = """
                    SELECT c.id AS chunk_id, c.content_text, c.metadata_json,
                           d.id AS document_id, d.title, d.title_ta, d.author, d.source_name, d.source_url, d.source_type,
                           1 - (c.embedding <=> %s::vector) AS similarity
                    FROM knowledge_chunks c
                    JOIN knowledge_documents d ON c.document_id = d.id
                    WHERE d.status = 'APPROVED' 
                      AND (d.indexing_status = 'INDEXED' OR d.indexing_status = 'NOT_INDEXED')
                    ORDER BY c.embedding <=> %s::vector
                    LIMIT %s;
                """
                cur.execute(query, (vec_str, vec_str, top_k))
                rows = cur.fetchall()

                for row in rows:
                    results.append({
                        "chunkId": str(row["chunk_id"]),
                        "documentId": str(row["document_id"]),
                        "contentText": row["content_text"],
                        "title": row["title"],
                        "titleTa": row["title_ta"],
                        "author": row["author"],
                        "sourceName": row["source_name"],
                        "sourceUrl": row["source_url"],
                        "sourceType": row["source_type"],
                        "similarity": float(row["similarity"])
                    })
        except Exception as e:
            logger.error("Vector similarity search failed: %s", str(e))
        finally:
            if conn:
                conn.close()

        return results

vector_store = VectorStore()
