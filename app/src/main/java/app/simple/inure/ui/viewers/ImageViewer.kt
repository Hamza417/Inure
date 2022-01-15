package app.simple.inure.ui.viewers

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ZoomImageView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.ImageLoader.loadGraphics
import app.simple.inure.preferences.ImageViewerPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.NullSafety.isNotNull
import com.google.android.material.animation.ArgbEvaluatorCompat
import org.jetbrains.annotations.NotNull

class ImageViewer : ScopedFragment() {

    private lateinit var image: ZoomImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var background: FrameLayout

    private var isFullScreen = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_viewer, container, false)

        image = view.findViewById(R.id.image_viewer)
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

        image.loadGraphics(requireContext(),
                           requireArguments().getString("path_of_apk")!!,
                           requireArguments().getString("path_of_image")!!)

        name.text = requireArguments().getString("path_of_image")

        back.setOnClickListener {
            requireActivity().onBackPressed()
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

        options.setOnClickListener {

        }
    }

    private fun setFullScreen(translationY: Float) {
        header.animate().translationY(translationY).setInterpolator(DecelerateInterpolator()).start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("translation", header.translationY)
        outState.putBoolean("fullscreen", isFullScreen)
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
        fun newInstance(@NotNull pathOfApk: String, filePath: String): ImageViewer {
            val args = Bundle()
            args.putString("path_of_apk", pathOfApk)
            args.putString("path_of_image", filePath)
            val fragment = ImageViewer()
            fragment.arguments = args
            return fragment
        }
    }
}