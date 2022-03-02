package app.simple.inure.activities.association

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.exceptions.LargeStringException
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.ViewUtils.visible
import com.anggrayudi.storage.file.fullName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.apache.commons.io.IOUtils
import java.io.IOException

class TextViewerActivity : BaseActivity() {

    private lateinit var txt: TypeFaceEditText
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton

    private val exportManifest = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
        if (uri == null) {
            return@registerForActivityResult
        }
        try {
            contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(txt.text.toString().toByteArray())
                outputStream.flush()
                Toast.makeText(applicationContext, R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        FastScrollerBuilder(findViewById(R.id.text_viewer_scroll_view)).build()

        txt = findViewById(R.id.text_viewer)
        path = findViewById(R.id.txt_name)
        options = findViewById(R.id.txt_viewer_options)

        path.text = kotlin.runCatching {
            DocumentFile.fromSingleUri(this, intent!!.data!!)!!.fullName
        }.getOrElse {
            getString(R.string.not_available)
        }

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", txt.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.save) -> {
                            exportManifest.launch(DocumentFile.fromSingleUri(applicationContext, intent!!.data!!)!!.name)
                        }
                    }
                }
            })
        }

        lifecycleScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                withTimeout(3000) {
                    val string = contentResolver.openInputStream(intent.data!!)!!.use {
                        IOUtils.toString(it, "UTF-8")
                    }

                    withContext(Dispatchers.Main) {
                        if (string.length >= 150000 && !FormattingPreferences.isLoadingLargeStrings()) {
                            throw LargeStringException("String size ${string.length} is too big to render without freezing the app")
                        }
                        txt.setText(string)
                        options.visible(true)
                    }
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    val e = Error.newInstance(it.stackTraceToString())
                    e.show(supportFragmentManager, "error_dialog")
                    e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                        override fun onDismiss() {
                            onBackPressed()
                        }
                    })
                }
            }
        }
    }
}