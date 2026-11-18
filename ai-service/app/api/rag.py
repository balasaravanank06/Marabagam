from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from app.rag.rag_service import rag_service
from app.rag.chunker import DocumentChunker
from app.rag.embedding import get_embedding_provider
from app.rag.vector_store import vector_store

router = APIRouter(prefix="/internal/v1/rag", tags=["Internal RAG Pipeline"])

class RagQueryRequest(BaseModel):
    query: str
    conversationId: Optional[str] = None

class DocumentIndexRequest(BaseModel):
    documentId: str
    title: str
    content: str
    language: Optional[str] = "en"
    author: Optional[str] = "Unknown"
    sourceName: str
    sourceUrl: Optional[str] = None
    sourceType: str

chunker = DocumentChunker()
embedding_provider = get_embedding_provider()

@router.post("/query")
async def process_rag_query(req: RagQueryRequest):
    try:
        res = await rag_service.query_cultural_guide(req.query, req.conversationId)
        return res
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal RAG processing failure: {str(e)}"
        )

@router.post("/index")
async def index_document(req: DocumentIndexRequest):
    try:
        # 1. Chunk document
        chunks = chunker.chunk_text(req.content)
        if not chunks:
            raise HTTPException(status_code=400, detail="Document content is empty or unparseable")

        # 2. Embed chunks
        embeddings = await embedding_provider.embed_documents(chunks)

        # 3. Store in pgvector
        metadata = {
            "title": req.title,
            "author": req.author,
            "sourceName": req.sourceName,
            "sourceUrl": req.sourceUrl,
            "sourceType": req.sourceType,
            "language": req.language
        }
        vector_store.insert_chunks(req.documentId, chunks, embeddings, metadata)

        return {"success": True, "message": f"Document {req.documentId} indexed into {len(chunks)} chunks", "chunkCount": len(chunks)}
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Document indexing failed: {str(e)}"
        )
