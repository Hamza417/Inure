package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceEditText
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.exception.StringTooLargeException
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.preferences.ConfigurationPreferences
import kotlinx.coroutines.*
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class TextViewer : ScopedFragment() {

    private lateinit var txt: TypeFaceEditText
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton

    private val exportText = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(txt.text.toString().toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_text_viewer, container, false)

        txt = view.findViewById(R.id.text_viewer)
        path = view.findViewById(R.id.txt_name)
        options = view.findViewById(R.id.txt_viewer_options)

        startPostponedEnterTransition()

        return view
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applicationInfo = requireArguments().getParcelable("application_info")!!
        path.text = requireArguments().getString("path")!!

        viewLifecycleOwner.lifecycleScope.launch {
            delay(500L)
            runCatching {
                val string: String

                withContext(Dispatchers.IO) {
                    string = IOUtils.toString(getInputStream(), "UTF-8")
                }
                if (string.length >= 150000 && !ConfigurationPreferences.isLoadingLargeStrings()) {
                    throw StringTooLargeException("String size ${string.length} is too big to render without freezing the app")
                }
                txt.setText(string)
            }.getOrElse {
                txt.setText(it.stackTraceToString())
                txt.setTextColor(Color.RED)
            }
        }

        options.setOnClickListener {
            val p = PopupXmlViewer(LayoutInflater.from(requireContext())
                                           .inflate(R.layout.popup_xml_options,
                                                    PopupLinearLayout(requireContext()),
                                                    true), it)

            p.setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", txt.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.save) -> {
                            val fileName: String = applicationInfo.packageName + "_" + path.text
                            exportText.launch(fileName)
                        }
                    }
                }
            })
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
