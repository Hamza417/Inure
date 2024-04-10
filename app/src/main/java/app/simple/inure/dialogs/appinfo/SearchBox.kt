package app.simple.inure.dialogs.appinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.appinfo.SearchBoxCallbacks

class SearchBox : ScopedBottomSheetFragment() {

    private lateinit var editText: DynamicCornerEditText
    private lateinit var save: DynamicRippleTextView

    private var searchBoxCallbacks: SearchBoxCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_search_box, container, false)

        editText = view.findViewById(R.id.edit_text)
        save = view.findViewById(R.id.search)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        save.setOnClickListener {
            searchBoxCallbacks?.onSearch(editText.text.toString())
            dismiss()
        }

        editText.showInput()
    }

    fun setSearchBoxCallbacks(searchBoxCallbacks: SearchBoxCallbacks) {
        this.searchBoxCallbacks = searchBoxCallbacks
    }

    companion object {
        fun newInstance(): SearchBox {
            val args = Bundle()
            val fragment = SearchBox()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showSearchBox(searchBoxCallbacks: SearchBoxCallbacks) {
            val searchBox = newInstance()
            searchBox.setSearchBoxCallbacks(searchBoxCallbacks)
            searchBox.show(this, "search_box")
        }
    }
}
