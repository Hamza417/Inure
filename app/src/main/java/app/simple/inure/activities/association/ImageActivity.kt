package app.simple.inure.activities.association

import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import app.simple.inure.R
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ZoomImageView
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.util.FileUtils.getMimeType
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ProcessUtils
import app.simple.inure.util.ViewUtils.gone
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class ImageActivity : BaseActivity() {

    private lateinit var image: SubsamplingScaleImageView
    private lateinit var gif: ZoomImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var header: PaddingAwareLinearLayout

    private var isFullScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        image = findViewById(R.id.image_viewer)
        gif = findViewById(R.id.gif_viewer)
        back = findViewById(R.id.image_viewer_back_button)
        name = findViewById(R.id.image_name)
        header = findViewById(R.id.header)

        ProcessUtils.ensureOnMainThread {
            if (intent.data!!.getMimeType(applicationContext)?.endsWith("gif") == true) {
                Glide.with(applicationContext)
                    .asGif()
                    .dontTransform()
                    .load(intent.data)
                    .addListener(object : RequestListener<GifDrawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                            Log.e("ImageActivity", "GIF: ${e?.message}")
                            showWarning(Warnings.getInureWarning02(intent.data.toString(), "GIF"))
                            return true
                        }

                        override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            Log.d("ImageActivity", "GIF: ${resource?.intrinsicWidth}x${resource?.intrinsicHeight}")
                            // gif.setImageDrawable(resource) // This is not working
                            image.gone()
                            if (savedInstanceState.isNotNull()) {
                                gif.currentZoom = savedInstanceState!!.getFloat("zoom")
                            }
                            return false
                        }

                    })
                    .into(gif)
            } else {
                Glide.with(applicationContext)
                    .asBitmap()
                    .dontAnimate()
                    .dontTransform()
                    .load(intent.data)
                    .addListener(object : RequestListener<Bitmap> {
                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            image.setImage(ImageSource.bitmap(resource!!))
                            gif.gone()
                            if (savedInstanceState.isNotNull()) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    image.setScaleAndCenter(savedInstanceState!!.getFloat("scale"), savedInstanceState.getParcelable("center", PointF::class.java)!!)
                                } else {
                                    @Suppress("DEPRECATION")
                                    image.setScaleAndCenter(savedInstanceState!!.getFloat("scale"), savedInstanceState.getParcelable("center")!!)
                                }
                            }
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            Log.e("ImageActivity", "Bitmap: ${e?.message}")
                            showWarning(Warnings.getInureWarning02(intent.data.toString(), "Bitmap"))
                            return true
                        }
                    })
                    .preload()
            }
        }

        name.text = kotlin.runCatching {
            DocumentFile.fromSingleUri(this, intent!!.data!!)!!.name
        }.getOrElse {
            getString(R.string.not_available)
        }

        image.setOnClickListener {
            isFullScreen = if (isFullScreen) {
                setFullScreen(header.height.toFloat() * -1F)
                false
            } else {
                setFullScreen(0F)
                true
            }
        }

        gif.setOnClickListener {
            image.callOnClick()
        }

        back.setOnClickListener {
            finish()
        }
    }

    private fun setFullScreen(translationY: Float) {
        header.animate()
            .translationY(translationY)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("translation", header.translationY)
        outState.putBoolean("fullscreen", isFullScreen)
        kotlin.runCatching {
            if (image.isVisible) {
                outState.putFloat("scale", image.scale)
                outState.putParcelable("center", image.center)
            }
        }.getOrElse {
            outState.putFloat("scale", image.scale)
            outState.putParcelable("center", image.center)
        }
        kotlin.runCatching {
            if (gif.isVisible) {
                outState.putFloat("zoom", gif.currentZoom)
            }
        }.getOrElse {
            outState.putFloat("zoom", 1F)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        setFullScreen(savedInstanceState.getFloat("translation"))
        isFullScreen = savedInstanceState.getBoolean("fullscreen")
        super.onRestoreInstanceState(savedInstanceState)
    }
}
