package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScreenerViewModel(application: Application) : AndroidViewModel(application) {

    // Global Database Initializer
    private val db = Room.databaseBuilder(
        application,
        ScreenerDatabase::class.java, "screener_secure_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = ScreenerRepository(db.screenerDao())

    // --- Dynamic User Configs ---
    val currentMedications: StateFlow<List<UserMedication>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val consultationHistory: StateFlow<List<ConsultationHistory>> = repository.consultationHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Persistent known allergies via SharedPreferences
    private val prefs = application.getSharedPreferences("screener_profile_prefs", android.content.Context.MODE_PRIVATE)
    val knownAllergies = MutableStateFlow<List<String>>(
        run {
            val csv = prefs.getString("known_allergies", "") ?: ""
            if (csv.isBlank()) emptyList()
            else csv.split("|").filter { it.isNotBlank() }
        }
    )

    fun addAllergy(allergy: String) {
        val trimmed = allergy.trim()
        if (trimmed.isBlank()) return
        val current = knownAllergies.value.toMutableList()
        if (!current.any { it.equals(trimmed, ignoreCase = true) }) {
            current.add(trimmed)
            knownAllergies.value = current
            prefs.edit().putString("known_allergies", current.joinToString("|")).apply()
        }
    }

    fun deleteAllergy(allergy: String) {
        val current = knownAllergies.value.toMutableList()
        current.removeAll { it.equals(allergy, ignoreCase = true) }
        knownAllergies.value = current
        prefs.edit().putString("known_allergies", current.joinToString("|")).apply()
    }

    // UI state states
    var selectedRegion = MutableStateFlow(Region.INDIA)
    val selectedCategory = MutableStateFlow<SymptomCategory?>(null)
    val selectedDurationDays = MutableStateFlow(1) // range 1..7

    // Red flag checks triggered of selected Category
    val activeRedFlags = MutableStateFlow<Set<String>>(emptySet())

    // Real-time screening outputs
    val hasEmergencySelection = activeRedFlags.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Current screened products
    val currentOtcAdvice = combine(
        selectedCategory,
        selectedRegion,
        currentMedications,
        selectedDurationDays,
        knownAllergies
    ) { category, region, meds, days, allergies ->
        if (category == null) return@combine null

        val ladder = PharmacistBrain.getLadderAdvice(category, days)
        val (allBrands, warn) = if (ladder.allowOtcSuggestions) {
            PharmacistBrain.getRegionalOtcSuggestions(category, region, meds)
        } else {
            Pair(emptyList(), "Symptom duration requires emergency or specialized doctor treatment.")
        }

        // Filter out products matching any known medication allergies
        val filteredBrands = allBrands.filter { brand ->
            val activeLower = brand.activeIngredient.lowercase()
            val nameLower = brand.brandName.lowercase()
            allergies.none { allergy ->
                val allergyLower = allergy.lowercase().trim()
                allergyLower.isNotEmpty() && (activeLower.contains(allergyLower) || nameLower.contains(allergyLower))
            }
        }

        val allergyWarning = if (filteredBrands.size < allBrands.size) {
            val filteredNames = allBrands.filterNot { filteredBrands.contains(it) }.map { it.brandName }.joinToString(", ")
            "ALLERGY SAFETY CHECK: We filtered out medications ($filteredNames) due to your registered allergen profiles."
        } else {
            null
        }

        val combinedWarning = when {
            warn != null && allergyWarning != null -> "$warn\n\n$allergyWarning"
            warn != null -> warn
            allergyWarning != null -> allergyWarning
            else -> null
        }

        ScreenerAdvice(
            ladderAdvice = ladder,
            recommendedBrands = filteredBrands,
            interactionWarning = combinedWarning
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // AI Pharmacist States
    val aiResponseText = MutableStateFlow<String?>(null)
    val parsedAiResponse = MutableStateFlow<ParsedAiResponse?>(null)
    val isAiLoading = MutableStateFlow(false)
    val customAiPrompt = MutableStateFlow("")

    // --- Actions ---

    fun selectRegion(region: Region) {
        selectedRegion.value = region
    }

    fun selectCategory(category: SymptomCategory?) {
        selectedCategory.value = category
        // Reset flags when category shifts
        activeRedFlags.value = emptySet()
    }

    fun setDuration(days: Int) {
        selectedDurationDays.value = days
    }

    fun toggleRedFlag(flag: String) {
        val currentSet = activeRedFlags.value.toMutableSet()
        if (currentSet.contains(flag)) {
            currentSet.remove(flag)
        } else {
            currentSet.add(flag)
        }
        activeRedFlags.value = currentSet
    }

    fun addMedication(name: String, ingredient: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addMedication(
                UserMedication(name = name, activeIngredient = ingredient)
            )
        }
    }

    fun deleteMedication(med: UserMedication) {
        viewModelScope.launch {
            repository.removeMedication(med)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Save Consultation Log
    fun saveConsultationLog() {
        val category = selectedCategory.value ?: return
        val days = selectedDurationDays.value
        val region = selectedRegion.value
        val hasEmergency = activeRedFlags.value.isNotEmpty()
        val advice = currentOtcAdvice.value

        viewModelScope.launch {
            val primaryBrand = advice?.recommendedBrands?.firstOrNull()
            repository.addToHistory(
                ConsultationHistory(
                    symptomCategoryName = category.displayName,
                    durationDays = days,
                    selectedCountry = region.label,
                    reportedEmergencySigns = hasEmergency,
                    recommendedBrandName = if (hasEmergency) "REFERRED TO CLINIC" else (primaryBrand?.brandName ?: "See Doctor"),
                    recommendedIngredient = if (hasEmergency) "N/A" else (primaryBrand?.activeIngredient ?: "See Specialist"),
                    retailShelfMap = if (hasEmergency) "Emergency Room" else (primaryBrand?.shelfLocation ?: "Doctor Office"),
                    triggeredInteractionWarning = advice?.interactionWarning
                )
            )
        }
    }

    // AI Query Execution
    fun runAiQuery() {
        val category = selectedCategory.value
        val rawPrompt = customAiPrompt.value
        val symptomDesc = if (rawPrompt.isNotBlank()) rawPrompt else {
            category?.displayName ?: "Unspecified symptom"
        }

        isAiLoading.value = true
        aiResponseText.value = null
        parsedAiResponse.value = null

        viewModelScope.launch {
            val medNames = currentMedications.value.map { "${it.name} (${it.activeIngredient})" }
            val response = GeminiService.queryPharmacistAssistant(
                symptomDescription = symptomDesc,
                country = selectedRegion.value.label,
                currentMedications = medNames
            )
            aiResponseText.value = response
            parsedAiResponse.value = parseGeminiResponse(response)
            isAiLoading.value = false
        }
    }
}

data class ScreenerAdvice(
    val ladderAdvice: PharmacistBrain.LadderAdvice,
    val recommendedBrands: List<OtcBrand>,
    val interactionWarning: String?
)

class ScreenerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScreenerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScreenerViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class ParsedAiResponse(
    val mainExplanation: String,
    val suggestedMedication: String?,
    val durationLadderAdvice: String?,
    val potentialSideEffects: String?
)

fun parseGeminiResponse(rawText: String): ParsedAiResponse {
    var raw = rawText

    // Helper to extract a tag pair and clean the text
    fun extractTag(text: String, startTag: String, endTag: String): Pair<String?, String> {
        val start = text.indexOf(startTag)
        val end = text.indexOf(endTag)
        if (start != -1 && end != -1 && end > start) {
            val tagContent = text.substring(start + startTag.length, end).trim()
            val cleanText = (text.substring(0, start) + text.substring(end + endTag.length)).trim()
            return Pair(tagContent, cleanText)
        }
        return Pair(null, text)
    }

    // Extract potential side effects first
    val extSideEffects = extractTag(raw, "[POTENTIAL-SIDE-EFFECTS]", "[END-POTENTIAL-SIDE-EFFECTS]")
    var sideEffects = extSideEffects.first
    raw = extSideEffects.second

    // Extract suggested medications next
    val extSuggested = extractTag(raw, "[SUGGESTED-MEDICATION]", "[END-SUGGESTED-MEDICATION]")
    var suggested = extSuggested.first
    raw = extSuggested.second

    // Extract duration ladder advice next
    val extDuration = extractTag(raw, "[DURATION-LADDER]", "[END-DURATION-LADDER]")
    var duration = extDuration.first
    raw = extDuration.second

    // Fallbacks if tags are missing or only partial
    if (duration == null) {
        val startTag = "[DURATION-LADDER]"
        val startIdx = raw.indexOf(startTag)
        if (startIdx != -1) {
            duration = raw.substring(startIdx + startTag.length).trim()
            raw = raw.substring(0, startIdx).trim()
        } else {
            val fallbackMarkers = listOf(
                "Duration-Ladder:",
                "Duration Ladder:",
                "Duration-Ladder Advice:",
                "Duration Ladder Advice:"
            )
            for (marker in fallbackMarkers) {
                val fbIndex = raw.indexOf(marker, ignoreCase = true)
                if (fbIndex != -1) {
                    duration = raw.substring(fbIndex + marker.length).trim()
                    raw = raw.substring(0, fbIndex).trim()
                    break
                }
            }
        }
    }

    if (sideEffects == null) {
        val fallbackMarkers = listOf(
            "Side Effects:",
            "Potential Side Effects:",
            "Side-Effects:"
        )
        for (marker in fallbackMarkers) {
            val fbIndex = raw.indexOf(marker, ignoreCase = true)
            if (fbIndex != -1) {
                sideEffects = raw.substring(fbIndex + marker.length).trim()
                raw = raw.substring(0, fbIndex).trim()
                break
            }
        }
    }

    if (suggested == null) {
        val fallbackMarkers = listOf(
            "Suggested Medication:",
            "Suggested Medications:",
            "Medication Suggestion:",
            "Suggested OTC medication:"
        )
        for (marker in fallbackMarkers) {
            val fbIndex = raw.indexOf(marker, ignoreCase = true)
            if (fbIndex != -1) {
                suggested = raw.substring(fbIndex + marker.length).trim()
                raw = raw.substring(0, fbIndex).trim()
                break
            }
        }
    }

    // Collapse excess multiline breaks caused by block tag removals
    val cleanExplanation = raw.replace(Regex("(?m)^[ \t]*\r?\n"), "\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()

    return ParsedAiResponse(
        mainExplanation = cleanExplanation,
        suggestedMedication = suggested,
        durationLadderAdvice = duration,
        potentialSideEffects = sideEffects
    )
}
