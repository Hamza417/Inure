package app.simple.inure.virustotal

import org.json.JSONObject

sealed class VirusTotalResult {
    data class Success(val result: JSONObject) : VirusTotalResult()
    data class Error(val message: String) : VirusTotalResult()
    data class Progress(val status: String) : VirusTotalResult()
}
