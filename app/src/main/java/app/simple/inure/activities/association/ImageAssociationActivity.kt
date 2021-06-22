package app.simple.inure.activities.association

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.documentfile.provider.DocumentFile
import app.simple.inure.R
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.util.NullSafety.isNotNull
import com.pdfview.subsamplincscaleimageview.ImageSource
import com.pdfview.subsamplincscaleimageview.ImageViewState
import com.pdfview.subsamplincscaleimageview.SubsamplingScaleImageView

class ImageAssociationActivity : BaseActivity() {

    private lateinit var image: SubsamplingScaleImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var gradient: View

    private var isFullScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        image = findViewById(R.id.image_viewer)
        back = findViewById(R.id.image_viewer_back_button)
        name = findViewById(R.id.image_name)
        header = findViewById(R.id.header)
        gradient = findViewById(R.id.gradient)

        image.isPanEnabled = true
        image.isZoomEnabled = true

        if(savedInstanceState.isNotNull()) {
            image.setImage(ImageSource.uri(intent?.data!!), savedInstanceState!!.getSerializable("image") as ImageViewState)
        } else {
            image.setImage(ImageSource.uri(intent?.data!!))
        }

        name.text = DocumentFile.fromSingleUri(this, intent!!.data!!)!!.name

        image.setOnClickListener {
            isFullScreen = if (isFullScreen) {
                setFullScreen(header.height.toFloat() * -1F, 0F)
                false
            } else {
                setFullScreen(0F, 1F)
                true
            }
        }

        back.setOnClickListener {
            finish()
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


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        setFullScreen(savedInstanceState.getFloat("translation"), savedInstanceState.getFloat("alpha"))
        isFullScreen = savedInstanceState.getBoolean("fullscreen")
        super.onRestoreInstanceState(savedInstanceState)
    }
}
