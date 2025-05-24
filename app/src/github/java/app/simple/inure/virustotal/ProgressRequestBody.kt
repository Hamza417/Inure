package app.simple.inure.virustotal

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

class ProgressRequestBody(
        private val file: File,
        private val contentType: MediaType?,
        private val onProgress: (percent: Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = contentType
    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        file.inputStream().use { input ->
            var uploaded = 0L
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                val percent = (uploaded * 100 / totalBytes).toInt()
                onProgress(percent)
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192
    }
}
