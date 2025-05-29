package app.simple.inure.virustotal

import android.net.TrafficStats
import android.util.Log
import app.simple.inure.preferences.VirusTotalPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

class VirusTotalClient(private val apiKey: String) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .eventListener(object : okhttp3.EventListener() {
            override fun connectStart(call: okhttp3.Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy) {
                TrafficStats.setThreadStatsTag(VIRUS_TOTAL_THREAD_TAG)
            }
        })
        .build()

    private val baseUrl = "https://www.virustotal.com/api/v3"

    private fun log(message: String) {
        Log.d("VirusTotalApi", message)
    }

    private fun computeSHA256(file: File): String {
        log("Calculating SHA-256 for: ${file.absolutePath}")
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        log("SHA-256 calculated: $hash")
        return hash
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
            log("Error checking hash: ${e.message}")
            null
        }
    }

    private fun uploadFile(file: File): Flow<VirusTotalResult> = callbackFlow {
        try {
            val uploadEndpoint: String
            when {
                file.length() <= MAX_FREE_FILE_SIZE -> {
                    uploadEndpoint = "$baseUrl/files"
                }
                file.length() <= MAX_LARGE_FILE_SIZE -> {
                    // Request upload URL for large files
                    log("Requesting upload URL for large file: ${file.absolutePath}")

                    val request = Request.Builder()
                        .url("$baseUrl/files/upload_url")
                        .addHeader("x-apikey", apiKey)
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    response.use {
                        if (!it.isSuccessful) {
                            trySend(VirusTotalResult.Error("Failed to get upload URL: ${it.code}"))
                            close()
                            return@callbackFlow
                        }
                        val json = it.body?.string()?.let { body -> JSONObject(body) }
                        uploadEndpoint = json?.getString("data")
                            ?: run {
                                trySend(VirusTotalResult.Error("Upload URL missing in response"))
                                close()
                                return@callbackFlow
                            }
                    }
                }
                else -> {
                    trySend(VirusTotalResult.Error("File size exceeds maximum limit of 650 MB"))
                    close()
                    return@callbackFlow
                }
            }

            val progressBody = ProgressRequestBody(file, "application/octet-stream".toMediaTypeOrNull()) { percent ->
                trySend(
                        VirusTotalResult.Progress(
                                progressCode = VirusTotalResult.Progress.UPLOADING,
                                status = "Uploading file: $percent%",
                                progress = percent
                        )
                )
                log("Upload progress: $percent%")
                ensureActive()
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, progressBody)
                .build()

            ensureActive()

            val uploadRequest = Request.Builder()
                .url(uploadEndpoint)
                .addHeader("x-apikey", apiKey)
                .post(requestBody)
                .build()

            val uploadResponse = client.newCall(uploadRequest).execute()
            uploadResponse.use {
                val json = it.body?.string()?.let { body -> JSONObject(body) }
                if (json != null) {
                    trySend(VirusTotalResult.Uploaded(json))
                } else {
                    trySend(VirusTotalResult.Error("Upload failed: Empty response"))
                }
            }
        } catch (e: Exception) {
            trySend(VirusTotalResult.Error("Upload failed: ${e.message}"))
        }

        close()
    }

    private fun pollAnalysisResult(analysisId: String): Flow<VirusTotalResult> = callbackFlow {
        repeat(MAX_POLLING_ATTEMPTS) { it ->
            ensureActive()
            log("Polling for analysis result... Attempt: ${it + 1}")

            val request = Request.Builder()
                .url("$baseUrl/analyses/$analysisId")
                .addHeader("x-apikey", apiKey)
                .get()
                .build()

            trySend(
                    VirusTotalResult.Progress(
                            progressCode = VirusTotalResult.Progress.POLLING,
                            status = "Polling for analysis result... Attempt: " +
                                    "${it + 1}, next in ${POLLING_INTERVAL / 1000} seconds",
                            progress = (it + 1) / MAX_POLLING_ATTEMPTS,
                            pollingAttempts = it + 1
                    ))

            try {
                val response = client.newCall(request).execute()
                response.use {
                    val body = it.body?.string()
                    if (it.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val status = json.getJSONObject("data")
                            .getJSONObject("attributes")
                            .getString("status")
                        // trySend(VirusTotalResult.Progress("Scan status: $status, waiting...\n"))
                        if (status == "completed") {
                            trySend(VirusTotalResult.Success(json))
                            close()
                            return@callbackFlow
                        }
                    }
                }
            } catch (e: Exception) {
                trySend(VirusTotalResult.Error("Polling error: ${e.message}"))
            }
            delay(POLLING_INTERVAL)
        }
        trySend(VirusTotalResult.Error("Timeout waiting for analysis to complete."))
        close()
    }

    fun scanFile(filePath: String): Flow<VirusTotalResult> = flow {
        val file = File(filePath)
        if (!file.exists()) {
            emit(VirusTotalResult.Error("File does not exist: $filePath"))
            return@flow
        }

        emit(VirusTotalResult.Progress(VirusTotalResult.Progress.CALCULATING, "Calculating file hash..."))
        val hash = computeSHA256(file)

        emit(VirusTotalResult.Progress(VirusTotalResult.Progress.CALCULATING, "Checking hash $hash at VirusTotal..."))
        val hashResult = checkHash(hash)

        if (hashResult != null) {
            emit(
                    VirusTotalResult.Progress(
                            progress = VirusTotalResult.Progress.COMPLETE_PROGRESS,
                            progressCode = VirusTotalResult.Progress.HASH_RESULT,
                            status = "File already scanned. Analysis ID: ${hashResult.getJSONObject("data").getString("id")}"
                    )
            )
            emit(VirusTotalResult.Success(hashResult))
        } else {
            emit(VirusTotalResult.Progress(VirusTotalResult.Progress.UPLOADING, "File not found. Uploading..."))
            val uploadResults = mutableListOf<VirusTotalResult>()
            uploadFile(file).collect { result ->
                emit(result)
                uploadResults.add(result)
            }

            val uploadResult = uploadResults.firstOrNull { it is VirusTotalResult.Uploaded } as? VirusTotalResult.Uploaded
            if (uploadResult != null) {
                val analysisId = uploadResult.result.getJSONObject("data").getString("id")
                emit(
                        VirusTotalResult.Progress(
                                progressCode = VirusTotalResult.Progress.UPLOAD_SUCCESS,
                                progress = VirusTotalResult.Progress.COMPLETE_PROGRESS,
                                status = "File uploaded. Analysis ID: $analysisId"
                        )
                )

                var analysisCompleted = false
                pollAnalysisResult(analysisId).collect { result ->
                    emit(result)
                    if (result is VirusTotalResult.Success) {
                        analysisCompleted = true
                    }
                }

                if (analysisCompleted) {
                    // Fetch the full report using the hash after analysis is complete
                    val fullReport = checkHash(hash)
                    if (fullReport != null) {
                        emit(VirusTotalResult.Success(fullReport))
                    } else {
                        emit(VirusTotalResult.Error("Failed to fetch full report after analysis."))
                    }
                }
            } else {
                emit(VirusTotalResult.Error("Failed to upload file to VirusTotal."))
            }
        }
    }

    fun onlyScanFile(filePath: String): Flow<VirusTotalResult> = flow {
        val file = File(filePath)
        if (!file.exists()) {
            emit(VirusTotalResult.Error("File does not exist: $filePath"))
            return@flow
        }

        emit(VirusTotalResult.Progress(VirusTotalResult.Progress.CALCULATING, "Calculating file hash..."))
        val hash = computeSHA256(file)

        emit(VirusTotalResult.Progress(VirusTotalResult.Progress.CALCULATING, "Checking hash $hash at VirusTotal..."))
        val hashResult = checkHash(hash)

        if (hashResult != null) {
            emit(
                    VirusTotalResult.Progress(
                            progress = VirusTotalResult.Progress.COMPLETE_PROGRESS,
                            progressCode = VirusTotalResult.Progress.HASH_RESULT,
                            status = "File already scanned. Analysis ID: ${hashResult.getJSONObject("data").getString("id")}"
                    )
            )
            emit(VirusTotalResult.Success(hashResult))
        } else {
            emit(
                    VirusTotalResult.Progress(
                            progressCode = VirusTotalResult.Progress.HASH_NOT_FOUND,
                            status = "File hash not found on VirusTotal. Do you want to upload?",
                            progress = 0
                    )
            )
        }
    }

    companion object {
        private const val TAG = "VirusTotal"

        fun getInstance(apiKey: String): VirusTotalClient {
            return VirusTotalClient(apiKey)
        }

        fun getInstance(): VirusTotalClient {
            val apiKey = VirusTotalPreferences.getVirusTotalApiKey()
            return if (apiKey.isNotEmpty()) {
                VirusTotalClient(apiKey)
            } else {
                throw IllegalStateException("VirusTotal API key is not set.")
            }
        }

        private const val MAX_FREE_FILE_SIZE = 32 * 1024 * 1024 // 32 MB
        private const val MAX_LARGE_FILE_SIZE = 650 * 1024 * 1024 // 650 MB
        private const val MAX_POLLING_ATTEMPTS = 30
        private const val POLLING_INTERVAL = 10_000L
        private const val VIRUS_TOTAL_THREAD_TAG = 1458
    }
}
