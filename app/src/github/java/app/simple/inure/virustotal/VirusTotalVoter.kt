package app.simple.inure.virustotal

import android.net.TrafficStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class VirusTotalVoter(private val apiKey: String) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .eventListener(object : okhttp3.EventListener() {
            override fun connectStart(call: okhttp3.Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy) {
                TrafficStats.setThreadStatsTag(VIRUS_TOTAL_THREAD_TAG)
            }
        })
        .build()

    private val baseUrl = "https://www.virustotal.com/api/v3"

    fun voteOnFile(hash: String, vote: VoteType): Flow<VirusTotalResult> = flow {
        val url = "$baseUrl/files/$hash/votes"
        val jsonBody = JSONObject().apply {
            put("data", JSONObject().apply {
                put("type", "vote")
                put("attributes", JSONObject().apply {
                    put("verdict", vote.value)
                })
            })
        }

        val requestBody = jsonBody.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .addHeader("x-apikey", apiKey)
            .post(requestBody)
            .build()
        try {
            val response = client.newCall(request).execute()
            response.use {
                if (it.isSuccessful) {
                    val finalAnalysis = checkHash(hash)
                    if (finalAnalysis != null) {
                        emit(VirusTotalResult.Success(finalAnalysis))
                    } else {
                        emit(VirusTotalResult.Error("Failed to retrieve final analysis for hash: $hash"))
                    }
                } else {
                    emit(VirusTotalResult.Error("Vote failed: ${it.code}"))
                }
            }
        } catch (e: Exception) {
            emit(VirusTotalResult.Error("Vote error: ${e.message}"))
        }
    }

    private fun checkHash(hash: String): JSONObject? {
        val request = Request.Builder()
            .url("$baseUrl/files/$hash")
            .addHeader("x-apikey", apiKey)
            .get()
            .build()

        return try {
            val response = client.newCall(request).execute()
            response.use {
                if (it.isSuccessful) {
                    it.body?.string()?.let { json -> JSONObject(json) }
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    enum class VoteType(val value: String) {
        MALICIOUS("malicious"),
        HARMLESS("harmless")
    }

    companion object {
        private const val VIRUS_TOTAL_THREAD_TAG = 1459
    }
}