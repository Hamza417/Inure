package app.simple.inure.util

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import app.simple.inure.R
import java.io.IOException

object AudioUtils {
    /**
     * Calculates the sampling of the given audio file in
     * kHz and returns the appended string
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @param context a context of the calling region
     * @param fileUri Uri of the file
     * @return Sampling of the given audio file as String
     */
    fun getSampling(context: Context, fileUri: Uri): String {
        val mex = MediaExtractor()
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mex.setDataSource(context.contentResolver.openAssetFileDescriptor(fileUri, "r")!!)
            } else {
                mex.setDataSource(context, fileUri, null)
            }
            return "${mex.getTrackFormat(0).getInteger(MediaFormat.KEY_SAMPLE_RATE).toFloat() / 1000} kHz"
        } catch (e: IOException) {
        } catch (e: IllegalArgumentException) {
        } catch (e: NullPointerException) {
        } finally {
            mex.release()
        }

        return context.getString(R.string.not_available)
    }

    fun Int.toBitrate(): String {
        return when {
            this / 1000 < 1024 -> {
                "${this / 1000} kbit/s"
            }
            this / 1000 > 1024 -> {
                "${this / 1000} mbit/s"
            }
            else -> {
                "exceeding bitrate"
            }
        }
    }
}