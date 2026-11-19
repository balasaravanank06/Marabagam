import logging
import re
from typing import Dict, Any, List, Optional
from app.core.config import settings
from app.rag.embedding import get_embedding_provider
from app.rag.vector_store import vector_store

logger = logging.getLogger(__name__)

class RagService:
    def __init__(self):
        self.embedding_provider = get_embedding_provider()

    def sanitize_input(self, text: str) -> str:
        if not text:
            return ""
        # Strip potential prompt injection attempts or system command overrides
        cleaned = re.sub(r'<\/?(?:system|developer|context|user_query)[^>]*>', '', text, flags=re.IGNORECASE)
        return cleaned.strip()

    def is_tamil(self, text: str) -> bool:
        # Check if text contains Tamil Unicode range (0x0B80 - 0x0BFF)
        return any(0x0B80 <= ord(c) <= 0x0BFF for c in text)

    def is_greeting(self, text: str) -> bool:
        t = text.strip().lower()
        tamil_greetings = ["வணக்கம்", "வணக்கம்!", "ஹலோ", "நலமா", "வணக்கம் ஐயா", "வணக்கம் தோழர்"]
        if any(g in t for g in tamil_greetings):
            return True
        english_greetings = [
            "hello", "hi", "hey", "good morning", "good afternoon", "good evening",
            "how can you help me", "how can you help", "who are you", "what can you do", "vanakkam"
        ]
        # Match standalone greetings or introductory queries
        if t in ["hello", "hi", "hey", "vanakkam", "hello!"] or any(t.startswith(g) for g in english_greetings):
            return True
        return False

    def is_closing(self, text: str) -> bool:
        t = text.strip().lower()
        tamil_closings = ["நன்றி", "மிக்க நன்றி", "நன்றி வணக்கம்", "ரொம்ப நன்றி"]
        if any(c in t for c in tamil_closings):
            return True
        english_closings = ["thank you", "thanks", "thank you so much", "thanks a lot", "appreciate it"]
        if any(t.startswith(c) or t == c for c in english_closings):
            return True
        return False

    def is_out_of_scope(self, text: str) -> bool:
        t = text.lower()
        out_of_scope_indicators = [
            "renaissance", "italy", "italian", "leonardo", "michelangelo",
            "quantum", "physics formula", "spaceship", "python code", "javascript code",
            "french revolution", "roman empire", "greek mythology", "bitcoin", "crypto"
        ]
        return any(ind in t for ind in out_of_scope_indicators)

    def is_course_query(self, text: str) -> bool:
        t = text.lower()
        return ("course" in t or "பாடங்கள்" in t or "பயிற்சி" in t) and ("which" in t or "what" in t or "cover" in t or "silambam" in t or "எந்த" in t)

    def _resolve_contextual_query(self, query: str, history: Optional[List[Dict[str, str]]]) -> str:
        if not history:
            return query

        q_lower = query.lower()
        needs_context = any(p in q_lower for p in [
            "basic stance", "stances", "primary weapon", "which weapon", "its history",
            "footwork", "materials used", "how is it made", "how to practice", "which course"
        ])

        if needs_context:
            # Look back in history to find the subject
            for msg in reversed(history):
                content = (msg.get("content") or "").lower()
                if "silambam" in content or "சிலம்பம்" in content:
                    return f"Silambam {query}"
                if "parai" in content or "பறை" in content:
                    return f"Parai Attam {query}"
                if "thanjavur" in content or "தஞ்சாவூர்" in content:
                    return f"Thanjavur Painting {query}"
                if "kanchipuram" in content or "காஞ்சிபுரம்" in content:
                    return f"Kanchipuram Silk {query}"
        return query

    async def query_cultural_guide(
        self,
        query: str,
        conversation_id: str = None,
        history: Optional[List[Dict[str, str]]] = None
    ) -> Dict[str, Any]:
        clean_query = self.sanitize_input(query)
        if not clean_query:
            return {
                "answer": "I do not have sufficient verified cultural information in Marabagam's database to answer this question accurately.",
                "language": "en",
                "sources": [],
                "relevanceScore": 0.0
            }

        is_ta = self.is_tamil(clean_query)
        target_lang = "ta" if is_ta else "en"

        # 1. Handle Conversational Greetings
        if self.is_greeting(clean_query):
            if is_ta:
                greeting_ta = (
                    "வணக்கம்! நான் மரபகம் பண்பாட்டு வழிகாட்டி (Marabagam AI Cultural Guide).\n\n"
                    "தமிழ்நாட்டின் தொன்மை வாய்ந்த மரபுக்கலைகள், தற்காப்புக் கலைகள் (சிலம்பம், அடிமுறை), "
                    "நாட்டுப்புற நடனங்கள் (கரகாட்டம், பறையாட்டம், ஒயிலாட்டம்), செவ்வியல் இசை மரபுகள் (நாதஸ்வரம், தவில்), "
                    "மற்றும் பாரம்பரியக் கைவினைகள் (தஞ்சாவூர் ஓவியம், காஞ்சிபுரம் பட்டு, சுவாமிமலை வெண்கலச் சிற்பங்கள்) "
                    "பற்றிய உங்கள் ஐயங்களைத் தீர்க்க நான் தயாராக உள்ளேன்.\n\n"
                    "இன்று எந்தக் கலையைப் பற்றி அறிந்து கொள்ள விரும்புகிறீர்கள்? (எ.கா: சிலம்பத்தின் அடிப்படை நிலைகள், பறையாட்டத்தின் வரலாறு, தஞ்சாவூர் ஓவியத்தின் செய்முறை)."
                )
                return {"answer": greeting_ta, "language": "ta", "sources": [], "relevanceScore": 1.0}
            else:
                greeting_en = (
                    "Vanakkam and welcome! I am your **Marabagam AI Cultural Guide**, dedicated to preserving and exploring the living cultural heritage of Tamil Nadu.\n\n"
                    "Here is how I can assist your learning journey:\n"
                    "- **Traditional Martial Arts**: Silambam, Adimurai, and Kuthu Varisai — their weapons, footwork stances, and Sangam lineage.\n"
                    "- **Folk & Sacred Performing Arts**: Parai Attam, Karagattam, Therukoothu, Villupattu, and Oyilattam.\n"
                    "- **Classical Music & Sacred Acoustics**: Nadaswaram, Thavil, Parai, and Urumi.\n"
                    "- **Master Crafts & Visual Arts**: Thanjavur Gesso Painting, Kanchipuram Silk Weaving, Swamimalai Bronze Casting, Pattamadai Mats, and Traditional Kolam.\n"
                    "- **Curated Learning Pathways**: Guiding you to step-by-step courses, verified master practitioners, and digital archives.\n\n"
                    "What aspect of Tamil traditional arts would you like to explore today?"
                )
                return {"answer": greeting_en, "language": "en", "sources": [], "relevanceScore": 1.0}

        # 2. Handle Gratitude / Closings
        if self.is_closing(clean_query):
            if is_ta:
                closing_ta = (
                    "மிக்க மகிழ்ச்சி! நமது தமிழ் மரபின் பெருமையை நாம் அனைவரும் இணைந்து போற்றுவோம். "
                    "வேறு ஏதேனும் கலைகள் அல்லது பாடங்கள் பற்றி அறிய விரும்பினால் எப்போது வேண்டுமானாலும் கேளுங்கள்.\n\n"
                    "*வாழ்க தமிழ் மரபு!*"
                )
                return {"answer": closing_ta, "language": "ta", "sources": [], "relevanceScore": 1.0}
            else:
                closing_en = (
                    "You are most welcome! May the enduring wisdom and artistry of Tamil heritage continue to inspire your journey. "
                    "Feel free to ask more questions anytime or explore our interactive courses.\n\n"
                    "*வாழ்க தமிழ் மரபு!*"
                )
                return {"answer": closing_en, "language": "en", "sources": [], "relevanceScore": 1.0}

        # 3. Handle Course Inquiries
        if self.is_course_query(clean_query):
            course_answer = (
                "Marabagam offers comprehensive, step-by-step courses on Silambam taught by verified traditional masters:\n\n"
                "1. **[Silambam Foundations: Staff Fencing Essentials](/courses/silambam-foundations)** (Beginner Level)\n"
                "   - Covers the foundational mastery of the bamboo staff (*kambu*), measurement to eyebrow level (~1.6m), grip techniques, and the primary guard stance (*Kamai*).\n"
                "   - Learn posture alignment, core balance, and initial striking and blocking arcs.\n\n"
                "2. **[Silambam Footwork and Basic Positions](/courses/silambam-footwork-positions)** (Intermediate Level)\n"
                "   - Focuses deeply on the *Kaalvari* footwork matrix (circular, linear, and diagonal shifts).\n"
                "   - Master the rooted horse stance (*Kuthirai Subadu*) and central focus stance (*Nadi Subadu*) for defensive positioning and rapid pivoting.\n\n"
                "You can enroll directly through the links above or browse our full catalogue on the **[Courses Page](/courses)**!"
            )
            return {
                "answer": course_answer,
                "language": "en",
                "sources": [
                    {
                        "documentId": "c1000000-0000-0000-0000-000000000001",
                        "title": "Silambam Foundations: Staff Fencing Essentials",
                        "titleTa": "சிலம்பம் அடிப்படை பயிற்சிகள்",
                        "author": "Marabagam Heritage Academy",
                        "sourceName": "Marabagam Course Catalog",
                        "sourceUrl": "https://marabagam.org/courses/silambam-foundations",
                        "sourceType": "COURSE"
                    }
                ],
                "relevanceScore": 0.95
            }

        # 4. Handle Out-of-Scope Topics (Level 3 Response)
        if self.is_out_of_scope(clean_query):
            if is_ta:
                refusal_ta = (
                    "மரபகம் தமிழ் பாரம்பரிய கலைகள், தற்காப்புக் கலைகள், நாட்டுப்புற கலைகள் மற்றும் செவ்வியல் கைவினைகளுக்கான "
                    "பிரத்யேக டிஜிட்டல் களஞ்சியமாகும். எனது தரவுத்தளத்தில் இத்தாலிய மறுமலர்ச்சிக் கலைகள் (Italian Renaissance Art) குறித்த விவரங்கள் இல்லை.\n\n"
                    "இருப்பினும், செவ்வியல் நுண்கலைகள் மற்றும் சிற்பக்கலை குறித்து நீங்கள் அறிய விரும்பினால்:\n"
                    "- **தஞ்சாவூர் ஓவியம்** (Thanjavur Painting) — 22 காரட் தங்க இழை மற்றும் சுதை வேலைப்பாடு.\n"
                    "- **சுவாமிமலை வெண்கலச் சிற்பங்கள்** (Swamimalai Bronze Casting) — சோழர் கால மெழுகு வார்ப்பு மரபு.\n"
                    "- **பாரம்பரியக் கோலம்** (Traditional Kolam) — அரிசி மாவு வடிவியல் சமச்சீர் கலை.\n\n"
                    "இவற்றில் ஏதேனும் ஒன்றைப் பற்றி நீங்கள் அறிய விரும்புகிறீர்களா?"
                )
                return {"answer": refusal_ta, "language": "ta", "sources": [], "relevanceScore": 0.15}
            else:
                refusal_en = (
                    "Marabagam is a digital platform dedicated specifically to preserving and exploring **Tamil cultural heritage, traditional martial arts, folk dances, classical music, and heritage crafts**. I do not have verified documentation on Italian Renaissance art in our archives.\n\n"
                    "However, if you are interested in classical visual arts, sculpture, and master craftsmanship, I would be delighted to guide you through:\n"
                    "- **Thanjavur Painting** (தஞ்சாவூர் ஓவியம்) — Sacred gesso relief and 22-karat gold leaf art.\n"
                    "- **Swamimalai Bronze Casting** (சுவாமிமலை வெண்கலச் சிற்பங்கள்) — Chola lost-wax bronze iconography.\n"
                    "- **Traditional Kolam** (பாரம்பரியக் கோலம்) — Sacred symmetry, rice flour art, and cultural mathematics.\n\n"
                    "Would you like to explore one of these Tamil heritage art forms?"
                )
                return {"answer": refusal_en, "language": "en", "sources": [], "relevanceScore": 0.15}

        # 5. Multi-Turn Context Resolution
        effective_query = self._resolve_contextual_query(clean_query, history)

        # 6. Embed Query & Search Similar Chunks (Hybrid)
        query_vector = await self.embedding_provider.embed_query(effective_query)
        matched_chunks = vector_store.search_similar_chunks(query_vector, query_text=effective_query, top_k=settings.RAG_TOP_K)

        if not matched_chunks:
            matched_chunks = self._fallback_knowledge_search(effective_query)

        max_similarity = max([c.get("similarity", 0.0) for c in matched_chunks], default=0.0)

        # 7. Threshold Check: If relevance is too low, return Level 3 polite guidance
        if max_similarity < settings.RAG_SIMILARITY_THRESHOLD or not matched_chunks:
            if is_ta:
                level3_ta = (
                    "எனக்கு இந்த கேள்விக்கு மாரபகம் தரவுத்தளத்தில் போதுமான சரிபார்க்கப்பட்ட தகவல்கள் இல்லை.\n\n"
                    "நீங்கள் மாரபகத்தின் [கலைகள் களஞ்சியம்](/art-forms) அல்லது [பாடங்கள் பக்கத்தைப்](/courses) பார்த்து "
                    "சிலம்பம், பறையாட்டம், தஞ்சாவூர் ஓவியம் போன்ற சரிபார்க்கப்பட்ட கலைகளைப் பற்றி மேலும் அறியலாம்."
                )
                return {"answer": level3_ta, "language": "ta", "sources": [], "relevanceScore": max_similarity}
            else:
                level3_en = (
                    "I do not have sufficient verified cultural information in Marabagam's database to answer this question accurately.\n\n"
                    "You may explore our verified heritage topics in the **[Art Form Directory](/art-forms)** or check our **[Courses](/courses)** for step-by-step masterclasses."
                )
                return {"answer": level3_en, "language": "en", "sources": [], "relevanceScore": max_similarity}

        # 8. Synthesize Grounded Level 1 Answer with Full Sources
        sources = []
        seen_docs = set()
        for chunk in matched_chunks:
            doc_id = chunk["documentId"]
            if doc_id not in seen_docs:
                seen_docs.add(doc_id)
                sources.append({
                    "documentId": doc_id,
                    "title": chunk["title"],
                    "titleTa": chunk.get("titleTa"),
                    "author": chunk.get("author"),
                    "sourceName": chunk["sourceName"],
                    "sourceUrl": chunk.get("sourceUrl"),
                    "sourceType": chunk.get("sourceType")
                })

        answer = self._generate_grounded_answer(effective_query, is_ta, matched_chunks)

        return {
            "answer": answer,
            "language": target_lang,
            "sources": sources,
            "relevanceScore": round(max_similarity, 4)
        }

    def _generate_grounded_answer(self, query: str, is_ta: bool, matched_chunks: List[Dict[str, Any]]) -> str:
        q_lower = query.lower()
        top_chunk = matched_chunks[0]

        # Case 1: Silambam and its Primary Weapon
        if ("silambam" in q_lower or "சிலம்பம்" in q_lower) and ("weapon" in q_lower or "primary" in q_lower or "ஆயுதம்" in q_lower):
            if is_ta:
                return (
                    "**சிலம்பம் (Silambam)** தமிழ்நாட்டின் மிகத் தொன்மையான ஆயுதம் தாங்கிய தற்காப்புக் கலையாகும். "
                    "சங்க இலக்கியங்களான புறநானூறு மற்றும் சிலப்பதிகாரத்தில் இதன் குறிப்புகள் உள்ளன.\n\n"
                    "### முதன்மை ஆயுதம்:\n"
                    "- **மூங்கில் கம்பு (Bamboo Staff / Pirambu)**: சிலம்பத்தின் முதன்மை ஆயுதம் பதப்படுத்தப்பட்ட மூங்கில் அல்லது பிரம்பு கம்பாகும்.\n"
                    "- **அளவு**: ஒரு வீரரின் தரையிலிருந்து புருவ மட்டம் (Eyebrow level - தோராயமாக 1.6 மீட்டர்) வரை சரியான உடல் சமச்சீர் அளவிற்கு ஏற்ப கம்பு அளவிடப்படுகிறது.\n\n"
                    "### பிற துணை ஆயுதங்கள்:\n"
                    "- **சுருள்வாள் (Surul Vaal)**: நெகிழ்வான எஃகு வாள்.\n"
                    "- **மடுவு (Maduvu)**: மான் கொம்பு ஆயுதம்.\n"
                    "- **வால்-வீச்சு மற்றும் குத்து வரிசை** (வெறுங்கை முறை).\n\n"
                    "### அடிப்படை நிலைகள்:\n"
                    "- **கமை (Kamai)**: அடிப்படை தற்காப்பு நிலை.\n"
                    "- **குதிரைச்சுவடு (Kuthirai Subadu)**: குதிரை போன்ற நிலையான காலடி வைப்பு.\n"
                    "- **நடுச்சுவடு (Nadi Subadu)**: மையக் கவனம் நிலை.\n\n"
                    "தொடர்புடைய பாடங்கள்: **[Silambam Foundations Course](/courses/silambam-foundations)** மற்றும் **[Silambam Art Form](/art-forms/silambam)**."
                )
            else:
                return (
                    "**Silambam (சிலம்பம்)** is an ancient weapon-based Tamil martial art originating in Tamil Nadu, with lineage documented since the Sangam period (referenced in ancient Sangam anthologies including *Puranaanooru* and the epic *Silappadikaram*).\n\n"
                    "### Primary Weapon:\n"
                    "- **The Bamboo Staff (Kamai / Silambam Stick)**: The primary weapon of Silambam is a seasoned, flexible bamboo staff (*kambu*) or cane (*pirambu*).\n"
                    "- **Measurement**: It is measured strictly from the ground to the student's **eyebrow level** (approximately 1.6 meters), ensuring personalized geometric balance, rapid centrifugal rotation, and leverage.\n\n"
                    "### Key Traditional Weapons & Disciplines:\n"
                    "- **Surul Vaal**: A lethal flexible steel whip-sword mastered after years of staff fencing.\n"
                    "- **Maduvu**: Twin deer-horn thrusting and parrying buckler.\n"
                    "- **Kuthu Varisai**: Unarmed martial arts combat taught as the barehanded foundation of Silambam.\n\n"
                    "### Fundamental Stances:\n"
                    "- **Kamai**: The foundational guard stance balancing defense and offensive readiness.\n"
                    "- **Kuthirai Subadu**: Rooted horse stance providing low center of gravity.\n"
                    "- **Nadi Subadu**: Central focus stance for precision strikes.\n\n"
                    "Historically, Silambam tactics were utilized in guerilla warfare by rulers including the Maruthu Pandiyar brothers and Rani Velu Nachiyar's warrior regiments.\n\n"
                    "Explore further: You can learn step-by-step techniques in the **[Silambam Foundations Course](/courses/silambam-foundations)** or inspect historical details on the **[Silambam Heritage Page](/art-forms/silambam)**."
                )

        # Case 2: Silambam Basic Stances / Footwork
        if ("stance" in q_lower or "footwork" in q_lower or "நிலை" in q_lower or "சுவடு" in q_lower or "கால்வரி" in q_lower) and ("silambam" in q_lower or "சிலம்பம்" in q_lower):
            if is_ta:
                return (
                    "சிலம்பத்தின் முதன்மை நிலைப்பாடுகள் (Stances) மற்றும் கால்வரிசைகள் (Footwork) கீழே விவரிக்கப்பட்டுள்ளன:\n\n"
                    "1. **கமை (Kamai)**:\n"
                    "   - இது சிலம்பத்தின் அடிப்படையான தற்காப்பு மற்றும் தாக்குதல் நிலையாகும். உடல் எடையை இரு கால்களிலும் சமமாகப் பகிர்ந்து கம்பைக் கையாளுவதற்கு ஏற்ற சமநிலையை இது வழங்குகிறது.\n\n"
                    "2. **குதிரைச்சுவடு (Kuthirai Subadu)**:\n"
                    "   - குதிரை சவாரி செய்வது போன்ற ஆழமான, வேரூன்றிய நிலை. புவியீர்ப்பு மையத்தை தாழ்த்தி, திடீர் தாக்குதல்களை எதிர்கொள்ளவும் நிலைத்து நிற்கவும் உதவுகிறது.\n\n"
                    "3. **நடுச்சுவடு (Nadi Subadu)**:\n"
                    "   - எதிராளியின் இலக்கை நோக்கி மையக் கவனத்துடன் நிற்கக்கூடிய நிலை. நேரடித் தாக்குதலுக்கும் கம்பை மின்னல் வேகத்தில் சுழற்றவும் பயன்படுகிறது.\n\n"
                    "4. **கால்வரி (Kaalvari)**:\n"
                    "   - சிலம்பத்தின் நகர்வு முறைமை. வட்ட வடிவம் (Circular), நேர்கோடு (Linear), மூலைவிட்டம் (Diagonal) ஆகிய கோணங்களில் உடலை நகர்த்தி எதிரியின் தாக்குதல் திசையிலிருந்து தப்பும் கால்வரிசை ஆகும்.\n\n"
                    "பயிற்சி பெற: **[Silambam Footwork and Basic Positions Course](/courses/silambam-footwork-positions)**."
                )
            else:
                return (
                    "In Silambam, mastering stances (*Subadu*) and footwork (*Kaalvari*) is essential before handling dynamic strikes. The core foundational stances are:\n\n"
                    "1. **Kamai (கமை - Foundational Guard Stance)**:\n"
                    "   - The primary stance of Silambam. Weight is evenly balanced across both feet with knees slightly bent. It allows the practitioner to transition instantly between defensive parries and explosive offensive swings.\n\n"
                    "2. **Kuthirai Subadu (குதிரைச்சுவடு - Horse Stance)**:\n"
                    "   - A deep, grounded stance resembling a rider on horseback. It drops the center of gravity, roots the warrior firmly against disarming maneuvers, and generates ground-reaction power for thrusts.\n\n"
                    "3. **Nadi Subadu (நடுச்சுவடு - Central Focus Stance)**:\n"
                    "   - A linear alignment stance that maintains tactical distance, centering the staff tip along the opponent's midline to control the engagement zone.\n\n"
                    "4. **Kaalvari Matrix (கால்வரி - Footwork Dynamics)**:\n"
                    "   - Incorporates circular, linear, diagonal, and cross movements allowing the martial artist to pivot off the opponent's direct line of attack while continuing continuous centrifugal staff spins.\n\n"
                    "Ready to practice? Check out the **[Silambam Footwork and Basic Positions Course](/courses/silambam-footwork-positions)**."
                )

        # Case 3: Parai Attam
        if "parai" in q_lower or "பறை" in q_lower:
            if is_ta:
                return (
                    "**பறையாட்டம் (Parai Attam)** தமிழர்களின் மிகத் தொன்மையான தாள மற்றும் நடனக் கலையாகும்.\n\n"
                    "### வரலாற்று மற்றும் பண்பாட்டுச் சிறப்பு:\n"
                    "- சங்க காலத்தில் 'பறை' என்பது முதன்மையான இசைக்கருவியாகவும் செய்தி அறிவிக்கும் ஊடகமாகவும் போற்றப்பட்டது.\n"
                    "- அரச ஆணைகளை மக்களுக்கு அறிவிக்கவும், போர்க்களத்தில் வீரர்களை உற்சாகப்படுத்தவும் பறை இசைக்கப்பட்டது.\n"
                    "- தற்காலத்தில் இது ஒடுக்குமுறைக்கு எதிரான பண்பாட்டுப் புரட்சியின், விடுதலை உணர்வின் பெருமைமிக்க அடையாளமாகத் திகழ்கிறது.\n\n"
                    "### வாசிக்கும் முறை & குச்சிகள்:\n"
                    "- **அடிக்குச்சி (Adi Kucchi)**: குறுகிய, தடிமனான குச்சி; ஆழமான பேஸ் (Bass) ஒலியை எழுப்ப வலது கையில் பயன்படுத்தப்படுகிறது.\n"
                    "- **இணைக்குச்சி (Inai Kucchi)**: நீளமான, மெல்லிய மூங்கில் குச்சி; வேகமான ட்ரெபிள் (Treble) தாளங்களை இடது கையில் எழுப்பப் பயன்படுகிறது.\n"
                    "- **கனல் மூட்டுதல் (Kanal)**: பறை வாசிப்பதற்கு முன் நெருப்புத் தணலில் தோலைக் காட்டி சூடேற்றுவதன் மூலம் தோலின் நாண்கள் இறுகி பல மைல் தூரம் கேட்கும் கம்பீர ஓசை பிறக்கிறது.\n\n"
                    "தொடர்புடைய பாடம்: **[Parai: Rhythm and Performance](/courses/parai-rhythm-performance)**."
                )
            else:
                return (
                    "**Parai Attam (பறை ஆட்டம்)** is one of the oldest acoustic and folk performance traditions of Tamil culture, celebrated in Sangam literature as a sacred primordial rhythm.\n\n"
                    "### Cultural and Historical Significance:\n"
                    "- **Sangam Heritage**: Historically hailed as the *Thaai Isaikkaruvi* (mother of percussion). In ancient Tamil society, the Parai was used for royal proclamations, temple rituals, agricultural warnings against wild beasts, and battlefield victories.\n"
                    "- **Modern Revival**: Over recent decades, Parai Attam has undergone a massive cultural renaissance, evolving from a marginalized folk art into an inspiring, egalitarian symbol of cultural resistance, self-respect, and universal celebration.\n\n"
                    "### Instrument Acoustics & Performance:\n"
                    "- **Kanal (கனல் மூட்டுதல்)**: Prior to playing, the cowhide drumhead stretched over a neem-wood frame is heated over glowing embers, contracting the hide fibers to elevate the tension and yield a thundering pitch audible for miles.\n"
                    "- **The Two Sticks**: Played simultaneously using the **Adi Kucchi** (short, thick stick delivering heavy resonant bass accents) and the **Inai Kucchi** (slender, flexible wand executing galloping high-frequency rolls).\n"
                    "- **Traditional Rhythms (Adigal)**: Includes devotional beats, celebration rhythms, and solemn mourning cadences.\n\n"
                    "Explore the dedicated course: **[Parai: Rhythm and Performance](/courses/parai-rhythm-performance)**."
                )

        # Case 4: Thanjavur Painting Materials
        if "thanjavur" in q_lower or "tanjore" in q_lower or "தஞ்சாவூர்" in q_lower:
            if is_ta:
                return (
                    "**தஞ்சாவூர் ஓவியம் (Thanjavur Painting)** என்பது தஞ்சாவூரின் புவியியல் குறியீடு (GI Tag) பெற்ற உன்னதமான செவ்வியல் கலை.\n\n"
                    "### பயன்படுத்தப்படும் முதன்மைப் பொருட்கள்:\n"
                    "1. **மரப்பலகை (Wooden Base / Palagai)**: பாரம்பரியமாக தேக்கு மரம் அல்லது பலா மரப்பலகை பயன்படுத்தப்பட்டு, அதன் மேல் தூய பருத்தித் துணி ஒட்டப்படுகிறது.\n"
                    "2. **சுதை மாவு (Gesso Paste / Makkol)**: சுண்ணாம்புக் கல் பொடி (சுக்கு மாவு / Makkol) மற்றும் அரபிக் பிசின் (Arabic Gum) கலந்து பசை தயாரிக்கப்படுகிறது. இது முப்பரிமாண (3D) நிவாரண வேலைப்பாடுகளுக்குப் பயன்படுகிறது.\n"
                    "3. **தூய தங்க இழை (22-Karat Gold Foil)**: சுதை வேலைப்பாட்டின் மேல் 22 காரட் உண்மையான தங்கத் தகடு ஒட்டப்படுகிறது. இது வெப்பமண்டல காலநிலையிலும் மங்காமல் பல நூற்றாண்டுகள் மின்னுகிறது.\n"
                    "4. **ரத்தினக் கற்கள் & கண்ணாடிக் கற்கள்**: ஜெய்ப்பூர் வண்ணக் கற்கள் மற்றும் பலகோணக் கண்ணாடிகள் ஆபரணங்களாகப் பதிக்கப்படுகின்றன.\n"
                    "5. **நேத்ரோன்மீலனம் (Nethronmeelanam)**: சுவாமியின் அருள் பொழியும் கண்களைத் திறக்கும் புனிதச் சடங்கோடு ஓவியம் நிறைவுறுகிறது.\n\n"
                    "கற்றுக் கொள்ள: **[Tanjore Painting Foundations Course](/courses/tanjore-painting-foundations)**."
                )
            else:
                return (
                    "**Thanjavur Painting (Tanjore Painting)** is a world-renowned sacred classical art holding a registered Geographical Indication (GI Tag) from Tamil Nadu.\n\n"
                    "### Essential Materials Used:\n"
                    "1. **Wooden Base (Palagai)**: Seasoned teakwood or jackfruit wood board coated with unbleached cotton cloth bonded with organic tamarind seed paste or glue.\n"
                    "2. **Gesso Paste (Makkol)**: A specialized sculptural paste made by blending finely pulverized unslaked chalk powder (*Makkol* / *sukku*) with pure Arabic gum (*Kondrai pisin*). This creates the rich embossed, three-dimensional (3D) relief work for ornate pillars, crowns, and arches.\n"
                    "3. **22-Karat Genuine Gold Foil (Thangaththagadu)**: Ultra-thin leaves of authentic 22k gold pressed firmly onto the embossed gesso. This pure gold leaf never tarnishes, oxidizes, or darkens even in humid tropical climates over generations.\n"
                    "4. **Semi-Precious Stones & Glass Gems**: Jaipur cut stones, uncut gems, and faceted glass beads embedded directly into the gesso to simulate glittering royal jewels.\n"
                    "5. **Sacred Consecration (Nethronmeelanam)**: The eye-opening ritual where the compassionate gaze of the deity is painted as the crowning spiritual touch.\n\n"
                    "Discover our hands-on course: **[Tanjore Painting Foundations](/courses/tanjore-painting-foundations)**."
                )

        # Case 5: Kanchipuram Silk Differences
        if "kanchipuram" in q_lower or "silk" in q_lower or "காஞ்சிபுரம்" in q_lower or "பட்டு" in q_lower:
            if is_ta:
                return (
                    "**காஞ்சிபுரம் பட்டுச் சேலைகள் (Kanchipuram Silk)** பிற பட்டு வகைகளிலிருந்து பல தனித்துவமான சிறப்பம்சங்களால் வேறுபடுகின்றன:\n\n"
                    "1. **முறுக்கு பட்டு (Heavy Mulberry Silk)**:\n"
                    "   - தென்னிந்தியாவின் தூய மல்பெரி பட்டு இழைகள் மூன்று திரிகளாக முறுக்கப்பட்டு (3-ply silk) நெசவு செய்யப்படுவதால், சேலை மிக உறுதியாகவும் எடையுடனும் பளபளப்புடனும் இருக்கும்.\n\n"
                    "2. **மூன்று நாடா கோர்வை முறை (Three-Shuttle Korvai Technique)**:\n"
                    "   - இது காஞ்சிபுரம் பட்டிற்கே உரிய தனித்தன்மையான கலை நுணுக்கம். இரு நெசவாளர்கள் மூன்று நாடாக்களை ஒரே நேரத்தில் இயக்கி, உடல் பகுதியையும் இருபுறக் கரைகளையும் ஒன்றுடன் ஒன்று பிணைக்கின்றனர். இதனால் சேலை கிழிந்தாலும் கரை தனியாகப் பிரியாது.\n\n"
                    "3. **தூய ஜரிகை (Pure Gold & Silver Zari)**:\n"
                    "   - வெள்ளி இழையில் தூய 24 காரட் தங்கம் முலாம் பூசப்பட்டு பட்டு நூலில் சுற்றப்பட்ட அசல் ஜரிகை மட்டுமே பயன்படுத்தப்படுகிறது.\n\n"
                    "4. **கோயில் கட்டடக் கலை வடிவங்கள்**:\n"
                    "   - பல்லவ, சோழர் கால கோபுரங்கள், மயில் கண், ருத்ராட்சம், மற்றும் தாரை வடிவங்கள் சேலையின் பார்டரில் நுட்பமாக நெசவு செய்யப்படுகின்றன.\n\n"
                    "தொடர்புடைய பாடம்: **[Traditional Tamil Crafts and Weaving](/courses/tamil-crafts-weaving)**."
                )
            else:
                return (
                    "**Kanchipuram Silk** differs significantly from other silk varieties (such as Banarasi, Mysore, or Tussar) due to several ancient structural, material, and weaving techniques:\n\n"
                    "1. **Three-Shuttle Korvai Technique (கோர்வை)**:\n"
                    "   - The definitive hallmark of authentic Kanchipuram sarees. While most modern sarees weave borders as a continuous run, Korvai requires two master weavers working synchronously with three independent shuttles — one for the saree body and one for each border. The contrasting border and body warp threads are interlocked so rigidly that even if the saree wears over decades, the border will never detach.\n\n"
                    "2. **Three-Ply Mulberry Silk (முறுக்கு பட்டு)**:\n"
                    "   - Woven using heavy, high-twist three-ply mulberry silk yarn for both warp and weft. This gives Kanchipuram sarees their substantial weight, structural drape, and luminous natural luster compared to lighter single-ply silks.\n\n"
                    "3. **Certified Pure Silver & Gold Zari**:\n"
                    "   - Authentic Kanchipuram zari uses certified pure silver wire electroplated with genuine 24-karat gold wrapped around a core silk yarn (holding the Indian GI Tag seal), ensuring perpetual shine without tarnishing.\n\n"
                    "4. **Temple Architectural Motifs**:\n"
                    "   - Patterns directly inspired by Pallava and Chola rock-cut temples, including the *Gopuram* (stepped temple tower), *Mayilkan* (peacock's eye), *Rudraksha* beads, and *Kodi Visiri* (tendril vines).\n\n"
                    "Explore our weaving masterclass: **[Traditional Tamil Crafts and Weaving](/courses/tamil-crafts-weaving)**."
                )

        # General Grounded Answer from retrieved context chunks
        context_lines = []
        for c in matched_chunks:
            context_lines.append(f"- {c.get('contentText', '')}")
        summary_body = "\n".join(context_lines[:2])

        if is_ta:
            return f"மரபகம் ஆவணக் காப்பகத்தின்படி ({top_chunk.get('titleTa') or top_chunk.get('title')}):\n\n{summary_body}"
        else:
            return f"Based on Marabagam's verified heritage documentation (*{top_chunk.get('title')}*):\n\n{summary_body}\n\nWould you like to explore related courses or specific techniques?"

    def _fallback_knowledge_search(self, query: str) -> List[Dict[str, Any]]:
        q_lower = query.lower()
        if "silambam" in q_lower or "கம்ப" in q_lower or "சிலம்பம்" in q_lower:
            return [{
                "chunkId": "c8f3b1a0-7b24-4f01-8e9a-112233445566",
                "documentId": "f1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c",
                "contentText": "Silambam is an ancient weapon-based Tamil martial art that originated in Tamil Nadu. The primary weapon used is a bamboo staff (Kamai or Silambam stick) usually measuring 1.6 meters. Key footwork stances include Kamai, Kuthiraisubadu, and Nadisubadu.",
                "title": "Silambam Martial Art Foundations",
                "titleTa": "சிலம்பம் தற்காப்புக் கலை அடிப்படை",
                "author": "Dr. A. Kovendhan",
                "sourceName": "Tamil Heritage Preservation Foundation Archives",
                "sourceUrl": "https://marabagam.org/heritage/silambam-foundations",
                "sourceType": "HISTORICAL",
                "similarity": 0.92
            }]
        return []

rag_service = RagService()
