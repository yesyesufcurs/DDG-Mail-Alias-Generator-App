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

    suspend fun generateEmail(token: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://quack.duckduckgo.com/api/email/addresses")
                .header("Authorization", "Bearer $token")
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
