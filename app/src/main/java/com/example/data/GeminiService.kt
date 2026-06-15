package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// --- MOSHI ANNOTATED MODELS FOR RETROFIT / DIRECT REQUESTS ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

object GeminiService {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun queryPharmacistAssistant(
        symptomDescription: String,
        country: String,
        currentMedications: List<String>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AI service is offline: Please set your GEMINI_API_KEY in the Secrets panel."
        }

        val systemPrompt = """
            You are a licensed professional pharmacist assistant practicing in $country.
            Your task is to provide objective, educational information regarding symptoms, over-the-counter active ingredients, and medication safety.
            
            GUIDELINES:
            1. You are NOT diagnosing the user. Strictly output a clinical disclaimer at the start of your message.
            2. Address the symptom description: "$symptomDescription".
            3. Highlight the role of relevant OTC active ingredients for $country.
            4. Review safe schedules and point out interactions with the user's current medications: ${currentMedications.joinToString(", ")}.
            5. Present suggestions strictly on safety. Keep your explanation clinical, reassuring, and structure it with bullets.
            6. MANDATORY RECOGNITION SUMMARY REQUIREMENT: You must provide your assessment and explicitly wrap pieces inside these three tags at the end of your response:
               - The suggested medication and clinical ingredients wrapped inside:
                 [SUGGESTED-MEDICATION]
                 <Identify the recommended active ingredients or brands appropriate for $symptomDescription in $country, with recommended OTC dosages>
                 [END-SUGGESTED-MEDICATION]
               - Explicit advice on how long the user should safely try an OTC treatment before consulting (Duration-Ladder):
                 [DURATION-LADDER]
                 <Detailed timeline advice, e.g., "For $symptomDescription, do not exceed 3 days...">
                 [END-DURATION-LADDER]
               - Clear safety warnings about potential side effects, drowsy risks, and drug interactions:
                 [POTENTIAL-SIDE-EFFECTS]
                 <Specific side effects, drowsy/alcohol warning, and safety precautions>
                 [END-POTENTIAL-SIDE-EFFECTS]
            
            Never mention prescription drug replacement without telling them to visit their primary physician.
        """.trimIndent()

        val requestData = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = "Review my symptoms: $symptomDescription")))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        val adapter = moshi.adapter(GeminiRequest::class.java)
        val jsonPayload = adapter.toJson(requestData)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(jsonPayload.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error: API returned code ${response.code}. Please verify your key or try again."
                }
                val bodyString = response.body?.string() ?: return@withContext "Error: Empty response body received."
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val responseObj = responseAdapter.fromJson(bodyString)
                val textResponse = responseObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                textResponse ?: "AI Pharmacist was unable to generate a response. Please check safety details in the structured tab."
            }
        } catch (e: Exception) {
            "Network connection failed: ${e.message}. Please rely on the offline-first structured drug tree recommendations."
        }
    }
}
