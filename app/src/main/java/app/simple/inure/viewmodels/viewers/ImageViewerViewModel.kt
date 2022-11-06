package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.simple.inure.constants.Warnings
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.glide.graphics.AppGraphicsModel
import app.simple.inure.util.ProcessUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

class ImageViewerViewModel(application: Application, private val pathToImage: String, private val pathToApk: String) : WrappedViewModel(application) {

    init {
        loadImage()
    }

    private val bitmap: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }

    private val gif: MutableLiveData<GifDrawable> by lazy {
        MutableLiveData<GifDrawable>()
    }

    fun getBitmap(): LiveData<Bitmap> {
        return bitmap
    }

    fun getGif(): LiveData<GifDrawable> {
        return gif
    }

    private fun loadImage() {
        if (pathToImage.endsWith("gif")) {
            loadGif()
        } else {
            loadBitmap()
        }
    }

    @MainThread
    private fun loadGif() {
        ProcessUtils.ensureOnMainThread {
            Glide.with(context)
                .asGif()
                .dontTransform()
                .load(AppGraphicsModel(pathToApk, pathToImage))
                .into(object : CustomTarget<GifDrawable>() {
                    override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                        gif.postValue(resource)
                        resource.start()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        /* no-op */
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        postWarning(Warnings.getInureWarning02(pathToImage, "GIF"))
                    }
                })
        }
    }

    @MainThread
    private fun loadBitmap() {
        ProcessUtils.ensureOnMainThread {
            Glide.with(context)
                .asBitmap()
                .dontAnimate()
                .dontTransform()
                .load(AppGraphicsModel(pathToApk, pathToImage))
                .addListener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        bitmap.postValue(resource)
                        return true
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        postWarning(Warnings.getInureWarning02(pathToImage, "Bitmap"))
                        return true
                    }
                })
                .preload()
        }
    }
}