package app.simple.inure.ui.viewers

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ZoomImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.viewers.ImageViewerViewModelFactory
import app.simple.inure.preferences.ImageViewerPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.ImageViewerViewModel
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.animation.ArgbEvaluatorCompat

class ImageViewer : ScopedFragment() {

    private lateinit var image: SubsamplingScaleImageView
    private lateinit var gif: ZoomImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var background: FrameLayout

    private val imageViewerViewModel: ImageViewerViewModel by lazy {
        val p0 = ImageViewerViewModelFactory(requireArguments().getString(BundleConstants.pathToImage)!!, requireArguments().getString(BundleConstants.pathToApk)!!)
        ViewModelProvider(this, p0)[ImageViewerViewModel::class.java]
    }

    private var isFullScreen = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_viewer, container, false)

        image = view.findViewById(R.id.image_viewer)
        gif = view.findViewById(R.id.gif_viewer)
        back = view.findViewById(R.id.image_viewer_back_button)
        name = view.findViewById(R.id.image_name)
        options = view.findViewById(R.id.image_viewer_option)
        header = view.findViewById(R.id.header)
        background = view.findViewById(R.id.image_viewer_container)

        startPostponedEnterTransition()

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewerViewModel.getBitmap().observe(viewLifecycleOwner) {
            image.setImage(ImageSource.bitmap(it))
            gif.gone()
            if (savedInstanceState.isNotNull()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    image.setScaleAndCenter(savedInstanceState!!.getFloat("scale"), savedInstanceState.getParcelable("center", PointF::class.java)!!)
                } else {
                    @Suppress("DEPRECATION")
                    image.setScaleAndCenter(savedInstanceState!!.getFloat("scale"), savedInstanceState.getParcelable("center")!!)
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

        options.setOnClickListener {

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
            ImageViewerPreferences.isBackgroundDark -> {
                setBackgroundColor()
            }
        }
    }

    private fun setBackgroundColor() {
        val colorAnim = if (ImageViewerPreferences.isBackgroundDark()) {
            ValueAnimator.ofObject(ArgbEvaluatorCompat(), ThemeManager.theme.viewGroupTheme.viewerBackground, Color.BLACK)
        } else {
            ValueAnimator.ofObject(ArgbEvaluatorCompat(), Color.BLACK, ThemeManager.theme.viewGroupTheme.viewerBackground)
        }
        colorAnim.duration = 1000
        colorAnim.interpolator = LinearOutSlowInInterpolator()
        colorAnim.addUpdateListener { animation -> image.setBackgroundColor(animation.animatedValue as Int) }
        colorAnim.start()
    }

    companion object {
        fun newInstance(pathToApk: String, imagePath: String): ImageViewer {
            val args = Bundle()
            args.putString(BundleConstants.pathToApk, pathToApk)
            args.putString(BundleConstants.pathToImage, imagePath)
            val fragment = ImageViewer()
            fragment.arguments = args
            return fragment
        }
    }
}