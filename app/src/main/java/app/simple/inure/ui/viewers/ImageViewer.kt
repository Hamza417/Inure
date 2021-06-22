package app.simple.inure.ui.viewers

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import app.simple.inure.R
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.ImageLoader.loadGraphics
import app.simple.inure.util.NullSafety.isNotNull
import com.pdfview.subsamplincscaleimageview.ImageViewState
import com.pdfview.subsamplincscaleimageview.SubsamplingScaleImageView
import org.jetbrains.annotations.NotNull


class ImageViewer : ScopedFragment() {

    private lateinit var image: SubsamplingScaleImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var gradient: View

    private var isFullScreen = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_viewer, container, false)

        image = view.findViewById(R.id.image_viewer)
        back = view.findViewById(R.id.image_viewer_back_button)
        name = view.findViewById(R.id.image_name)
        header = view.findViewById(R.id.header)
        gradient = view.findViewById(R.id.gradient)

        startPostponedEnterTransition()

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image.isPanEnabled = true
        image.isZoomEnabled = true

        if (savedInstanceState.isNotNull()) {
            image.loadGraphics(requireContext(),
                               requireArguments().getString("path_of_apk")!!,
                               requireArguments().getString("path_of_image")!!,
                               savedInstanceState!!.getSerializable("image") as ImageViewState)
        } else {
            image.loadGraphics(requireContext(),
                               requireArguments().getString("path_of_apk")!!,
                               requireArguments().getString("path_of_image")!!)
        }

        name.text = requireArguments().getString("path_of_image")

        back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        image.setOnClickListener {
            isFullScreen = if (isFullScreen) {
                setFullScreen(header.height.toFloat() * -1F, 0F)
                false
            } else {
                setFullScreen(0F, 1F)
                true
            }
        }
    }

    private fun setFullScreen(translationY: Float, alpha: Float) {
        header.animate().translationY(translationY).setInterpolator(DecelerateInterpolator()).start()
        gradient.animate().alpha(alpha).setInterpolator(DecelerateInterpolator()).start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("translation", header.translationY)
        outState.putFloat("alpha", gradient.alpha)
        outState.putBoolean("fullscreen", isFullScreen)
        outState.putSerializable("image", image.state)
        super.onSaveInstanceState(outState)
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState.isNotNull()) {
            setFullScreen(savedInstanceState!!.getFloat("translation"), savedInstanceState.getFloat("alpha"))
            isFullScreen = savedInstanceState.getBoolean("fullscreen")
        }
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
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