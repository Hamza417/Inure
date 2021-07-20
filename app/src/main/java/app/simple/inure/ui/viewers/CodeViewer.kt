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
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnLayout
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.factory.TextDataFactory
import app.simple.inure.viewmodels.viewers.TextViewerData
import com.pddstudio.highlightjs.HighlightJsView
import com.pddstudio.highlightjs.models.Language
import com.pddstudio.highlightjs.models.Theme
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

class CodeViewer : ScopedFragment() {

    private lateinit var codeView: HighlightJsView
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var viewContainer: FrameLayout

    private lateinit var textViewerData: TextViewerData
    private lateinit var textDataFactory: TextDataFactory

    private var code = ""

    private var isFullScreen = true

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
        val view = inflater.inflate(R.layout.fragment_code_viewer, container, false)

        codeView = view.findViewById(R.id.code_viewer)
        path = view.findViewById(R.id.code_name)
        options = view.findViewById(R.id.code_viewer_options)
        header = view.findViewById(R.id.header_code_view)
        viewContainer = view.findViewById(R.id.code_container)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        textDataFactory = TextDataFactory(
            applicationInfo,
            requireArguments().getString("path")!!,
            requireActivity().application,
        )

        textViewerData = ViewModelProvider(this, textDataFactory).get(TextViewerData::class.java)

        path.text = requireArguments().getString("path")!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        if (savedInstanceState.isNotNull()) {
            println(savedInstanceState?.getBoolean("full_screen"))
        }

        header.doOnLayout {
            viewContainer.apply {
                setPadding(paddingLeft, header.height + paddingTop, paddingRight, paddingBottom)
            }
        }

        codeView.setOnClickListener {
            setFullScreen()
        }

        textViewerData.getText().observe(viewLifecycleOwner, {
            code = it
            codeView.highlightLanguage = getLanguage()
            codeView.theme = Theme.GOOGLECODE
            codeView.setZoomSupportEnabled(true)
            codeView.setShowLineNumbers(true)
            codeView.setBackgroundColor(0)
            codeView.setSource(it)
        })

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
    }

    private fun getLanguage(): Language {
        return when (requireArguments().getString("language")!!) {
            "css" -> {
                Language.CSS
            }
            "java" -> {
                Language.JAVA
            }
            "js" -> {
                Language.JAVA_SCRIPT
            }
            "json" -> {
                Language.JSON
            }
            "md" -> {
                Language.MARKDOWN
            }
            else -> {
                Language.AUTO_DETECT
            }
        }
    }

    private fun setFullScreen() {
        isFullScreen = if (isFullScreen) {
            header.animate().translationY(header.height.toFloat() * -1).setInterpolator(DecelerateInterpolator()).start()
            false
        } else {
            header.animate().translationY(0F).setInterpolator(DecelerateInterpolator()).start()
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        codeView.saveState(outState)
        outState.putBoolean("full_screen", isFullScreen)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo, path: String, language: String): CodeViewer {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            args.putString("path", path)
            args.putString("language", language)
            val fragment = CodeViewer()
            fragment.arguments = args
            return fragment
        }
    }
}
