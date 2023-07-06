package app.simple.inure.popups.notes

import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import app.simple.inure.R
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.util.ViewUtils.onProgressChanged

class PopupTextSize(view: View, size: Int) : BasePopupWindow() {

    private var seekBar: ThemeSeekBar
    var onSizeChanged: ((Int) -> Unit)? = null

    init {
        val root = PopupLinearLayout(view.context, LinearLayout.HORIZONTAL)
        root.clipToPadding = false
        root.clipChildren = false

        val contentView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_text_size, root, true)

        seekBar = contentView.findViewById(R.id.seekbar)

        seekBar.max = 100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.min = 10
        }
        seekBar.progress = size

        seekBar.onProgressChanged {
            if (it < 10) {
                onSizeChanged?.invoke(10)
            } else {
                onSizeChanged?.invoke(it)
            }
        }

        init(contentView, view, Gravity.CENTER)
    }
}