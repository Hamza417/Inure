package app.simple.inure.extensions.fragments

import android.os.Bundle
import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.internals.FinderMatchSpan
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class FinderScopedFragment : KeyboardScopedFragment() {

    private var searchJob: Job? = null
    private var rescanJob: Job? = null

    private var query: String = ""

    private var position = -1

    private lateinit var searchContainer: ThemeLinearLayout
    private lateinit var searchInput: TypeFaceEditText
    private lateinit var previous: DynamicRippleImageButton
    private lateinit var next: DynamicRippleImageButton
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var count: TypeFaceTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewIDs(view)

        searchInput.doOnTextChanged { text, _, _, _ ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(300) // Debounce time
                query = text?.toString().orEmpty()
                rescanMatches(keepCurrentIfPossible = true)
            }
        }

        // When the underlying text changes, spans will naturally shift, but we still need to:
        // 1) re-find matches (new/removed occurrences)
        // 2) re-apply focused/unfocused background colors
        getEditText()?.doOnTextChanged { _, _, _, _ ->
            if (query.isBlank()) {
                // If user cleared the query, keep things clean.
                clearFinderSpans(getEditText()?.text)
                position = -1
                updateCounter(emptyList(), position)
                return@doOnTextChanged
            }

            rescanJob?.cancel()
            rescanJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(200)
                rescanMatches(keepCurrentIfPossible = true)
            }
        }

        next.setOnClickListener {
            val spans = getMatchSpansSorted()
            if (position < spans.lastIndex) {
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
                // query will be updated by watcher; but reset UI immediately.
                query = ""
                clearFinderSpans(getEditText()?.text)
                position = -1
                updateCounter(emptyList(), position)
            }
        }
    }

    private fun rescanMatches(keepCurrentIfPossible: Boolean) {
        val editText = getEditText() ?: return
        val editable = editText.text ?: return

        val activeQuery = query
        if (activeQuery.isBlank()) {
            clearFinderSpans(editable)
            position = -1
            updateCounter(emptyList(), position)
            return
        }

        val previousFocusedStart = if (keepCurrentIfPossible) {
            getMatchSpansSorted().getOrNull(position)?.let { span ->
                editable.getSpanStart(span).takeIf { it >= 0 }
            }
        } else {
            null
        }

        clearFinderSpans(editable)

        val lower = activeQuery.lowercase()
        val textLength = editable.length
        val keywordLength = lower.length

        var index = 0
        while (index <= textLength - keywordLength) {
            val substring = editable.subSequence(index, index + keywordLength).toString()
            if (substring.equals(lower, ignoreCase = true)) {
                // Marker span that will shift automatically as user edits.
                editable.setSpan(FinderMatchSpan(), index, index + keywordLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            index++
        }

        val spans = getMatchSpansSorted()

        position = when {
            spans.isEmpty() -> -1
            previousFocusedStart == null -> 0
            else -> spans.indexOfFirst { editable.getSpanStart(it) == previousFocusedStart }.takeIf { it >= 0 } ?: 0
        }

        updateTextHighlight(spans)
        updateCounter(spans, position)
        jumpToMatch(position)
    }

    private fun getMatchSpansSorted(): List<FinderMatchSpan> {
        val editable = getEditText()?.text ?: return emptyList()
        val spans = editable.getSpans(0, editable.length, FinderMatchSpan::class.java).toList()
        return spans.sortedBy { editable.getSpanStart(it) }
    }

    private fun clearFinderSpans(editable: Editable?) {
        if (editable == null) return

        // Remove only FinderMatchSpan and their associated background spans.
        val matchSpans = editable.getSpans(0, editable.length, FinderMatchSpan::class.java)
        matchSpans.forEach { editable.removeSpan(it) }

        val bg = editable.getSpans(0, editable.length, BackgroundColorSpan::class.java)
        bg.forEach {
            val start = editable.getSpanStart(it)
            val end = editable.getSpanEnd(it)
            // Our highlights are always exactly on a match range.
            if (start >= 0 && end > start) {
                // Remove only if there isn't any non-finder reason to keep it.
                // Heuristic: remove spans that use our configured highlight colors.
                if (it.backgroundColor == Misc.textHighlightFocused || it.backgroundColor == Misc.textHighlightUnfocused) {
                    editable.removeSpan(it)
                }
            }
        }
    }

    private fun jumpToMatch(position: Int) {
        val editText = getEditText() ?: return
        val editable = editText.text ?: return
        val spans = getMatchSpansSorted()

        if (spans.isEmpty() || position !in spans.indices) {
            updateCounter(emptyList(), -1)
            updateTextHighlight(emptyList())
            return
        }

        updateCounter(spans, position)

        val span = spans[position]
        val start = editable.getSpanStart(span)
        if (start < 0) return

        val layout: Layout? = editText.layout
        layout?.let {
            val line = it.getLineForOffset(start)
            val y = it.getLineTop(line)
            getScrollView()?.smoothScrollTo(0, y)
        }

        updateTextHighlight(spans)
    }

    private fun updateCounter(spans: List<FinderMatchSpan>, position: Int) {
        count.text = buildString {
            if (spans.isNotEmpty() && position in spans.indices) {
                append(position + 1)
                append("/")
                append(spans.size)
            } else {
                append("0")
                append("/")
                append("0")
            }
        }
    }

    private fun updateTextHighlight(spans: List<FinderMatchSpan> = getMatchSpansSorted()) {
        val editText = getEditText() ?: return
        val editable = editText.text ?: return

        kotlin.runCatching {
            // Remove only our background spans (keep other formatting highlights intact).
            val existing: Array<BackgroundColorSpan> = editable.getSpans(0, editable.length, BackgroundColorSpan::class.java)
            for (span in existing) {
                if (span.backgroundColor == Misc.textHighlightFocused || span.backgroundColor == Misc.textHighlightUnfocused) {
                    editable.removeSpan(span)
                }
            }

            spans.forEachIndexed { index, matchSpan ->
                val start = editable.getSpanStart(matchSpan)
                val end = editable.getSpanEnd(matchSpan)
                if (start >= 0 && end > start) {
                    val color = if (index == position) Misc.textHighlightFocused else Misc.textHighlightUnfocused
                    editable.setSpan(BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    protected fun changeSearchState() {
        if (searchContainer.isVisible) {
            searchInput.hideInput()
            searchContainer.gone()
        } else {
            searchInput.showInput()
            searchContainer.visible(false)
        }
    }

    protected open fun getScrollView(): PaddingAwareNestedScrollView? {
        return null
    }

    protected open fun getEditText(): TypeFaceEditText? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        searchJob?.cancel()
        rescanJob?.cancel()
    }

    private fun setViewIDs(view: View) {
        searchContainer = view.findViewById(R.id.search_container)
        searchInput = view.findViewById(R.id.input)
        previous = view.findViewById(R.id.previous)
        next = view.findViewById(R.id.next)
        clear = view.findViewById(R.id.clear)
        count = view.findViewById(R.id.count)
    }
}
