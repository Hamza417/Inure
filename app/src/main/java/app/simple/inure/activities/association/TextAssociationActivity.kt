package app.simple.inure.activities.association

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.exceptions.StringTooLargeException
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.preferences.ConfigurationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.apache.commons.io.IOUtils
import java.util.*


class TextAssociationActivity : BaseActivity() {

    private lateinit var txt: TypeFaceTextView
    private lateinit var path: TypeFaceTextView
    private lateinit var close: DynamicRippleImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        txt = findViewById(R.id.text_viewer)
        path = findViewById(R.id.txt_name)
        close = findViewById(R.id.close)

        path.text = kotlin.runCatching {
            DocumentFile.fromSingleUri(this, intent!!.data!!)!!.name
        }.getOrElse {
            getString(R.string.not_available)
        }

        lifecycleScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                withTimeout(3000) {
                    val string = contentResolver.openInputStream(intent.data!!)!!.use {
                        IOUtils.toString(it, "UTF-8")
                    }

                    withContext(Dispatchers.Main) {
                        if (string.length >= 150000 && !ConfigurationPreferences.isLoadingLargeStrings()) {
                            throw StringTooLargeException("String size ${string.length} is too big to render without freezing the app")
                        }
                        txt.text = string
                    }
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    val e = ErrorPopup.newInstance(it.stackTraceToString())
                    e.show(supportFragmentManager, "error_dialog")
                    e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                        override fun onDismiss() {
                            onBackPressed()
                        }
                    })
                }
            }
        }

        close.setOnClickListener {
            finish()
        }
    }

    private fun getMimeType(uri: Uri): String? {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.ROOT))
        }
    }
}