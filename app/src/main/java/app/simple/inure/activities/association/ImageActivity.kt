package app.simple.inure.activities.association

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ZoomImageView
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.glide.svg.SVG
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.DevelopmentPreferences.get
import app.simple.inure.preferences.ImageViewerPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.util.FileUtils.getMimeType
import app.simple.inure.util.FileUtils.isSVG
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ProcessUtils
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils.gone
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlin.math.abs

class ImageActivity : BaseActivity() {

    private lateinit var image: SubsamplingScaleImageView
    private lateinit var gif: ZoomImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var backgroundMode: DynamicRippleImageButton
    private lateinit var header: LinearLayout

    private var isFullScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        image = findViewById(R.id.image_viewer)
        gif = findViewById(R.id.gif_viewer)
        back = findViewById(R.id.image_viewer_back_button)
        name = findViewById(R.id.image_name)
        backgroundMode = findViewById(R.id.bg_mode)
        header = findViewById(R.id.header)

        setBackgroundColor(animate = false)

        with(header) {
            if (get(DevelopmentPreferences.disableTransparentStatus)) {
                if (paddingTop >= StatusBarHeight.getStatusBarHeight(resources)) {
                    setPadding(paddingLeft,
                               abs(StatusBarHeight.getStatusBarHeight(resources) - paddingTop),
                               paddingRight,
                               paddingBottom)
                }
            } else {
                setPadding(paddingLeft,
                           StatusBarHeight.getStatusBarHeight(resources) + paddingTop,
                           paddingRight,
                           paddingBottom)
            }
        }

        ProcessUtils.ensureOnMainThread {
            kotlin.runCatching {
                if (intent.data!!.getMimeType(applicationContext)?.endsWith("gif") == true) {
                    Glide.with(applicationContext)
                        .asGif()
                        .dontTransform()
                        .load(intent.data)
                        .addListener(object : RequestListener<GifDrawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                                Log.e("ImageActivity", "GIF: ${e?.message}")
                                showWarning(Warnings.getFailedToLoadFileWarning(intent.data.toString(), "GIF"))
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
                } else if (intent.data!!.isSVG(applicationContext)) {
                    Log.d("ImageActivity", "SVG: ${intent.data}")
                    Glide.with(applicationContext)
                        .asBitmap()
                        .dontAnimate()
                        .dontTransform()
                        .load(SVG(applicationContext, intent.data!!))
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
                                Log.e("ImageActivity", "SVG: ${e?.message}")
                                showWarning(Warnings.getFailedToLoadFileWarning(intent.data.toString(), "SVG"))
                                return true
                            }
                        })
                        .preload()
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
                                showWarning(Warnings.getFailedToLoadFileWarning(intent.data.toString(), "Bitmap"))
                                return true
                            }
                        })
                        .preload()
                }
            }.onFailure {
                showWarning(it.message ?: it.cause.toString())
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

        backgroundMode.setOnClickListener {
            ImageViewerPreferences.setBackgroundMode(!ImageViewerPreferences.isBackgroundDark())
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

    private fun setBackgroundColor(animate: Boolean = true) {
        if (animate) {
            val colorAnim = if (ImageViewerPreferences.isBackgroundDark()) {
                ValueAnimator.ofObject(ArgbEvaluatorCompat(), ThemeManager.theme.viewGroupTheme.background, Color.BLACK)
            } else {
                ValueAnimator.ofObject(ArgbEvaluatorCompat(), Color.BLACK, ThemeManager.theme.viewGroupTheme.background)
            }

            colorAnim.duration = resources.getInteger(R.integer.animation_duration).toLong()
            colorAnim.interpolator = LinearOutSlowInInterpolator()
            colorAnim.addUpdateListener { animation -> image.setBackgroundColor(animation.animatedValue as Int) }
            colorAnim.start()
        } else {
            if (ImageViewerPreferences.isBackgroundDark()) {
                image.setBackgroundColor(Color.BLACK)
            } else {
                image.setBackgroundColor(ThemeManager.theme.viewGroupTheme.background)
            }
        }

        if (ImageViewerPreferences.isBackgroundDark()) {
            backgroundMode.setImageResource(R.drawable.ic_light_mode)
            backgroundMode.imageTintList = ColorStateList.valueOf(Color.WHITE)
            name.setTextColor(Color.WHITE)
            back.imageTintList = ColorStateList.valueOf(Color.WHITE)
            ThemeUtils.manualBarColors(light = false, window)

            header.background = GradientDrawable().apply {
                colors = intArrayOf(Color.BLACK, Color.TRANSPARENT)
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                gradientType = GradientDrawable.LINEAR_GRADIENT
                shape = GradientDrawable.RECTANGLE
            }
        } else {
            backgroundMode.setImageResource(R.drawable.ic_dark_mode)
            backgroundMode.imageTintList = ColorStateList.valueOf(AppearancePreferences.getAccentColor())
            name.setTextColor(AppearancePreferences.getAccentColor())
            back.imageTintList = ColorStateList.valueOf(AppearancePreferences.getAccentColor())
            ThemeUtils.manualBarColors(light = true, window)

            header.background = GradientDrawable().apply {
                colors = intArrayOf(Color.WHITE, Color.TRANSPARENT)
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                gradientType = GradientDrawable.LINEAR_GRADIENT
                shape = GradientDrawable.RECTANGLE
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ImageViewerPreferences.isBackgroundDark -> {
                setBackgroundColor()
            }
        }
    }

    override fun onDestroy() {
        ThemeUtils.setBarColors(resources, window)
        super.onDestroy()
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
