package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.exception.FileOrStringTooBigException
import app.simple.inure.extension.fragments.ScopedFragment
import kotlinx.coroutines.*
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class TextViewer : ScopedFragment() {

    private lateinit var txt: TypeFaceTextView
    private lateinit var path: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_text_viewer, container, false)

        txt = view.findViewById(R.id.text_viewer)
        path = view.findViewById(R.id.txt_name)

        startPostponedEnterTransition()

        return view
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applicationInfo = requireArguments().getParcelable("application_info")!!
        path.text = requireArguments().getString("path")!!

        launch {
            runCatching {
                val string: String

                withContext(Dispatchers.IO) {
                    string = IOUtils.toString(getInputStream(), "UTF-8")
                }
                if (string.length >= 100000) {
                    throw FileOrStringTooBigException("String is too big to render without freezing the app")
                }
                txt.text = string
            }.getOrElse {
                txt.text = it.stackTrace.toString()
                txt.setTextColor(Color.RED)
            }
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun getInputStream(): InputStream {
        val waitFor = CoroutineScope(Dispatchers.IO).async {
            runCatching {
                ZipFile(applicationInfo.sourceDir).use {
                    val entries: Enumeration<out ZipEntry?> = it.entries()
                    while (entries.hasMoreElements()) {
                        val entry: ZipEntry? = entries.nextElement()
                        val name: String = entry!!.name
                        if (name == requireArguments().getString("path")) {
                            return@async BufferedInputStream(ZipFile(applicationInfo.sourceDir).getInputStream(entry))
                        }
                    }
                }
            }.getOrElse {
                it.printStackTrace()
            }
        }

        waitFor.await()

        return waitFor.getCompleted() as InputStream
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo, path: String): TextViewer {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            args.putString("path", path)
            val fragment = TextViewer()
            fragment.arguments = args
            return fragment
        }
    }
}