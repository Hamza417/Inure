package app.simple.inure.activities.association

import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import app.simple.inure.R
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.util.NullSafety.isNotNull
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.io.InputStream

class ImageAssociationActivity : BaseActivity() {

    private lateinit var image: SubsamplingScaleImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var header: PaddingAwareLinearLayout

    private var isFullScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        image = findViewById(R.id.image_viewer)
        back = findViewById(R.id.image_viewer_back_button)
        name = findViewById(R.id.image_name)
        header = findViewById(R.id.header)

        image.isPanEnabled = true
        image.isZoomEnabled = true
        image.maxScale = 100F
        image.minScale = -100F
        image.setExifOrientation(contentResolver.openInputStream(intent?.data!!)!!)

        if (savedInstanceState.isNotNull()) {
            image.setImage(ImageSource.uri(intent?.data!!), savedInstanceState!!.getSerializable("image") as ImageViewState)
        } else {
            image.setImage(ImageSource.uri(intent?.data!!))
        }

        name.text = DocumentFile.fromSingleUri(this, intent!!.data!!)!!.name

        image.setOnClickListener {
            isFullScreen = if (isFullScreen) {
                setFullScreen(header.height.toFloat() * -1F)
                false
            } else {
                setFullScreen(0F)
                true
            }
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

    private fun SubsamplingScaleImageView.setExifOrientation(inputStream: InputStream) {
        val exifOrientation = ExifInterface(inputStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        orientation = when (exifOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                scaleX = -1f
                SubsamplingScaleImageView.ORIENTATION_0
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                scaleY = -1f
                SubsamplingScaleImageView.ORIENTATION_0
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                scaleX = -1f
                SubsamplingScaleImageView.ORIENTATION_270
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                scaleX = -1f
                SubsamplingScaleImageView.ORIENTATION_90
            }
            else -> SubsamplingScaleImageView.ORIENTATION_USE_EXIF
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("translation", header.translationY)
        outState.putBoolean("fullscreen", isFullScreen)
        outState.putSerializable("image", image.state)
        super.onSaveInstanceState(outState)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        setFullScreen(savedInstanceState.getFloat("translation"))
        isFullScreen = savedInstanceState.getBoolean("fullscreen")
        super.onRestoreInstanceState(savedInstanceState)
    }
}
