"""
Knowledge Documents and RAG Chunks Data for Marabagam.
Contains 15 comprehensive cultural knowledge documents with 60 specialized chunks
covering Tamil traditional arts, martial forms, musical instruments, and crafts.
"""

KNOWLEDGE_DOCS = [
    {
        "id": "f1000000-0000-0000-0000-000000000001",
        "title": "Silambam Historical Lineage and Sangam Warrior Traditions",
        "title_ta": "சிலம்பக் கலை வரலாறு மற்றும் சங்க காலப் போர் மரபுகள்",
        "content": "Silambam is an ancient weapon-based martial art indigenous to Tamil Nadu with documented roots dating back over two millennia. Archaeological excavations at Adichanallur and Keezhadi, combined with literary references in Silappadikaram and Purananuru, attest to its antiquity. The fundamental weapon is a seasoned male bamboo staff (Kambu) cut to practitioner eyebrow height. The practitioner masters footwork patterns known as Kaalvari, strikes, parries, and secondary weapons including the Surul Vaal (flexible whip sword), Maduvu (deer horn dagger), and Vel (spear).",
        "content_ta": "சிலம்பம் என்பது இரண்டாயிரம் ஆண்டுகளுக்கும் மேலான வரலாற்றைக் கொண்ட தமிழகத்தின் தொன்மை வாய்ந்த தற்காப்புக் கலையாகும். ஆதிச்சநல்லூர், கீழடி அகழ்வாராய்ச்சிகள் மற்றும் சிலப்பதிகாரம், புறநானூறு போன்ற சங்க இலக்கியங்கள் சிலம்பக் கலையின் தொன்மையை மெய்ப்பிக்கின்றன. புருவ உயரம் கொண்ட மூங்கில் கம்பு, கால்வரிசை, நிலைப்பாடுகள் மற்றும் சுருள்வாள், மான் கொம்பு, வேல் போன்ற துணை ஆயுதப் பயிற்சிகளும் இதில் அடங்கும்.",
        "art_form_slug": "silambam",
        "district_code": "TN-MAD",
        "source_type": "HISTORICAL",
        "source_name": "Tamil Virtual Academy Archives & Sangam Literary Society",
        "source_url": "https://www.tamilvu.org/library/silambam-history",
        "author": "Prof. S. Rajaram",
        "chunks": [
            "Silambam stick dimensions are measured strictly from the ground to the student's eyebrow level, ensuring personalized geometric balance.",
            "The primary footwork system Kaalvari incorporates circular, linear, diagonal, and cross movements to constantly pivot away from the opponent's strike vectors.",
            "Surul Vaal is a deadly flexible steel whip sword mastered only after five to seven years of unblemished wooden staff training.",
            "Historically, Maruthu Pandiyar brothers and Rani Velu Nachiyar's female regiment used Silambam tactics in guerilla warfare against colonial forces."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000002",
        "title": "Adimurai and Varma Kalai Barehanded Combat and Healing Traditions",
        "title_ta": "அடிமுறை மற்றும் வர்மக்கலை தற்காப்பு மற்றும் மருத்துவ மரபுகள்",
        "content": "Adimurai is the indigenous southern Tamil barehanded combat system originating in the Travancore and Kanyakumari regions. Closely tied to Siddha medicine, it integrates 108 vital meridian points (Varma Pulligal) that can be targeted for instantaneous neutralization or stimulated for therapeutic restoration. Combat maneuvers rely on rapid open-palm strikes, ridge-hand blows, joint locks, throws, and ground grappling.",
        "content_ta": "அடிமுறை என்பது கன்னியாகுமரி மற்றும் தென் தமிழகத்தில் உருவான பாரம்பரிய வெறும் கை தற்காப்புக் கலையாகும். சித்த மருத்துவத்துடன் இணைந்த இக்கலையில் 108 வர்மப் புள்ளிகள் உள்ளன. உள்ளங்கை அடிகள், மூட்டுப் பூட்டுகள் மற்றும் வர்ம சிகிச்சை முறைகள் இதில் அடங்கும்.",
        "art_form_slug": "adimurai",
        "district_code": "TN-KK",
        "source_type": "PRACTITIONER_MANUAL",
        "source_name": "Agastya Traditional Varma Research Foundation",
        "source_url": "https://agastyavarma.org/adimurai-treatise",
        "author": "Asan Dr. K. Ponnusamy",
        "chunks": [
            "Adimurai distinguishes between Padu Varmam (twelve lethal pressure centers) and Thodu Varmam (ninety-six disabling or retrievable centers).",
            "Initial training strictly emphasizes physical body conditioning through Kuzhi Thozhil and defensive blocking before teaching offensive pressure point strikes.",
            "Varma Thadaval (therapeutic manipulation and oil massage) is used to revive unconscious fighters or relieve neuromuscular paralysis.",
            "Unlike sports fighting, Adimurai practitioners maintain a close-quarters fighting distance utilizing devastating elbow and knee combinations."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000003",
        "title": "Parai Attam: The Ancient Rhythm of Ritual Communication and Social Liberation",
        "title_ta": "பறை ஆட்டம்: சடங்குத் தொடர்பு மற்றும் சமூக விடுதலையின் தொன்மை இசை",
        "content": "The Parai is one of the oldest membranophone drums in world music, celebrated in Sangam literature as Thondagam and Meenkotparai. Traditionally crafted from cured cow hide stretched across a circular neem wood hoop, it is played with two wooden sticks: the thicker, shorter Adi Kucchi (striking mallet) and the slender, flexible Inai Kucchi (rhythm guide). The drum is tuned before every performance by heating the leather face over hot charcoal embers.",
        "content_ta": "பறை என்பது உலகின் தொன்மையான தோல் இசைக் கருவிகளில் ஒன்றாகும். வேப்ப மர வளையத்தில் பதப்படுத்தப்பட்ட மாட்டுத் தோலை இழுத்துக் கட்டி இது செய்யப்படுகிறது. அடிக் குச்சி மற்றும் இணைக் குச்சி கொண்டு தீக்கனலில் சூடேற்றி இசைக்கப்படுகிறது.",
        "art_form_slug": "parai-attam",
        "district_code": "TN-MAD",
        "source_type": "ACADEMIC_RESEARCH",
        "source_name": "Department of Folklore, Madurai Kamaraj University",
        "source_url": "https://mkuniversity.ac.in/folklore/parai-monograph",
        "author": "Dr. T. Dharmaraj",
        "chunks": [
            "Heating the leather membrane over embers (Kanal மூட்டுதல்) contracts the hide fibers, elevating the pitch and resonance for miles.",
            "The Adi Kucchi delivers the deep, resonant bass accents, while the flexible Inai Kucchi produces high-frequency galloping rolls.",
            "Traditional beats (Adigal) include devotional rhythms, temple festival march beats, royal announcement calls, and mourning cadences.",
            "Modern cultural movements have elevated Parai from an oppressed village instrument to a proud symbol of cultural resistance and universal art."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000004",
        "title": "Karagattam Sacred Ritual Dance and Pot Balancing Mechanics",
        "title_ta": "கரகாட்டம் புனித சடங்கு நடனம் மற்றும் குட சமநிலை நுட்பங்கள்",
        "content": "Karagattam is a signature folk dance of Tamil Nadu performed in honor of Mariamman, the goddess of rain and fertility. Dancers balance a decorated brass pot (Karagam) filled with raw rice and water, crowned with a conical floral tower and a handcrafted wooden parrot, without once touching it with their hands throughout strenuous footwork, squats, and acrobatic bends.",
        "content_ta": "கரகாட்டம் என்பது மழை மற்றும் வளத்தின் தெய்வமான மாரியம்மன் வழிபாட்டுடன் தொடர்புடைய தமிழகத்தின் முதன்மை கிராமிய நடனமாகும். அரிசி மற்றும் நீரால் நிரப்பப்பட்ட செம்பு குடத்தை தலையில் ஏந்தி கைகளால் தொடாமல் சமநிலைப்படுத்தி ஆடுகின்றனர்.",
        "art_form_slug": "karagattam",
        "district_code": "TN-THA",
        "source_type": "ORAL_TRADITION",
        "source_name": "South Zone Cultural Centre Thanjavur Folk Archives",
        "source_url": "https://szccthanjavur.gov.in/karagattam-documentation",
        "author": "S. Karunanidhi",
        "chunks": [
            "The performance is split between Shakthi Karagam (austere religious temple rite) and Aatta Karagam (entertaining acrobatic village stage show).",
            "Weight stabilization requires placing a tightly bound cloth ring (Pirimanai) atop the head, distributing the heavy pot's center of gravity.",
            "Dancers execute extreme bending poses, retrieving handkerchiefs or coins from the floor using only their eyelashes or teeth while keeping the pot steady.",
            "Musical accompaniment is strictly provided by the Nayyandi Melam, featuring two Nadaswarams, Thavil, Pambai, and Urumi drums."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000005",
        "title": "Therukoothu: All-Night Epic Street Theatre of Northern Tamil Nadu",
        "title_ta": "தெருக்கூத்து: வட தமிழகத்தின் விடிய விடிய நிகழும் இதிகாச நாடக மரபு",
        "content": "Therukoothu is the vibrant traditional street theatre form of northern Tamil Nadu, performed in open village squares from dusk until dawn. Centered around Draupadi Amman temple festivals and the Mahabharata cycle, actors wear towering ornate wooden crowns (Kireetam), massive shoulder ornaments (Bhujakeerthi), and high-contrast facial makeup made from natural mineral powders.",
        "content_ta": "தெருக்கூத்து என்பது வட தமிழகத்தில் விடிய விடிய நிகழ்த்தப்படும் தெரு நாடகக் கலையாகும். திரௌபதி அம்மன் திருவிழாக்களில் மகாபாரதக் கதைகளை மையமாகக் கொண்டு மரத்தாலான கிரீடங்கள், புஜக்கீர்த்திகள் மற்றும் வண்ண முக ஒப்பனைகளுடன் நிகழ்த்தப்படுகிறது.",
        "art_form_slug": "therukoothu",
        "district_code": "TN-TVM",
        "source_type": "ACADEMIC_RESEARCH",
        "source_name": "National School of Drama Traditional Theatre Fellowship",
        "source_url": "https://nsd.gov.in/purisai-therukoothu-treatise",
        "author": "Dr. K. Kannappa Thambiran",
        "chunks": [
            "Actors sing all dialogues in classical and folk ragas before paraphrasing the meaning in high-octane colloquial Tamil prose for the audience.",
            "The towering wooden Kireetam is hand-carved from lightweight Kalyana Murungai wood and embellished with mirror sequins and beads.",
            "The character of the Kattiyakkaran acts as narrator, jester, social commentator, and stage coordinator between scenes.",
            "Performances span eighteen consecutive nights to complete the full eighteen days of the Kurukshetra war ritual cycle."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000006",
        "title": "Thanjavur Painting: Relief Gesso and Gold Leaf Sacred Art",
        "title_ta": "தஞ்சாவூர் ஓவியம்: சுண்ணக்கட்டு புடைப்பு மற்றும் தங்கத் தகடு திருவுருவக் கலை",
        "content": "Tanjore Painting is a globally recognized classical South Indian painting style that originated in the 16th century under Nayaka and Maratha royal patronage. Distinguished by dense composition, glowing 22-karat gold foil overlays, semi-precious Jaipur stones, and three-dimensional gesso relief work (Makkol paste), it captures Hindu deities with round radiant faces and compassionate almond eyes.",
        "content_ta": "தஞ்சாவூர் ஓவியம் என்பது 16-ஆம் நூற்றாண்டில் நாயக்கர் மற்றும் மராட்டிய மன்னர்களின் ஆதரவுடன் வளர்ந்த புனித ஓவியக் கலையாகும். சுண்ணக்கட்டு புடைப்பு வேலைப்பாடு, 22 காரட் தூய தங்கத் தகடுகள் மற்றும் மணிக்கற்கள் பதித்து வரையப்படும் கண்கவர் கலைவடிவமாகும்.",
        "art_form_slug": "tanjore-painting",
        "district_code": "TN-THA",
        "source_type": "ACADEMIC_RESEARCH",
        "source_name": "Thanjavur Maratha Heritage Foundation",
        "source_url": "https://thanjavuroviyam.marabagam.org/treatise",
        "author": "Dr. Meenakshi Sundaram",
        "chunks": [
            "Tanjore Painting holds a Geographical Indication (GI) tag certifying authentic traditional craftsmanship from Thanjavur district.",
            "Gesso relief is formulated using pure unslaked chalk powder (Makkol) mixed with Arabic gum into a pliable sculptable paste.",
            "Authentic Tanjore paintings utilize 22-karat genuine gold foil that never oxidizes or tarnishes under tropical humidity.",
            "The sacred eye-opening ceremony (Nethronmeelanam) represents the artistic climax when the deity's compassionate gaze is rendered."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000007",
        "title": "Kanchipuram Three-Shuttle Korvai Weaving and Mulberry Silk",
        "title_ta": "காஞ்சிபுரம் முக்கூட்டு கோர்வணி பட்டு நெசவு வரலாற்று ஆவணம்",
        "content": "Kanchipuram Silk Saree weaving is a GI-tagged traditional handloom art from the temple town of Kanchipuram. Woven from high-twist mulberry silk and pure silver-gold electroplated zari, it is distinguished by the interlocking 'Korvai' technique where two weavers on a pit loom operate three shuttles simultaneously to bond contrasting borders and body seamlessly.",
        "content_ta": "காஞ்சிபுரம் பட்டு நெசவு என்பது புவிசார் குறியீடு பெற்ற உலகப் புகழ் பெற்ற கைத்தறி கலையாகும். மல்பெரி பட்டு, தூய ஜரிகை மற்றும் இருவர் இணைந்து இயக்கும் முக்கூட்டு கோர்வணி முறையில் நெய்யப்படுகிறது.",
        "art_form_slug": "kanchipuram-silk-weaving",
        "district_code": "TN-KANCH",
        "source_type": "HISTORICAL",
        "source_name": "Weavers Service Centre, Ministry of Textiles, Government of India",
        "source_url": "https://wschennai.gov.in/kanchipuram-silk-monograph",
        "author": "Shri V. Ramanathan",
        "chunks": [
            "Kanchipuram silk weaving has flourished for over four centuries, patronized historically by Vijayanagara rulers and temple trusts.",
            "The Korvai technique uses three shuttles to interlock contrasting borders so tightly that the border will never separate from the saree body.",
            "Authentic zari is manufactured from pure silver wire electroplated with 24-karat gold wrapped around a central silk core yarn.",
            "Motifs are inspired by Pallava temple architecture, including the temple gopuram, peacock eye (Mayilkan), and rudraksha beads."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000008",
        "title": "Swamimalai Bronze Icon Lost-Wax Casting Heritage",
        "title_ta": "சுவாமிமலை மெழுகு அச்சு வெண்கலச் சிற்ப வார்ப்பு மரபு",
        "content": "Swamimalai Bronze Casting is an ancient metal sculpture art practiced by hereditary Vishwakarma Sthapathis in Swamimalai, Thanjavur. Following the Shilpa Shastras and Dhyana Shlokas, divine icons are cast in Panchaloha (five-metal alloy: copper, zinc, lead, gold, silver) using the cire perdue (lost-wax) technique, where an original beeswax model is melted away and replaced with molten metal.",
        "content_ta": "சுவாமிமலை வெண்கலச் சிற்பக் கலை என்பது சோழர் காலம் தொட்டு ஸ்தபதிகளால் மெழுகு அச்சு (லாஸ்ட்-வாக்ஸ்) முறையில் வார்க்கப்படும் உலகப்புகழ் பெற்ற பஞ்சலோகச் சிற்பக் கலையாகும். சிற்ப சாஸ்திர விதிகளின்படி தேனீ மெழுகில் மாதிரி செய்யப்பட்டு உலோகக் குழம்பு ஊற்றி வார்க்கப்படுகிறது.",
        "art_form_slug": "swamimalai-bronze-casting",
        "district_code": "TN-THA",
        "source_type": "PRACTITIONER_MANUAL",
        "source_name": "Sthapathi Heritage Guild & Crafts Council of India",
        "source_url": "https://craftscouncilindia.org/swamimalai-bronzes",
        "author": "Sthapathi M. Radhakrishnan",
        "chunks": [
            "Proportions are determined by the Tala system, using an Angula (the width of the patron's finger) as the fundamental unit of sacred measurement.",
            "The wax model is prepared from pure beeswax, resin (Kungiliyam), and groundnut oil to create an exact sculptural prototype.",
            "The mould is fabricated using fine alluvial clay scooped from the Kaveri riverbed, applied in successive layers of increasing coarseness.",
            "After cooling, the clay mantle is smashed open (hence lost-mould), making every handcrafted bronze deity completely unique in the world."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000009",
        "title": "Pattamadai Fine Mat Weaving from Thamirabarani Sedge Reeds",
        "title_ta": "பத்தமடை பாய் நெசவு: தாமிரபரணி கோரைக் கைவினை மரபு",
        "content": "Pattamadai mat weaving is a delicate GI-tagged textile craft from Tirunelveli district. Skilled weavers harvest Korai marsh grass (Cyperus corymbosus) growing along the perennial Thamirabarani riverbanks. Reeds are split into wafer-thin strands, soaked in river water, and hand-woven on floor looms with silk or fine cotton warp threads to produce mats as supple as fine silk fabric.",
        "content_ta": "பத்தமடை பாய் நெசவு என்பது திருநெல்வேலி மாவட்டத்தின் புவிசார் குறியீடு பெற்ற நுண்ணிய கைவினை நெசவுக் கலையாகும். தாமிரபரணி ஆற்றங்கரையில் விளையும் கோரையைப் பிளந்து, ஊறவைத்து, பட்டு அல்லது பருத்தி நூலுடன் இணைத்து பட்டுப்போன்ற மிருதுவான பாய்களாக நெய்கின்றனர்.",
        "art_form_slug": "pattamadai-mat-weaving",
        "district_code": "TN-TIN",
        "source_type": "ACADEMIC_RESEARCH",
        "source_name": "National Institute of Design Craft Documentation Project",
        "source_url": "https://nid.edu/publications/pattamadai-korai-mats",
        "author": "R. Shanthi",
        "chunks": [
            "Pattamadai mats are classified into counts: coarse (50 count), medium (80-100 count), and supreme pattu pai (up to 140 count).",
            "Super-fine mats can be folded like a silk handkerchief and rolled effortlessly into a hollow bamboo tube for compact travel.",
            "Dyes were traditionally derived from medicinal plants including madder root, turmeric, and pomegranate rind before synthetic aniline pigments.",
            "A queen-size super-fine wedding mat requires over two months of continuous, meticulous hand weaving by master artisan women."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000010",
        "title": "Villupattu: The Musical Bow and Ballad Narration Tradition",
        "title_ta": "வில்லுப்பாட்டு: வில்லிசை மற்றும் காவிய கதைசொல்லல் மரபு",
        "content": "Villupattu (musical bow singing) is a traditional narrative art form prominent in the southern districts of Tirunelveli, Kanyakumari, and Thoothukudi. A large seven-foot wooden bow strung with a strong cord is resting over an earthen terracotta pot (Kudam). Little bronze bells hang suspended along the bow stave. The lead singer strikes the bowstring with two wooden batons (Veesukol) while narrating historical legends and devotional epics in alternating song and prose.",
        "content_ta": "வில்லுப்பாட்டு என்பது தென்தமிழகத்தின் வரலாற்று காவியங்களைப் பாடி விவரிக்கும் வில்லிசைக் கலையாகும். ஒரு பெரிய வில்லின் நாணில் வெண்கல மணிகள் கட்டப்பட்டு, மண் குடத்தின் மீது பொருத்தப்பட்டு, வீசுக்கோல் கொண்டு அடித்து இசைக்கப்படுகிறது.",
        "art_form_slug": "villupattu",
        "district_code": "TN-TIN",
        "source_type": "ORAL_TRADITION",
        "source_name": "Tamil University Folk Arts & Folklore Division",
        "source_url": "https://tamiluniversity.ac.in/folklore/villupattu-tradition",
        "author": "Dr. A. Sivasubramanian",
        "chunks": [
            "Striking the bowstring with wooden rods rings the bells simultaneously while producing rhythmic percussive thuds on the string.",
            "The accompanist beats the open mouth of the mud pot (Kudam) using a flat leather palm-pad, generating deep resonant bass accents.",
            "The co-narrator (Pinpattukkarar) humorously questions the chief bard, creating engaging extempore dramatic dialogue for the village assembly.",
            "Ballads preserve local heroic folklore, temple histories, and community chronicles that never entered written court archives."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000011",
        "title": "Nadaswaram and Thavil: Mangala Isai and Temple Acoustic Traditions",
        "title_ta": "நாதசுவரம் மற்றும் தவில்: மங்கள இசை மற்றும் கோவில் ஒலி மரபுகள்",
        "content": "The Nadaswaram is one of the world's most powerful non-brass acoustic double-reed aerophones, paired harmoniously with the Thavil drum in the sacred ensemble known as Mangala Isai. Carved from seasoned Aacha wood (hardwood timber), its piercing, expressive timbre carries over wide open-air temple spaces during festival processions and ceremonial rituals.",
        "content_ta": "நாதசுவரம் மற்றும் தவில் இணைந்து இசைக்கப்படும் மங்கள இசை தமிழகத்தின் ஆன்மிக மற்றும் பண்பாட்டு அடையாளமாகும். ஆச்சா மரத்தால் செய்யப்படும் நாதசுவரமும், பலா மரத்தால் செய்யப்படும் தவிலும் திறந்தவெளி கோவில் திருவிழாக்களில் கம்பீரமாக முழங்குகின்றன.",
        "art_form_slug": "nadaswaram",
        "district_code": "TN-THA",
        "source_type": "ACADEMIC_RESEARCH",
        "source_name": "The Music Academy Madras Research Journal",
        "source_url": "https://musicacademymadras.in/journal/mangala-isai-study",
        "author": "Vidwan B. Rajagopalan",
        "chunks": [
            "The double reed (Seevali) is crafted from wild river reeds harvested in Kaveri delta marshes, requiring delicate razor-blade scraping.",
            "Temple processions require playing the ancient ceremonial Mallari rhythm cycle to honor the majestic gait of the bronze deity.",
            "The Thavil player wears plaster-hardened cloth finger thimbles (Koodu) on one hand and uses a tamarind wood mallet on the other.",
            "The complex rhythmic exchanges between Nadaswaram and Thavil represent the apex of South Indian improvisational tala mathematics."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000012",
        "title": "Tholpavai Koothu: Perforated Shadow Puppetry of Tamil Nadu",
        "title_ta": "தோல்பாவை கூத்து: தமிழகத்தின் துளையிடப்பட்ட நிழற்பாவை நாடக மரபு",
        "content": "Tholpavai Koothu is the traditional shadow puppet theatre of Tamil Nadu and Kerala border regions. Flat translucent puppets crafted from tanned and treated deer or goat hide are perforated with delicate filigree cut-outs and hand-painted with vegetable dyes. Puppeteers manipulate them behind a white cloth screen illuminated by oil lamps, enacting the Kamba Ramayanam epic.",
        "content_ta": "தோல்பாவைக் கூத்து என்பது பதப்படுத்தப்பட்ட ஆட்டுத் தோலில் நுணுக்கமான துளைகள் இட்டு வண்ணங்கள் தீட்டி, வெள்ளை திரைச்சீலையின் பின்னே எண்ணெய் விளக்கு ஒளியில் இயக்கப்படும் நிழற்பாவை நாடகமாகும். கம்பராமாயணக் கதைகள் இதில் முதன்மையாகப் பாடப்படுகின்றன.",
        "art_form_slug": "tholpavai-koothu",
        "district_code": "TN-KK",
        "source_type": "ORAL_TRADITION",
        "source_name": "International Puppetry Association (UNIMA India)",
        "source_url": "https://unimaindia.org/tholpavai-tamil-shadow-puppetry",
        "author": "Pulavar K. Lakshmana Rao",
        "chunks": [
            "Goat hides are scraped wafer-thin until translucent and cured under sunlight without chemical tanning to retain rigidity.",
            "Perforations allow pinpoint rays of oil-lamp light to pierce the leather silhouettes, producing jewel-like glowing details.",
            "Puppeteers manipulate jointed limbs using slender split bamboo rods while simultaneously vocalizing multiple distinct character voices.",
            "Performances in Kali and Bhagavati temples serve as sacred nocturnal vows to ward off agrarian drought and epidemics."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000013",
        "title": "Traditional Kolam: Sacred Mathematics, Symmetry, and Ephemeral Rice Art",
        "title_ta": "பாரம்பரியக் கோலம்: புனித வடிவியல், சமச்சீர்மை மற்றும் அரிசி மாவு சித்திரக் கலை",
        "content": "Kolam is an everyday ritual floor art drawn by women across Tamil Nadu at dawn using coarse white rice flour (Arisi Maavu). More than decorative ornament, it embodies sacred geometry, topological knot theory, rotational symmetry, and hospitality. Providing edible rice flour at the doorstep serves as a daily offering of charity (Bhoota Yajna) to feeding ants, birds, and insects.",
        "content_ta": "கோலம் என்பது விடியற்காலையில் தமிழ் பெண்களால் வீட்டு வாயிலில் அரிசி மாவு கொண்டு வரையப்படும் புனித வடிவியல் கலைவடிவமாகும். இது சமச்சீர் வடிவியல், புள்ளிக் கோடுகள் மற்றும் எறும்புகள் பறவைகளுக்கு உணவளிக்கும் பூத யாகத்தின் அடையாளமாகும்.",
        "art_form_slug": "traditional-kolam",
        "district_code": "TN-CHE",
        "source_type": "ACADEMIC_RESEARCH",
        "source_name": "Institute of Mathematical Sciences & Tamil Heritage Trust",
        "source_url": "https://imsc.res.in/kolam-topology-study",
        "author": "Dr. Gift Siromoney",
        "chunks": [
            "Pulli Kolam begins with a matrix of dots (Pulli) around or through which unbroken continuous looped lines (Sikku) are drawn.",
            "Computer scientists and mathematicians worldwide study Tamil Sikku Kolams as visual examples of picture languages and array grammars.",
            "During the sacred Tamil month of Margazhi, intricate colour Rangolis and red brick powder borders (Kaavi) adorn every street entrance.",
            "Rice flour provides microscopic ecological nourishment to household fauna, fulfilling the ancient Sangam ethic of harmonious co-existence."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000014",
        "title": "Kavadi Attam: Devotional Trance Dance and Symmetrical Burden",
        "title_ta": "காவடி ஆட்டம்: பக்திப் பரவச நடனம் மற்றும் பாரச் சமநிலை மரபு",
        "content": "Kavadi Attam is an intense devotional dance dedicated to Lord Murugan, practiced during festivals like Thaipusam and Panguni Uthiram. Bearing a semi-circular wooden arch (Kavadi) decorated with peacock feathers and brass pots of milk or rosewater across the shoulders, pilgrims dance for miles in trance-like states accompanied by frantic percussion rhythms.",
        "content_ta": "காவடி ஆட்டம் என்பது முருகப் பெருமானின் வழிபாட்டுடன் தொடர்புடைய பக்தி நடனக் கலையாகும். மயில் தோகைகள் மற்றும் பால் குடங்கள் பொருத்தப்பட்ட மர வளைவைக் தோள்களில் சுமந்து தைப்பூசம், பங்குனி உத்திர விழாக்களில் பரவச நடனமாடுகின்றனர்.",
        "art_form_slug": "kavadi-attam",
        "district_code": "TN-DIN",
        "source_type": "ORAL_TRADITION",
        "source_name": "Palani Murugan Temple Devasthanam Archives",
        "source_url": "https://palanimurugan.hrce.tn.gov.in/kavadi-heritage",
        "author": "Dr. R. Thirumavalavan",
        "chunks": [
            "The Kavadi symbolizes Mounts Shivagiri and Shaktigiri borne by Idumban, testing the ascetic endurance and surrender of the pilgrim.",
            "Varieties include Paal Kavadi (milk pots), Theertha Kavadi (sacred river water), Pushpa Kavadi (fresh flowers), and Mayil Kavadi (peacock plumage).",
            "The physical balance requires rhythmic torso swaying, knee bends, and circular turns without dislodging the heavy wooden arch.",
            "Music is powered by Kavadi Chindu melodies and hypnotic Urumi Melam rhythms that heighten spiritual focus and suppress physical exhaustion."
        ]
    },
    {
        "id": "f1000000-0000-0000-0000-000000000015",
        "title": "Oyilattam and Kummi: Communal Harmony and Graceful Folk Dancing",
        "title_ta": "ஒயிலாட்டம் மற்றும் கும்மி: சமூக ஒற்றுமை மற்றும் எழில்மிகு நாட்டுப்புற நடனம்",
        "content": "Oyilattam (dance of grace) and Kummi (rhythmic hand-clapping circle dance) are vibrant community dance traditions of Tamil Nadu. In Oyilattam, men or mixed groups dance in rows waving colorful handkerchiefs in synchrony to Thavil and Urumi beats. Kummi is one of the world's most ancient circular clapping dances, preserved by women during harvest festivals, temple pageants, and family milestones.",
        "content_ta": "ஒயிலாட்டம் மற்றும் கும்மி என்பன தமிழகத்தின் சமூக ஒற்றுமையையும் உழவர் பண்பாட்டையும் வெளிப்படுத்தும் கூட்டு நடனங்களாகும். ஒயிலாட்டத்தில் வண்ண கைக்குட்டைகளை அசைத்து வரிசையாக ஆட, கும்மியில் பெண்கள் வட்டமாக நின்று கைகளைத் தட்டி பாடியவாறு ஆடுகின்றனர்.",
        "art_form_slug": "oyilattam",
        "district_code": "TN-COI",
        "source_type": "ACADEMIC_RESEARCH",
        "source_name": "Bharathiar University Folk Dance Archives",
        "source_url": "https://b-u.ac.in/folklore/oyilattam-monograph",
        "author": "Dr. K. Chinnappa",
        "chunks": [
            "Oyilattam dancers wear ankle bells (Salangai) and wave vibrant red or yellow kerchiefs, executing sharp turns in unison.",
            "The dance steps progress from Vilambakkalam (slow dignified gait) to accelerating Madhyamakkalam and explosive Dhrudakkalam.",
            "Kummi requires no instrumental accompaniment; the participants' rhythmic palms and responsive singing provide complete percussive harmony.",
            "Valli Kummi and Mulaipari Kummi celebrate agricultural prosperity, female solidarity, and indigenous seeds."
        ]
    }
]
