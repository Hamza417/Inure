package app.simple.inure.activities.association

import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.util.FileUtils
import com.pdfview.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

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
        }

        pdfView.setOnClickListener {
            isFullScreen = if (isFullScreen) {
                header.animate().translationY(header.height.toFloat() * -1).setInterpolator(DecelerateInterpolator()).start()
                false
            } else {
                header.animate().translationY(0F).setInterpolator(DecelerateInterpolator()).start()
                true
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val file = getPdfFile(contentResolver.openInputStream(intent.data!!)!!)

            withContext(Dispatchers.Main) {
                pdfView.fromFile(file!!)
                pdfView.show()
            }
        }
    }

    private fun getPdfFile(inputStream: InputStream): File? {
        kotlin.runCatching {
            File(applicationContext.getExternalFilesDir(null)!!.path + "/cache/").mkdir()
            val file = File(applicationContext.getExternalFilesDir(null)?.path + "/cache/" + "temp_pdf.pdf")
            FileUtils.copyStreamToFile(inputStream, file)
            return file
        }.getOrElse {
            it.printStackTrace()
        }

        return null
    }
}