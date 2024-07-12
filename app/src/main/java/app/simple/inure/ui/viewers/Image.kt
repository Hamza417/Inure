package app.simple.inure.ui.viewers

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ZoomImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.viewers.ImageViewerViewModelFactory
import app.simple.inure.popups.viewers.PopupImageViewer
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.ImageViewerPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.ImageViewerViewModel
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.animation.ArgbEvaluatorCompat
import java.io.File
import java.io.IOException
import kotlin.math.abs

class Image : ScopedFragment() {

    private lateinit var image: SubsamplingScaleImageView
    private lateinit var gif: ZoomImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var backgroundMode: DynamicRippleImageButton
    private lateinit var options: DynamicRippleImageButton
    private lateinit var header: LinearLayout
    private lateinit var background: FrameLayout

    private val imageViewerViewModel: ImageViewerViewModel by lazy {
        val imageViewerViewModelFactory = ImageViewerViewModelFactory(
                requireArguments().getString(BundleConstants.pathToImage)!!,
                requireArguments().getString(BundleConstants.pathToApk)!!)

        ViewModelProvider(this, imageViewerViewModelFactory)[ImageViewerViewModel::class.java]
    }

    private val exportImage = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(filePath?.let { File(it).readBytes() })
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    private var isFullScreen = true
    private var filePath: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_viewer, container, false)

        image = view.findViewById(R.id.image_viewer)
        gif = view.findViewById(R.id.gif_viewer)
        back = view.findViewById(R.id.image_viewer_back_button)
        name = view.findViewById(R.id.image_name)
        backgroundMode = view.findViewById(R.id.bg_mode)
        options = view.findViewById(R.id.image_viewer_option)
        header = view.findViewById(R.id.header)
        background = view.findViewById(R.id.image_viewer_container)

        startPostponedEnterTransition()

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView).isAppearanceLightStatusBars = false
        setBackgroundColor(animate = false)

        with(header) {
            if (DevelopmentPreferences.get(DevelopmentPreferences.disableTransparentStatus)) {
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

        imageViewerViewModel.getBitmap().observe(viewLifecycleOwner) {
            image.setImage(ImageSource.bitmap(it))
            gif.gone()
            if (savedInstanceState.isNotNull()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    image.setScaleAndCenter(
                            savedInstanceState!!.getFloat("scale"),
                            savedInstanceState.getParcelable("center", PointF::class.java)!!)
                } else {
                    @Suppress("DEPRECATION")
                    image.setScaleAndCenter(
                            savedInstanceState!!.getFloat("scale"),
                            savedInstanceState.getParcelable("center")!!)
                }
            }
        }

        imageViewerViewModel.getGif().observe(viewLifecycleOwner) {
            gif.setImageDrawable(it)
            image.gone()
            if (savedInstanceState.isNotNull()) {
                gif.currentZoom = savedInstanceState!!.getFloat("zoom")
            }
        }

        imageViewerViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }

        name.text = requireArguments().getString(BundleConstants.pathToImage)

        back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
            ImageViewerPreferences.setBackgroundMode(
                    !ImageViewerPreferences.isBackgroundDark())
        }

        options.setOnClickListener { it ->
            PopupImageViewer(it).setOnPopupClickedListener(object : PopupImageViewer.PopupImageCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.export) -> {
                            showLoader(manualOverride = true)
                            imageViewerViewModel.exportImage()

                            imageViewerViewModel.path.observe(viewLifecycleOwner) {
                                hideLoader()
                                if (it.isNotNull()) {
                                    try {
                                        filePath = it
                                        exportImage.launch(it.substringAfterLast("/"))
                                        imageViewerViewModel.path.removeObservers(viewLifecycleOwner)
                                    } catch (e: ActivityNotFoundException) {
                                        showError(Warnings.getActivityNotFoundWarning())
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun setFullScreen(translationY: Float) {
        header.animate().translationY(translationY).setInterpolator(DecelerateInterpolator()).start()
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState.isNotNull()) {
            setFullScreen(savedInstanceState!!.getFloat("translation"))
            isFullScreen = savedInstanceState.getBoolean("fullscreen")
        }
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ImageViewerPreferences.IS_BACKGROUND_DARK -> {
                setBackgroundColor()
            }
        }
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
            options.imageTintList = ColorStateList.valueOf(Color.WHITE)
            back.imageTintList = ColorStateList.valueOf(Color.WHITE)
            ThemeUtils.manualBarColors(light = false, requireActivity().window)

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
            options.imageTintList = ColorStateList.valueOf(AppearancePreferences.getAccentColor())
            back.imageTintList = ColorStateList.valueOf(AppearancePreferences.getAccentColor())
            ThemeUtils.manualBarColors(light = true, requireActivity().window)

            header.background = GradientDrawable().apply {
                colors = intArrayOf(Color.WHITE, Color.TRANSPARENT)
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                gradientType = GradientDrawable.LINEAR_GRADIENT
                shape = GradientDrawable.RECTANGLE
            }
        }
    }

    override fun onDestroy() {
        ThemeUtils.setBarColors(resources, requireActivity().window)
        super.onDestroy()
    }

    companion object {
        fun newInstance(pathToApk: String, imagePath: String): Image {
            val args = Bundle()
            args.putString(BundleConstants.pathToApk, pathToApk)
            args.putString(BundleConstants.pathToImage, imagePath)
            val fragment = Image()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Image"
    }
}
