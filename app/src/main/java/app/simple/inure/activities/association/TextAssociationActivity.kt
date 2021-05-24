package app.simple.inure.activities.association

import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.exception.StringTooLargeException
import app.simple.inure.extension.activities.BaseActivity
import kotlinx.coroutines.*
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

        path.text = intent.data!!.path

        CoroutineScope(Dispatchers.Default).launch {
            kotlin.runCatching {
                withTimeout(3000) {
                    val string = IOUtils.toString(contentResolver.openInputStream(intent.data!!), "UTF-8")

                    withContext(Dispatchers.Main) {
                        if(string.length >= 100000) throw StringTooLargeException("String is too big to render without freezing the app")
                        txt.text = string
                    }
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    txt.text = it.stackTraceToString()
                    txt.setTextColor(Color.RED)
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
                fileExtension.toLowerCase(Locale.ROOT))
        }
    }
}