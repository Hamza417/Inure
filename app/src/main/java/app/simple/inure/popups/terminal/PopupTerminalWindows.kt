package app.simple.inure.popups.terminal

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.adapters.terminal.AdapterWindows
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupTerminalWindows(anchor: View, adapterWindows: AdapterWindows) : BasePopupWindow() {
    private var recyclerView: CustomVerticalRecyclerView

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_terminal_windows, PopupLinearLayout(anchor.context))

        recyclerView = contentView.findViewById(R.id.session_list_recycler_view)
        recyclerView.adapter = adapterWindows

        setContentView(contentView)
        init()
        showAsDropDown(anchor, -width / 8, height / 16, Gravity.NO_GRAVITY)
    }
}