package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
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
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.MimeConstants
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.MarkedView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.TextViewViewModelFactory
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.viewmodels.viewers.TextViewerViewModel
import java.io.IOException

class Markdown : ScopedFragment() {

    private lateinit var codeView: MarkedView
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton

    private lateinit var textViewerViewModel: TextViewerViewModel
    private lateinit var textViewViewModelFactory: TextViewViewModelFactory

    private var code = ""

    private var backPress: OnBackPressedDispatcher? = null

    private val exportText = registerForActivityResult(ActivityResultContracts.CreateDocument(MimeConstants.markdownType)) { uri: Uri? ->
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

        textViewViewModelFactory = TextViewViewModelFactory(
                packageInfo,
                requireArguments().getString("path")!!,
        )

        backPress = requireActivity().onBackPressedDispatcher
        textViewerViewModel = ViewModelProvider(this, textViewViewModelFactory)[TextViewerViewModel::class.java]

        path.text = requireArguments().getString("path")!!

        FastScrollerBuilder(view.findViewById(R.id.markdown_viewer_scroll_view)).build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        textViewerViewModel.getText().observe(viewLifecycleOwner) {
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
        }

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
                            val fileName: String = packageInfo.packageName + "_" + path.text
                            exportText.launch(fileName)
                        }
                    }
                }
            })
        }

        backPress?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (codeView.canGoBack()) {
                    codeView.goBack()
                    codeView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                } else {
                    remove()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        codeView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo, path: String): Markdown {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            args.putString("path", path)
            val fragment = Markdown()
            fragment.arguments = args
            return fragment
        }
    }
}