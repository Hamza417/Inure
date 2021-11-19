package app.simple.inure.popups.viewers

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.adapters.popups.AdapterExtrasFilter
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout

class PopupExtrasFilter(anchor: View) : BasePopupWindow() {

    private var recyclerView: CustomVerticalRecyclerView

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_extras_filter, PopupLinearLayout(anchor.context))

        recyclerView = contentView.findViewById(R.id.extras_filter_recycler_view)
        recyclerView.adapter = AdapterExtrasFilter()

        init(contentView, anchor)
    }
}