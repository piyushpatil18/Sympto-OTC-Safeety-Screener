package com.example.data

object PharmacistBrain {

    // --- 1. EMERGENCY SIGNALS (Red Flags) ---
    fun getRedFlags(category: SymptomCategory): List<String> {
        return when (category) {
            SymptomCategory.FEVER_ACHE -> listOf(
                "Neck stiffness or high fever combined with confusion",
                "New onset chest pain or severe breathing difficulty",
                "Severe unsupportable headache or dynamic rash with flat purple spots",
                "Inability to stay awake or localized limb weakness"
            )
            SymptomCategory.ACID_REFLUX -> listOf(
                "Chest pain shifting/spreading into the neck, jaw, shoulders, or arms",
                "Vomiting bright red blood or dark coffee-grounds-like substance",
                "Black, sticky, or tarry bowel movements (indicates gastrointestinal bleeding)",
                "Severe difficulty or painful swallowing of food or fluids"
            )
            SymptomCategory.COLD_CONGESTION -> listOf(
                "Audible wheezing or high-pitched sound (Stridor) when breathing",
                "Bluish color of the lips, face, or fingernail beds (Hypoxia)",
                "Coughing up blood or frothy pink sputum",
                "Severe high temperature failing to respond even slightly to antipyretics for 4+ days"
            )
            SymptomCategory.SNEEZE_ALLERGY -> listOf(
                "Sudden swelling of the tongue, throat, lips, or soft facial tissues",
                "Tightness in chest, severe gasping, or inability to catch your breath",
                "Rapidly spreading severe hives coupled with dizziness or lightheadedness",
                "Anaphylactic feeling (rapid warm flush + sudden drop in blood pressure)"
            )
            SymptomCategory.BLOATING_GAS -> listOf(
                "Severe, acute, sharp pain concentrated in the lower right quadrant (Appendicitis warning)",
                "Inability to pass any gas or stool combined with progressive abdominal distinction",
                "Constant projectile vomiting or high fever above 101°F/38.3°C",
                "Extremely hard, rigid, or board-like stomach tender to even light touch"
            )
        }
    }

    // --- 2. DURATION LADDER INTERPRETATION ---
    data class LadderAdvice(
        val level: String, // "Primary Care", "Targeted OTC", "Advanced & Warning", "Absolute Referral"
        val steps: List<String>,
        val allowOtcSuggestions: Boolean
    )

