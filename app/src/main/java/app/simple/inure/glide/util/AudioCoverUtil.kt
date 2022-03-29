package app.simple.inure.glide.util

import android.graphics.Bitmap
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.glide.filedescriptorcover.DescriptorCoverModel
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.transformation.BlurShadow
import app.simple.inure.glide.transformation.Greyscale
import app.simple.inure.glide.transformation.Padding
import app.simple.inure.preferences.AppearancePreferences
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

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
            .transform(RoundedCorners(AppearancePreferences.getCornerRadius().toInt()),
                       Padding(BlurShadow.DEFAULT_SHADOW_SIZE.toInt()),
                       BlurShadow(context)
                           .setElevation(25F)
                           .setBlurRadius(BlurShadow.DEFAULT_SHADOW_SIZE))
            .load(DescriptorCoverModel(this.context, uri))
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    this@loadFromFileDescriptor.setImageResource(R.drawable.ani_ic_app_icon).also {
                        (this@loadFromFileDescriptor.drawable as AnimatedVectorDrawable).start()
                    }
                    return true
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    return false
                }
            })
            .into(this)
    }

    /**
     * @param uri requires a valid file uri and not art uri else
     * error 0x80000000 will be thrown by the MediaMetadataRetriever
     *
     * Asynchronously load Album Arts for song files from their URIs using file descriptor
     */
    fun ImageView.loadFromFileDescriptorFullScreen(uri: Uri) {
        GlideApp.with(this)
            .asBitmap()
            .transform(CenterCrop())
            .load(DescriptorCoverModel(this.context, uri))
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    this@loadFromFileDescriptorFullScreen.setImageResource(R.drawable.ani_ic_app_icon).also {
                        (this@loadFromFileDescriptorFullScreen.drawable as AnimatedVectorDrawable).start()
                    }
                    return true
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    return false
                }
            })
            .into(this)
    }

    /**
     * @param uri requires a valid file uri and not art uri else
     * error 0x80000000 will be thrown by the MediaMetadataRetriever
     *
     * Asynchronously load Album Arts for song files from their URIs using file descriptor
     */
    fun ImageView.loadFromFileDescriptorGreyscale(uri: Uri) {
        GlideApp.with(this)
            .asBitmap()
            .transform(CenterCrop(), Greyscale())
            .load(DescriptorCoverModel(this.context, uri))
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    this@loadFromFileDescriptorGreyscale.setImageResource(R.drawable.ani_ic_app_icon).also {
                        (this@loadFromFileDescriptorGreyscale.drawable as AnimatedVectorDrawable).start()
                    }
                    return true
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    return false
                }
            })
            .into(this)
    }
}