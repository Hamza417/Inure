package app.simple.inure.activities.association

import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import androidx.documentfile.provider.DocumentFile
import app.simple.inure.R
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.activities.BaseActivity
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy

class PDFAssociationActivity : BaseActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var name: TypeFaceTextView
    private lateinit var pdfContainer: FrameLayout
    private lateinit var header: PaddingAwareLinearLayout

    private var isFullScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        pdfView = findViewById(R.id.pdf_viewer)
        name = findViewById(R.id.pdf_name)
        pdfContainer = findViewById(R.id.pdf_container)
        header = findViewById(R.id.header_pdf)

        name.text = DocumentFile.fromSingleUri(this, intent!!.data!!)!!.name

        header.doOnLayout {
            pdfContainer.apply {
                setPadding(paddingLeft, header.height + paddingTop, paddingRight, paddingBottom)
            }

            pdfView.useBestQuality(true)
            pdfView.fromUri(intent!!.data!!)
                    .defaultPage(0)
                    .enableAnnotationRendering(true)
                    .swipeHorizontal(false)
                    .spacing(25)
                    .fitEachPage(false)
                    .onError {
                        val e = ErrorPopup.newInstance(it.message!!)
                        e.show(supportFragmentManager, "error_dialog")
                        e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                            override fun onDismiss() {
                                onBackPressed()
                            }
                        })
                    }
                    .enableAntialiasing(true)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .onTap {
                        isFullScreen = if (isFullScreen) {
                            header.animate().translationY(header.height.toFloat() * -1).setInterpolator(DecelerateInterpolator()).start()
                            false
                        } else {
                            header.animate().translationY(0F).setInterpolator(DecelerateInterpolator()).start()
                            true
                        }
                        true
                    }
                    .load()
        }
    }
}