    fun getLadderAdvice(category: SymptomCategory, days: Int): LadderAdvice {
        if (days >= 7) {
            return LadderAdvice(
                level = "Doctor Consultation Required",
                steps = listOf(
                    "Symptom duration exceeds typical self-care timelines (7+ days).",
                    "Do NOT generic medicate further with over-the-counter options.",
                    "Consult a licensed health practitioner for a thorough medical diagnosis.",
                    "An infection or underlying chronic condition may be present requiring prescription medications."
                ),
                allowOtcSuggestions = false
            )
        }
        return when (category) {
            SymptomCategory.FEVER_ACHE -> {
                when {
                    days <= 2 -> LadderAdvice(
                        level = "Supportive & Baseline Hydration",
                        steps = listOf(
                            "Focus heavily on electrolyte fluids and active resting state.",
                            "Warm or room-temperature sponge baths can assist cooling safely.",
                            "Single ingredient fever reducer is recommended on an as-needed basis."
                        ),
                        allowOtcSuggestions = true
                    )
                    days <= 4 -> LadderAdvice(
                        level = "Standard Target Fever Control",
                        steps = listOf(
                            "Administer anti-inflammatory relief on a fixed, safe schedule (watch max daily limits).",
                            "Track body temperature every 6 hours and document it.",
                            "Review other subtle symptoms like joint aches or sweating patterns."
                        ),
                        allowOtcSuggestions = true
                    )
                    else -> LadderAdvice(
                        level = "Extended Fever Alert",
                        steps = listOf(
                            "Fever persisting for 5-6 days warrants active healthcare practitioner oversight.",
                            "Avoid stacking multisyndrome cold tablets to prevent liver strain.",
                            "If symptoms aggravate or show zero signs of recovery, schedule an appointment immediately."
                        ),
                        allowOtcSuggestions = true
                    )
                }
            }
            SymptomCategory.ACID_REFLUX -> {
                when {
                    days <= 2 -> LadderAdvice(
                        level = "Immediate Direct Antacids",
                        steps = listOf(
                            "Utilize fast-acting local antacids to coat stomach lining rapidly.",
                            "Avoid lying down style post meals for at least 3 hours.",
                            "Remove carbonated sips, spicy curries, citrus, or heavy caffeine inputs."
                        ),
                        allowOtcSuggestions = true
                    )
                    days <= 4 -> LadderAdvice(
                        level = "Acid Suppression (H2 Blockers)",
                        steps = listOf(
                            "Opt for longer-acting H2 blockers on an empty stomach to limit cumulative acid.",
                            "Keep your dinner lightweight, focusing on bland dry toast or porridge.",
                            "Raise head of bed by 6 inches using strong risers."
                        ),
                        allowOtcSuggestions = true
                    )
                    else -> LadderAdvice(
                        level = "Chronic Burn Warning",
                        steps = listOf(
                            "Persistent acid reflux for 5+ days could indicate GERD (Gastroesophageal Reflux Disease).",
                            "Limit prolonged antacid tablet chewing containing high calcium to avoid kidney stone burden.",
                            "Prepare to seek consult if swallowing begins to feel restricted in any way."
                        ),
                        allowOtcSuggestions = true
                    )
                }
            }
            SymptomCategory.COLD_CONGESTION -> {
                when {
                    days <= 2 -> LadderAdvice(
                        level = "Moisture & Decongestion",
                        steps = listOf(
                            "Perform manual saline nasal rinses twice daily.",
                            "Utilize hot showers or room cool-mist humidifiers to loosen dense mucus.",
                            "Stay extremely well hydrated with herbal teas or warm broths."
                        ),
                        allowOtcSuggestions = true
                    )
                    days <= 4 -> LadderAdvice(
                        level = "Active Nasal / Cough Therapy",
                        steps = listOf(
                            "Incorporate target chest rubs or single-agent antihistamines/decongestants.",
                            "If cough is non-productive, a gentle suppressant may help sleep.",
                            "Do not combine multiple over-the-counter multi-symptom cold products."
                        ),
                        allowOtcSuggestions = true
                    )
                    else -> LadderAdvice(
                        level = "Escalated Congestion Risk",
                        steps = listOf(
                            "Day 5-6 with zero congestion relief may suggest a secondary bacterial sinus or chest infection.",
                            "Keep vigil over colored thick yellow/green phlegm or sinus-bone localized pain.",
                            "Prepare to transition from self-care to expert medical evaluation."
                        ),
                        allowOtcSuggestions = true
                    )
                }
            }
            SymptomCategory.SNEEZE_ALLERGY -> {
                when {
                    days <= 2 -> LadderAdvice(
                        level = "Targeted Allergen Blockers",
                        steps = listOf(
                            "Initiate second-generation non-drowsy antihistamines to suppress systemic histamine release.",
                            "Close living windows during peak pollen spikes (morning & evening dry wind).",
                            "Wash face and hands immediately upon entering indoors."
                        ),
                        allowOtcSuggestions = true
                    )
                    days <= 4 -> LadderAdvice(
                        level = "Secondary Nasal Corticosteroid",
                        steps = listOf(
                            "Incorporate daily nasal corticosteroid sprays for long-term mucosal healing.",
                            "Consistency is vital—sprays take 36-48 hours to unlock full protective capabilities.",
                            "Keep eyes lubricated with basic lubricating artificial teardrops."
                        ),
                        allowOtcSuggestions = true
                    )
                    else -> LadderAdvice(
                        level = "Allergic Flare Control",
                        steps = listOf(
                            "If severe sneezing and eye irritation persist for 5+ days, seek specialist advice.",
                            "Identify triggers and keep note of air filters cleanliness.",
                            "Be cautious with sedating antihistamines to prevent hazardous daytime drowsiness."
                        ),
                        allowOtcSuggestions = true
                    )
                }
            }
            SymptomCategory.BLOATING_GAS -> {
                when {
                    days <= 2 -> LadderAdvice(
                        level = "Enzymes & Gas Repression",
                        steps = listOf(
                            "Administer gas-dispersing simethicone drops or tablets.",
                            "Sip fresh ginger or peppermint tea slowly post meals.",
                            "Engage in brief 15-minute slow walking to stimulate active intestinal motility naturally."
                        ),
                        allowOtcSuggestions = true
                    )
                    days <= 4 -> LadderAdvice(
                        level = "Gut Rest & Probiotic Support",
                        steps = listOf(
                            "Incorporate high-potency digestive enzymes or simple active probiotic cultures.",
                            "Temporarily eliminate high-FODMAP foods (cabbage, beans, dairy, onions).",
                            "Chew every single bite of solid food at least 20 times slower."
                        ),
                        allowOtcSuggestions = true
                    )
                    else -> LadderAdvice(
                        level = "Extended Gut Distention Alert",
                        steps = listOf(
                            "Gas and bloating persisting for nearly a week requires active gastroenterology evaluation.",
                            "Check for signs like persistent alternating constipation or loose bowels.",
                            "Limit chemical sweeteners (sorbitol, mannitol) immediately as they worsen gas."
                        ),
                        allowOtcSuggestions = true
                    )
                }
            }
        }
    }


