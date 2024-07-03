package app.simple.inure.ui.association

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.MimeConstants
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.LargeString.Companion.showLargeStringDialog
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.popups.viewers.PopupXmlViewer
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.text.EditTextHelper.findMatches
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import com.anggrayudi.storage.file.fullName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException

class Text : KeyboardScopedFragment() {

    private lateinit var text: TypeFaceEditText
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var searchContainer: ThemeLinearLayout
    private lateinit var searchInput: TypeFaceEditText
    private lateinit var previous: DynamicRippleImageButton
    private lateinit var next: DynamicRippleImageButton
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var count: TypeFaceTextView

    private var uri: Uri? = null
    private var matches: ArrayList<Pair<Int, Int>>? = null
    private var position = -1

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
        searchContainer = view.findViewById(R.id.search_container)
        searchInput = view.findViewById(R.id.input)
        previous = view.findViewById(R.id.previous)
        next = view.findViewById(R.id.next)
        clear = view.findViewById(R.id.clear)
        count = view.findViewById(R.id.count)
        search = view.findViewById(R.id.search)
        searchContainer = view.findViewById(R.id.search_container)
        searchInput = view.findViewById(R.id.input)
        previous = view.findViewById(R.id.previous)
        next = view.findViewById(R.id.next)
        clear = view.findViewById(R.id.clear)
        count = view.findViewById(R.id.count)

        uri = if (requireActivity().intent?.action == Intent.ACTION_SEND) {
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
            if (searchContainer.isVisible) {
                searchInput.hideInput()
                searchContainer.gone()
            } else {
                searchInput.showInput()
                searchContainer.visible(false)
            }
        }

        searchInput.doOnTextChanged { text, _, _, _ ->
            matches?.clear()
            matches = this.text.findMatches(text.toString())

            if (matches?.isNotEmpty() == true) position = 0
            jumpToMatch(position)
        }

        next.setOnClickListener {
            if (position < (matches?.size?.minus(1) ?: 0)) {
                jumpToMatch(++position)

            }
        }

        previous.setOnClickListener {
            if (position > 0) {
                jumpToMatch(--position)
            }
        }

        clear.setOnClickListener {
            if (searchInput.text?.isEmpty() == true) {
                searchInput.hideInput()
                searchContainer.gone()
            } else {
                searchInput.text?.clear()
                count.text = "0"
            }
        }
    }

    private fun jumpToMatch(position: Int) {
        matches?.let {
            if (it.isNotEmpty()) {
                if (position in 0 until it.size) {
                    count.text = buildString {
                        append(position.plus(1))
                        append("/")
                        append(it.size)
                    }

                    val layout = this.text.layout
                    scrollView.scrollTo(0, layout.getLineTop(layout.getLineForOffset(it[position].first)))
                    updateTextHighlight()
                }
            } else {
                count.text = "0"
                updateTextHighlight()
            }
        }
    }

    private fun updateTextHighlight() {
        kotlin.runCatching {
            matches?.let {
                val spans: Array<BackgroundColorSpan> = text.text?.getSpans(0, text.text!!.length, BackgroundColorSpan::class.java)!!

                for (span in spans) {
                    text.text?.removeSpan(span)
                }

                for (i in matches?.indices!!) {
                    if (i == position) {
                        text.text?.setSpan(BackgroundColorSpan(Misc.textHighlightFocused),
                                           it[i].first, it[i].second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else {
                        text.text?.setSpan(BackgroundColorSpan(Misc.textHighlightUnfocused),
                                           it[i].first, it[i].second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
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
