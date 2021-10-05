package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.viewmodels.factory.TextDataFactory
import app.simple.inure.viewmodels.viewers.TextViewerData
import com.mittsu.markedview.MarkedView
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

class Markdown : ScopedFragment() {

    private lateinit var codeView: MarkedView
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton

    private lateinit var textViewerData: TextViewerData
    private lateinit var textDataFactory: TextDataFactory

    private var code = ""

    private var backPress: OnBackPressedDispatcher? = null

    private val exportText = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(code.toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_markdown_viewer, container, false)

        codeView = view.findViewById(R.id.code_viewer)
        path = view.findViewById(R.id.code_name)
        options = view.findViewById(R.id.code_viewer_options)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        textDataFactory = TextDataFactory(
            applicationInfo,
            requireArguments().getString("path")!!,
            requireActivity().application,
        )

        backPress = requireActivity().onBackPressedDispatcher
        textViewerData = ViewModelProvider(this, textDataFactory).get(TextViewerData::class.java)

        path.text = requireArguments().getString("path")!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        textViewerData.getText().observe(viewLifecycleOwner, {
            code = it
            codeView.setBackgroundColor(0)
            codeView.settings.apply {
                setSupportZoom(false)
                useWideViewPort = false
                builtInZoomControls = true
                displayZoomControls = false
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            }
            codeView.canGoBack()
            codeView.setMDText(it)
        })

        options.setOnClickListener {
            val p = PopupXmlViewer(it)

            p.setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("code", code)
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

        backPress?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println(codeView.canGoBack())
                if (codeView.canGoBack()) {
                    codeView.goBack()
                    codeView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                } else {
                    remove()
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        codeView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo, path: String): Markdown {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            args.putString("path", path)
            val fragment = Markdown()
            fragment.arguments = args
            return fragment
        }
    }
}
