package app.simple.inure.glide.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import app.simple.inure.glide.filedescriptorcover.DescriptorCoverModel
import app.simple.inure.glide.modules.GlideApp

object AudioCoverUtil {
    /**
     * @param uri requires a valid file uri and not art uri else
     * error 0x80000000 will be thrown by the MediaMetadataRetriever
     *
     * Asynchronously load Album Arts for song files from their URIs using file descriptor
     */
    fun ImageView.loadFromFileDescriptor(context: Context, uri: Uri) {
        GlideApp.with(this).asBitmap().load(DescriptorCoverModel(context, uri)).into(this)
    }
}