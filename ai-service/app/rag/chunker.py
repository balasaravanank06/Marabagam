import unicodedata
import re
from typing import List, Dict, Any

class DocumentChunker:
    def __init__(self, target_chunk_chars: int = 2000, overlap_chars: int = 200):
        self.target_chunk_chars = target_chunk_chars
        self.overlap_chars = overlap_chars

    def normalize_text(self, text: str) -> str:
        if not text:
            return ""
        # Unicode NFC normalization preserves Tamil characters and diacritics
        normalized = unicodedata.normalize('NFC', text)
        # Clean extra whitespace while keeping Tamil script intact
        normalized = re.sub(r'[ \t]+', ' ', normalized)
        return normalized.strip()

    def chunk_text(self, text: str) -> List[str]:
        clean_text = self.normalize_text(text)
        if not clean_text:
            return []

        if len(clean_text) <= self.target_chunk_chars:
            return [clean_text]

        chunks = []
        start = 0
        text_len = len(clean_text)

        while start < text_len:
            end = start + self.target_chunk_chars
            if end >= text_len:
                chunks.append(clean_text[start:])
                break

            # Try to break at paragraph or sentence boundary
            break_pos = clean_text.rfind('\n\n', start, end)
            if break_pos == -1 or break_pos < start + 500:
                break_pos = clean_text.rfind('. ', start, end)
            if break_pos == -1 or break_pos < start + 500:
                # Tamil sentence boundary check
                break_pos = clean_text.rfind('। ', start, end)
            if break_pos == -1 or break_pos < start + 500:
                break_pos = end

            chunk = clean_text[start:break_pos + 1].strip()
            if chunk:
                chunks.append(chunk)
            
            start = max(start + 1, break_pos + 1 - self.overlap_chars)

        return chunks