    // --- 3. REGIONAL BRANDING RECOMMENDATIONS ---

    fun getRegionalOtcSuggestions(
        category: SymptomCategory,
        region: Region,
        userMeds: List<UserMedication>
    ): Pair<List<OtcBrand>, String?> {
        // Evaluate interactions first
        val hazards = checkInteractions(category, userMeds)
        val warnedMessage = hazards.firstOrNull()?.warningMessage

        val items = when (region) {
            Region.INDIA -> {
                when (category) {
                    SymptomCategory.FEVER_ACHE -> {
                        val baseBrands = mutableListOf<OtcBrand>()
                        // If no NSAID warning, include Ibuprofen-based
                        val isNsaidHazard = hazards.any { it.hazardousIngredient.lowercase() == "ibuprofen" }

                        baseBrands.add(
                            OtcBrand(
                                brandName = "Crocin Pain Relief",
                                activeIngredient = "Paracetamol (500mg) + Caffeine (50mg)",
                                recommendedDose = "1-2 tablets every 4 to 6 hours as needed. Max 8 tablets in 24 hours.",
                                shelfLocation = "Aisle 1, Shelf A (Fever & Pain Care)",
                                standardPrice = "₹35 / strip"
                            )
                        )
                        baseBrands.add(
                            OtcBrand(
                                brandName = "Dolo-650",
                                activeIngredient = "Paracetamol (650mg)",
                                recommendedDose = "1 tablet every 6 hours. Absolutely do not cross 4g (6 tablets) per day.",
                                shelfLocation = "Aisle 1, Shelf B (Fever Management)",
                                standardPrice = "₹30 / strip"
                            )
                        )

                        if (!isNsaidHazard) {
                            baseBrands.add(
                                OtcBrand(
                                    brandName = "Combiflam",
                                    activeIngredient = "Ibuprofen (400mg) + Paracetamol (325mg)",
                                    recommendedDose = "1 tablet after meals, 2-3 times daily max. Avoid on empty stomach.",
                                    shelfLocation = "Aisle 1, Shelf C (Inflammatory Pain)",
                                    standardPrice = "₹45 / strip"
                                )
                            )
                        }
                        baseBrands
                    }
                    SymptomCategory.ACID_REFLUX -> listOf(
                        OtcBrand(
                            brandName = "Digene Mint Gel",
                            activeIngredient = "Magnesium Hydroxide + Aluminium Hydroxide + Simethicone",
                            recommendedDose = "2 teaspoons (10ml) after meals or at onset of heartburn. Shake bottle well.",
                            shelfLocation = "Aisle 4, Shelf A (Antacids & Liquids)",
                            standardPrice = "₹150 / bottle"
                        ),
                        OtcBrand(
                            brandName = "ENO Fruit Salt (Regular/Lemon)",
                            activeIngredient = "Svarjiksara (Sodium Bicarbonate) + Nimbukamra (Citric Acid)",
                            recommendedDose = "1 sachet dissolved in 150ml water. Drink immediately. Repeat if needed after 2-3 hours.",
                            shelfLocation = "Aisle 4, Shelf D (Fast Gas Relief)",
                            standardPrice = "₹9 / sachet"
                        )
                    )
                    SymptomCategory.COLD_CONGESTION -> listOf(
                        OtcBrand(
                            brandName = "Crocin Cold & Flu Max",
                            activeIngredient = "Paracetamol (500mg) + Caffeine (32mg) + Phenylephrine HCl (5mg)",
                            recommendedDose = "1 tablet every 6 hours. May cause mild wakefulness due to caffeine.",
                            shelfLocation = "Aisle 2, Shelf B (Nasal & Cold)",
                            standardPrice = "₹60 / strip"
                        ),
                        OtcBrand(
                            brandName = "Otrivin Adult Nasal Spray",
                            activeIngredient = "Xylometazoline HCl (0.1%)",
                            recommendedDose = "1 spray in each nostril, 2-3 times daily. Do not use for more than 5 consecutive days!",
                            shelfLocation = "Aisle 2, Shelf C (Nasal Sprays)",
                            standardPrice = "₹110 / spray"
                        )
                    )
                    SymptomCategory.SNEEZE_ALLERGY -> {
                        val isAntihistamineHazard = hazards.any { it.hazardousIngredient.lowercase().contains("antihistamine") }
                        val base = mutableListOf<OtcBrand>()

                        base.add(
                            OtcBrand(
                                brandName = "Alerid",
                                activeIngredient = "Cetirizine HCl (10mg)",
                                recommendedDose = "1 tablet daily at bedtime. Non-drowsy but limit driving if sleepy.",
                                shelfLocation = "Aisle 2, Shelf F (Allergy Care)",
                                standardPrice = "₹40 / strip"
                            )
                        )
                        if (!isAntihistamineHazard) {
                            base.add(
                                OtcBrand(
                                    brandName = "Avil 25",
                                    activeIngredient = "Pheniramine Maleate (22.75mg)",
                                    recommendedDose = "0.5 to 1 tablet at night. WARNING: Highly sedating. Absolutely avoid physical machinery.",
                                    shelfLocation = "Aisle 2, Shelf G (Classic Antihistamines)",
                                    standardPrice = "₹12 / strip"
                                )
                            )
                        }
                        base
                    }
                    SymptomCategory.BLOATING_GAS -> listOf(
                        OtcBrand(
                            brandName = "Gas-O-Fast Active Ajwain",
                            activeIngredient = "Sodium Bicarbonate + Citric Acid + Trachyspermum ammi extracts",
                            recommendedDose = "1 sachet in lukewarm water post heavy meals. Natural flatulence relief.",
                            shelfLocation = "Aisle 4, Shelf E (Ayurvedic OTC)",
                            standardPrice = "₹10 / sachet"
                        ),
                        OtcBrand(
                            brandName = "Digene Mint Tablets (Chewable)",
                            activeIngredient = "Aluminium Hydroxide + Magnesium Aluminium Silicate Hydrate",
                            recommendedDose = "Chew 2 to 4 tablets thoroughly post meals. Do not swallow whole.",
                            shelfLocation = "Aisle 4, Shelf B (Digestive Tablets)",
                            standardPrice = "₹25 / strip"
                        )
                    )
                }
            }
            Region.USA -> {
                when (category) {
                    SymptomCategory.FEVER_ACHE -> {
                        val baseBrands = mutableListOf<OtcBrand>()
                        val isNsaidHazard = hazards.any { it.hazardousIngredient.lowercase() == "ibuprofen" }

                        baseBrands.add(
                            OtcBrand(
                                brandName = "Tylenol Extra Strength",
                                activeIngredient = "Acetaminophen (500mg)",
                                recommendedDose = "1-2 gelcaps every 6 hours as needed. Do not exceed 3,000mg (6 gelcaps) in 24 hours.",
                                shelfLocation = "Aisle 5, Shelf A (Pain & Fever)",
                                standardPrice = "$9.49 / 24ct"
                            )
                        )

                        if (!isNsaidHazard) {
                            baseBrands.add(
                                OtcBrand(
                                    brandName = "Advil Liqui-Gels",
                                    activeIngredient = "Ibuprofen (200mg)",
                                    recommendedDose = "1-2 capsules every 4 to 6 hours over meals. Max 6 capsules in 24 hours.",
                                    shelfLocation = "Aisle 5, Shelf C (Anti-inflammatory/NSAIDs)",
                                    standardPrice = "$11.99 / 40ct"
                                )
                            )
                        }
                        baseBrands
                    }
                    SymptomCategory.ACID_REFLUX -> listOf(
                        OtcBrand(
                            brandName = "Tums Ultra Strength 1000",
                            activeIngredient = "Calcium Carbonate (1000mg)",
                            recommendedDose = "Chew 2 to 3 tablets as symptoms arise. Maximum 5 tablets daily.",
                            shelfLocation = "Aisle 8, Shelf A (Rapid Antacids)",
                            standardPrice = "$7.29 / 72ct"
                        ),
                        OtcBrand(
                            brandName = "Pepcid AC Maximum Strength",
                            activeIngredient = "Famotidine (20mg) [H2 Blocker]",
                            recommendedDose = "Take 1 tablet with a glass of water 15-60 mins before trigger meals. Max 2 daily.",
                            shelfLocation = "Aisle 8, Shelf C (Acid Reducers)",
                            standardPrice = "$14.99 / 25ct"
                        )
                    )
                    SymptomCategory.COLD_CONGESTION -> listOf(
                        OtcBrand(
                            brandName = "Tylenol Cold + Flu Severe",
                            activeIngredient = "Acetaminophen (325mg) + Dextromethorphan HBr (10mg) + Phenylephrine HCl (5mg) + Guaifenesin (200mg)",
                            recommendedDose = "2 caplets every 4 hours while symptoms persist. Do not usage other acetaminophen.",
                            shelfLocation = "Aisle 3, Shelf B (Cold & Congestion)",
                            standardPrice = "$10.49 / 24ct"
                        ),
                        OtcBrand(
                            brandName = "Sudafed PE nasal",
                            activeIngredient = "Phenylephrine HCl (10mg)",
                            recommendedDose = "1 tablet every 4 hours. Do not exceed 6 tablets daily.",
                            shelfLocation = "Aisle 3, Shelf D (Decongestants)",
                            standardPrice = "$8.99 / 18ct"
                        )
                    )
                    SymptomCategory.SNEEZE_ALLERGY -> {
                        val isAntihistamineHazard = hazards.any { it.hazardousIngredient.lowercase().contains("antihistamine") }
                        val base = mutableListOf<OtcBrand>()

                        base.add(
                            OtcBrand(
                                brandName = "Claritin 24 Hour",
                                activeIngredient = "Loratadine (10mg)",
                                recommendedDose = "1 tablet daily. Fully non-drowsy formulation.",
                                shelfLocation = "Aisle 4, Shelf A (24HR Non-Drowsy)",
                                standardPrice = "$22.99 / 30ct"
                            )
                        )
                        if (!isAntihistamineHazard) {
                            base.add(
                                OtcBrand(
                                    brandName = "Benadryl Allergy Ultratabs",
                                    activeIngredient = "Diphenhydramine HCl (25mg)",
                                    recommendedDose = "1 to 2 tablets every 4 to 6 hours. May cause extreme drowsiness. Do not drive.",
                                    shelfLocation = "Aisle 4, Shelf F (Sleep & Antihistamine)",
                                    standardPrice = "$6.49 / 24ct"
                                )
                            )
                        }
                        base
                    }
                    SymptomCategory.BLOATING_GAS -> listOf(
                        OtcBrand(
                            brandName = "Gas-X Extra Strength",
                            activeIngredient = "Simethicone (125mg)",
                            recommendedDose = "Chew 1 or 2 softgels post meals or before bed. Max 4 daily.",
                            shelfLocation = "Aisle 8, Shelf F (Gas Remediation)",
                            standardPrice = "$7.99 / 18ct"
                        ),
                        OtcBrand(
                            brandName = "Pepto-Bismol Liquid chewables",
                            activeIngredient = "Bismuth Subsalicylate (262mg)",
                            recommendedDose = "Chew 2 tablets or swallow 30ml every 30-60 mins as needed. Max 8 doses in 24 hrs.",
                            shelfLocation = "Aisle 8, Shelf E (Pink Stomach Care)",
                            standardPrice = "$8.49 / 12 fl.oz"
                        )
                    )
                }
            }
        }
        return Pair(items, warnedMessage)
    }

