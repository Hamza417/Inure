package app.simple.inure.glide.util

import android.net.Uri
import android.widget.ImageView
import app.simple.inure.glide.filedescriptorcover.DescriptorCoverModel
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.transformation.BlurShadow
import app.simple.inure.glide.transformation.Padding
import app.simple.inure.preferences.AppearancePreferences
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

object AudioCoverUtil {
    /**
     * @param uri requires a valid file uri and not art uri else
     * error 0x80000000 will be thrown by the MediaMetadataRetriever
     *
     * Asynchronously load Album Arts for song files from their URIs using file descriptor
     */
    fun ImageView.loadFromFileDescriptor(uri: Uri) {
        GlideApp.with(this)
                .asBitmap()
                .transform(RoundedCorners(AppearancePreferences.getCornerRadius()),
                           Padding(BlurShadow.RENDERSCRIPT_DEFAULT_SHADOW_SIZE.toInt()), BlurShadow(context)
                                   .setElevation(25F)
                                   .setBlurRadius(BlurShadow.RENDERSCRIPT_DEFAULT_SHADOW_SIZE))
                .load(DescriptorCoverModel(this.context, uri))
                .into(this)
    }
}