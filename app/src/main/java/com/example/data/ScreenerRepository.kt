package com.example.data

import kotlinx.coroutines.flow.Flow

class ScreenerRepository(private val dao: ScreenerDao) {
    val allMedications: Flow<List<UserMedication>> = dao.getAllMedications()
    val consultationHistory: Flow<List<ConsultationHistory>> = dao.getHistory()

    suspend fun addMedication(med: UserMedication) {
        dao.insertMedication(med)
    }

    suspend fun removeMedication(med: UserMedication) {
        dao.deleteMedication(med)
    }

    suspend fun addToHistory(item: ConsultationHistory) {
        dao.insertHistory(item)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}
