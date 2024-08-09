package app.simple.inure.ui.association

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.MimeConstants
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.LargeString.Companion.showLargeStringDialog
import app.simple.inure.extensions.fragments.FinderScopedFragment
import app.simple.inure.popups.viewers.PopupXmlViewer
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.visible
import com.anggrayudi.storage.file.fullName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException

class Text : FinderScopedFragment() {

    private lateinit var text: TypeFaceEditText
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var search: DynamicRippleImageButton

    private var uri: Uri? = null

    private val exportText = registerForActivityResult(ActivityResultContracts.CreateDocument(MimeConstants.textType)) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(text.text.toString().toByteArray())
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

        text = view.findViewById(R.id.text_viewer)
        path = view.findViewById(R.id.txt_name)
        options = view.findViewById(R.id.txt_viewer_options)
        scrollView = view.findViewById(R.id.text_viewer_scroll_view)
        search = view.findViewById(R.id.search)

        uri = if (requireActivity().intent.action == Intent.ACTION_SEND) {
            requireActivity().intent.parcelable(Intent.EXTRA_STREAM)
        } else {
            requireActivity().intent.data
        }

        path.text = kotlin.runCatching {
            DocumentFile.fromSingleUri(requireContext(), uri!!)!!.fullName
        }.getOrElse {
            getString(R.string.not_available)
        }

        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun getScrollView(): PaddingAwareNestedScrollView {
        return scrollView
    }

    override fun getEditText(): TypeFaceEditText {
        return text
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        lifecycleScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                withTimeout(3000) {
                    val string = requireActivity().contentResolver.openInputStream(uri!!)!!.use { inputStream ->
                        inputStream.bufferedReader().use {
                            it.readText()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (string.length > FormattingPreferences.getLargeStringLimit()) {
                            requireActivity().supportFragmentManager.showLargeStringDialog(string.length) {
                                text.setText(string)
                                options.visible(true)
                            }
                        } else {
                            text.setText(string)
                            text.visible(animate = true)
                        }
                    }
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    showError(it.stackTraceToString())
                }
            }
        }

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", text.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.export) -> {
                            try {
                                val name = with(path.text.toString()) {
                                    substring(lastIndexOf("/") + 1, length)
                                }

                                val fileName: String = packageInfo.packageName + "_" + name
                                exportText.launch(fileName)
                            } catch (e: UninitializedPropertyAccessException) {
                                val name = with(path.text.toString()) {
                                    substring(lastIndexOf("/") + 1, length)
                                }

                                val fileName: String = name
                                exportText.launch(fileName)
                            }
                        }
                    }
                }
            })
        }

        search.setOnClickListener {
            changeSearchState()
        }
    }

    companion object {
        fun newInstance(): Text {
            val args = Bundle()
            val fragment = Text()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Text"
    }
}
