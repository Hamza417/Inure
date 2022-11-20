package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.MimeConstants
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.exceptions.LargeStringException
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.TextViewViewModelFactory
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.viewmodels.viewers.TextViewerViewModel
import java.io.IOException

class Text : KeyboardScopedFragment() {

    private lateinit var txt: TypeFaceEditText
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var scrollView: PaddingAwareNestedScrollView

    private lateinit var textViewerViewModel: TextViewerViewModel
    private lateinit var textViewViewModelFactory: TextViewViewModelFactory

    private val exportText = registerForActivityResult(ActivityResultContracts.CreateDocument(MimeConstants.textType)) { uri: Uri? ->
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
        scrollView = view.findViewById(R.id.text_viewer_scroll_view)

        textViewViewModelFactory = TextViewViewModelFactory(packageInfo,
                                                            requireArguments().getString(BundleConstants.path)!!)
        textViewerViewModel = ViewModelProvider(this, textViewViewModelFactory)[TextViewerViewModel::class.java]

        path.text = requireArguments().getString(BundleConstants.path)!!

        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        textViewerViewModel.getText().observe(viewLifecycleOwner) {
            runCatching {
                if (it.length >= 150000 && !FormattingPreferences.isLoadingLargeStrings()) {
                    throw LargeStringException("String size ${it.length} is too big to render without freezing the app")
                }
                txt.setText(it)
            }.getOrElse {
                txt.setText(it.stackTraceToString())
                txt.setTextColor(Color.RED)
            }
        }

        options.setOnClickListener {
            val p = PopupXmlViewer(it)

            p.setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", txt.text.toString())
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
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, path: String, isRaw: Boolean = false): Text {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.path, path)
            args.putBoolean(BundleConstants.isRaw, isRaw)
            val fragment = Text()
            fragment.arguments = args
            return fragment
        }
    }
}
