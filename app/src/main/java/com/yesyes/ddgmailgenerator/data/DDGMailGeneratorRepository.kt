package com.yesyes.ddgmailgenerator.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class DuckDuckGoResponse(val address: String)

class DuckDuckGoRepository {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 15; Pixel 8) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
    }

    suspend fun generateEmail(token: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://quack.duckduckgo.com/api/email/addresses")
                .header("Accept", "*/*")
                .header("Authorization", "Bearer $token")
                .header("Origin", "https://duckduckgo.com")
                .header("Referer", "https://duckduckgo.com/")
                .header("User-Agent", USER_AGENT)
                .post(ByteArray(0).toRequestBody(null))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.code == 201) {
                    val body = response.body.string()
                    val ddgResponse = json.decodeFromString<DuckDuckGoResponse>(body)
                    Result.success(ddgResponse.address + "@duck.com")
                } else {
                    Result.failure(Exception("Server Error: ${response.code} ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection Error: ${e.localizedMessage}", e))
        }
    }
}