    // --- 4. DRUG INTERACTION GATE ENGINE ---

    fun checkInteractions(
        category: SymptomCategory,
        userMeds: List<UserMedication>
    ): List<InteractionCheck> {
        val triggered = mutableListOf<InteractionCheck>()
        val medsLower = userMeds.map { it.name.lowercase() + " " + it.activeIngredient.lowercase() }

        for (med in medsLower) {
            // WARFARIN checks
            if (med.contains("warfarin") || med.contains("coumadin") || med.contains("clopidogrel") || med.contains("plavix") || med.contains("blood thinner")) {
                if (category == SymptomCategory.FEVER_ACHE) {
                    triggered.add(
                        InteractionCheck(
                            currentMedRequiredIngredient = "Warfarin / Blood Thinner",
                            hazardousIngredient = "Ibuprofen",
                            severity = "Critical",
                            warningMessage = "CRITICAL: You are taking a blood thinner. Stacking Ibuprofen (Combiflam/Advil) carries a high risk of life-threatening gastrointestinal bleeding. Use Paracetamol/Acetaminophen (Dolo/Tylenol) instead!"
                        )
                    )
                }
                if (category == SymptomCategory.BLOATING_GAS) {
                    triggered.add(
                        InteractionCheck(
                            currentMedRequiredIngredient = "Warfarin",
                            hazardousIngredient = "Bismuth Subsalicylate",
                            severity = "High",
                            warningMessage = "WARNING: Pepto-Bismol contains salicylate (similar to Aspirin) which increases anticoagulant action, highly boosting bleeding risks. Choose Simethicone (Gas-X) instead."
                        )
                    )
                }
            }

            // ASPIRIN checks
            if (med.contains("aspirin") || med.contains("ecotrin") || med.contains("disprin")) {
                if (category == SymptomCategory.FEVER_ACHE) {
                    triggered.add(
                        InteractionCheck(
                            currentMedRequiredIngredient = "Aspirin",
                            hazardousIngredient = "Ibuprofen / NSAIDs",
                            severity = "Moderate",
                            warningMessage = "MODERATE WARNING: Combining Aspirin with Ibuprofen can block Aspirin's cardio-protective benefits and increase gastritis/stomach ulceration risks. Paracetamol is safer here."
                        )
                    )
                }
            }

            // SSRI (Fluoxetine, Sertraline, Lexapro, Escitalopram) checks
            if (med.contains("fluoxetine") || med.contains("prozac") || med.contains("sertraline") || med.contains("zoloft") || med.contains("escitalopram") || med.contains("lexapro")) {
                if (category == SymptomCategory.FEVER_ACHE) {
                    triggered.add(
                        InteractionCheck(
                            currentMedRequiredIngredient = "SSRI Antidepressant",
                            hazardousIngredient = "Ibuprofen",
                            severity = "Moderate",
                            warningMessage = "MODERATE: SSRIs combined with Ibuprofen (NSAID) significantly increase gastrointestinal mucosa bleeding risk. Acetaminophen/Paracetamol is preferred."
                        )
                    )
                }
            }

            // EXISTING ANTIHISTAMINES (prevent double dosing)
            if (med.contains("cetirizine") || med.contains("alerid") || med.contains("zyrtec") || med.contains("loratadine") || med.contains("claritin") || med.contains("fexofenadine") || med.contains("allegra") || med.contains("allergy pill")) {
                if (category == SymptomCategory.SNEEZE_ALLERGY || category == SymptomCategory.COLD_CONGESTION) {
                    triggered.add(
                        InteractionCheck(
                            currentMedRequiredIngredient = "Antihistamine Medication",
                            hazardousIngredient = "Antihistamine OTC (Avil / Benadryl)",
                            severity = "High",
                            warningMessage = "DOUBLE-DOSING WARNING: You are already taking an allergy antihistamine. Registering Avil, Cetirizine, or Diphenhydramine (Benadryl) risk severe toxicity, double-dosing, and dangerous extreme sedation."
                        )
                    )
                }
            }

            // METFORMIN / SUGAR CONTROLS
            if (med.contains("metformin") || med.contains("insulin") || med.contains("glycomet") || med.contains("sugar pill") || med.contains("glimepiride")) {
                if (category == SymptomCategory.COLD_CONGESTION) {
                    triggered.add(
                        InteractionCheck(
                            currentMedRequiredIngredient = "Diabetes Medication",
                            hazardousIngredient = "Cold Syrups / Multi-Symptom",
                            severity = "Moderate",
                            warningMessage = "DIABETES CAUTION: Liquid cold mixtures often contain substantial liquid sugars. Choose dry tablet/caplet formulations instead to maintain stable glucose control."
                        )
                    )
                }
            }
        }
        return triggered
    }
}
