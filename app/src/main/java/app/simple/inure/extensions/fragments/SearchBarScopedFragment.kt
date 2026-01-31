package app.simple.inure.extensions.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

open class SearchBarScopedFragment : KeyboardScopedFragment() {

    open lateinit var search: DynamicRippleImageButton
    open lateinit var title: LinearLayout
    open lateinit var count: TypeFaceTextView
    open lateinit var searchBox: DynamicCornerEditText

    private var keywords = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keywords = requireArguments().getString(BundleConstants.KEYWORDS, "") ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kotlin.runCatching {
            searchBox.setWindowInsetsAnimationCallback()
        }

        count = view.findViewById(R.id.count)
    }

    protected fun searchBoxState(animate: Boolean, isVisible: Boolean) {
        if (isVisible || keywords.isNotEmpty()) {
            search.setImageResource(R.drawable.ic_close)
            title.gone()
            searchBox.visible(animate)
            searchBox.showInput()

            if (keywords.isNotEmpty()) {
                searchBox.setText(keywords)
            }
        } else {
            search.setImageResource(R.drawable.ic_search)
            title.visible(animate)
            searchBox.gone()
            searchBox.hideInput()
        }
    }

    protected fun setCount(count: Int) {
        this.count.text = getString(R.string.total, count)
    }

    protected fun setCount(count: String) {
        this.count.text = count
    }
}
