package com.ruchitech.quicklinkcaller.ai

import com.ruchitech.quicklinkcaller.helper.AppPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val appPreference: AppPreference,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    val hasAiAccess: Boolean
        get() = !appPreference.geminiApiKey.isNullOrBlank()

    suspend fun generate(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = appPreference.geminiApiKey
                ?: throw Exception("No Gemini API key. Add one in Settings → AI Features.")
            val body = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }.toString()

            val request = Request.Builder()
                .url("$endpoint?key=$apiKey")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response from Gemini")

            if (!response.isSuccessful) {
                val msg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message")
                        ?: responseBody
                } catch (e: Exception) {
                    responseBody
                }
                throw Exception(msg)
            }

            JSONObject(responseBody)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
        }
    }

    suspend fun scoreLeadIntelligence(
        name: String?,
        phone: String,
        status: String,
        source: String,
        notes: String,
        callCount: Int,
    ): Result<String> = generate(
        buildString {
            appendLine("You are a sales AI. Analyze this business lead and respond in EXACTLY this format:")
            appendLine("SCORE: [0-100]")
            appendLine("ACTION: [one sentence next action]")
            appendLine("REASON: [one sentence why]")
            appendLine()
            appendLine("Lead info:")
            appendLine("Name: ${name ?: "Unknown"}")
            appendLine("Status: $status")
            appendLine("Source: $source")
            appendLine("Calls made: $callCount")
            appendLine("Notes: $notes")
            appendLine()
            appendLine("Higher score = higher purchase likelihood. Be concise.")
        }
    )

    suspend fun composeWhatsAppMessage(
        name: String?,
        status: String,
        purpose: String,
    ): Result<String> = generate(
        buildString {
            appendLine("Write a short professional WhatsApp business message.")
            appendLine("Contact name: ${name ?: "there"}")
            appendLine("Their status: $status")
            appendLine("Message purpose: $purpose")
            appendLine()
            appendLine("Requirements: under 3 sentences, friendly, professional, end with a clear call-to-action.")
            appendLine("Return ONLY the message text.")
        }
    )

    suspend fun analyzeNotes(notes: String): Result<String> = generate(
        buildString {
            appendLine("Extract 2-3 key action items from these call notes.")
            appendLine("Format as bullet points using •")
            appendLine("Be concise and actionable.")
            appendLine()
            appendLine("Notes:")
            appendLine(notes)
        }
    )

    suspend fun getDailyBriefing(
        totalLeads: Int,
        newLeads: Int,
        callsMade: Int,
        pendingFollowUps: Int,
    ): Result<String> = generate(
        buildString {
            appendLine("Write a 2-sentence motivating daily sales briefing.")
            appendLine("Stats: $totalLeads total leads, $newLeads new today, $callsMade calls today, $pendingFollowUps pending follow-ups.")
            appendLine("Be specific, upbeat and actionable. No emojis.")
        }
    )
}
