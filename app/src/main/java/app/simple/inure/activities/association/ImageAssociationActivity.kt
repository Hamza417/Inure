package app.simple.inure.activities.association

import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.documentfile.provider.DocumentFile
import app.simple.inure.R
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ZoomImageView
import app.simple.inure.extension.activities.BaseActivity
import com.bumptech.glide.Glide

class ImageAssociationActivity : BaseActivity() {

    private lateinit var image: ZoomImageView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var name: TypeFaceTextView
    private lateinit var header: PaddingAwareLinearLayout

    private var isFullScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_image)
        super.onCreate(savedInstanceState)

        image = findViewById(R.id.image_viewer)
        back = findViewById(R.id.image_viewer_back_button)
        name = findViewById(R.id.image_name)
        header = findViewById(R.id.header)

        Glide.with(this)
                .asBitmap()
                .dontAnimate()
                .dontTransform()
                .load(intent.data)
                .into(image)

        image.swipeToDismissEnabled = false

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
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        setFullScreen(savedInstanceState.getFloat("translation"))
        isFullScreen = savedInstanceState.getBoolean("fullscreen")
        super.onRestoreInstanceState(savedInstanceState)
    }
}
