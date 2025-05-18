package app.simple.inure.virustotal

import org.json.JSONObject

sealed class VirusTotalResult {
    data class Success(val result: JSONObject) : VirusTotalResult()
    data class Error(val message: String) : VirusTotalResult()
    data class Progress(val progressCode: Int, val status: String, val progress: Float = 0F) : VirusTotalResult() {
        companion object {
            const val CALCULATING = 2
            const val UPLOADING = 0
            const val POLLING = 3
            const val UPLOAD_SUCCESS = 1
            const val COMPLETE_PROGRESS = 100F
        }
    }
}
