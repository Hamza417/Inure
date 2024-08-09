package app.simple.inure.extensions.fragments

import android.os.Bundle
import android.text.Spannable
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
import app.simple.inure.text.EditTextHelper.findMatches
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class FinderScopedFragment : KeyboardScopedFragment() {

    private var matches: ArrayList<Pair<Int, Int>>? = null
    private var searchJob: Job? = null

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
                matches?.clear()
                matches = this@FinderScopedFragment.getEditText()?.findMatches(text.toString())
                position = if (matches?.isNotEmpty() == true) 0 else -1
                updateTextHighlight()
                jumpToMatch(position)
            }
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
                count.text = buildString {
                    append("0")
                    append("/")
                    append("0")
                }
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

                    val layout = this.getEditText()?.layout
                    layout?.let { it1 ->
                        getScrollView()?.smoothScrollTo(0, it1.getLineTop(layout.getLineForOffset(it[position].first)))
                    }

                    updateTextHighlight()
                }
            } else {
                count.text = buildString {
                    append("0")
                    append("/")
                    append("0")
                }

                updateTextHighlight()
            }
        }
    }

    private fun updateTextHighlight() {
        kotlin.runCatching {
            matches?.let {
                val text = getEditText()!!
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
