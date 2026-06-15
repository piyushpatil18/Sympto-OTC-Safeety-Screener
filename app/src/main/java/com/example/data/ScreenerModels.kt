package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ROOM ENTITIES ---

@Entity(tableName = "medications")
data class UserMedication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val activeIngredient: String = ""
)

@Entity(tableName = "consultation_history")
data class ConsultationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val symptomCategoryName: String,
    val durationDays: Int,
    val selectedCountry: String, // "India" or "USA"
    val reportedEmergencySigns: Boolean,
    val recommendedBrandName: String,
    val recommendedIngredient: String,
    val retailShelfMap: String,
    val triggeredInteractionWarning: String? = null
)

// --- DAO DEFINITION ---

@Dao
interface ScreenerDao {
    @Query("SELECT * FROM medications ORDER BY id DESC")
    fun getAllMedications(): Flow<List<UserMedication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: UserMedication)

    @Delete
    suspend fun deleteMedication(med: UserMedication)

    @Query("SELECT * FROM consultation_history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<ConsultationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ConsultationHistory)

    @Query("DELETE FROM consultation_history")
    suspend fun clearHistory()
}

// --- DATABASE DEFINITION ---

@Database(entities = [UserMedication::class, ConsultationHistory::class], version = 1, exportSchema = false)
abstract class ScreenerDatabase : RoomDatabase() {
    abstract fun screenerDao(): ScreenerDao
}

// --- STRUCTURED PHARMACIST LOGIC METADATA ---

enum class Region(val label: String, val flag: String) {
    INDIA("India", "🇮🇳"),
    USA("United States", "🇺🇸")
}

enum class SymptomCategory(val displayName: String, val iconUnicode: String) {
    FEVER_ACHE("Fever & Body pain", "🌡️"),
    ACID_REFLUX("Acid Burn", "🔥"),
    COLD_CONGESTION("Cold & Nasal Congestion", "🤧"),
    SNEEZE_ALLERGY("Seasonal Allergies", "🌸"),
    BLOATING_GAS("Bloating", "🎈")
}

data class OtcBrand(
    val brandName: String,
    val activeIngredient: String,
    val recommendedDose: String,
    val shelfLocation: String, // Mock B2B Pharmacy mapping
    val standardPrice: String
)

data class InteractionCheck(
    val currentMedRequiredIngredient: String, // Keyword (e.g., "warfarin", "aspro", "aspirin")
    val hazardousIngredient: String, // E.g., "Ibuprofen"
    val severity: String, // "High" | "Moderate"
    val warningMessage: String
)
