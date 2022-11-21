package app.simple.inure.popups.app

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import app.simple.inure.R
import app.simple.inure.constants.GridConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.SharedPreferences

class PopupGridSelection(view: View, key: String) : BasePopupWindow() {

    private val grid1: DynamicRippleTextView
    private val grid2: DynamicRippleTextView
    private val grid3: DynamicRippleTextView
    private val grid4: DynamicRippleTextView

    init {
        val contentView = View.inflate(
                view.context, R.layout.popup_grid_selection,
                PopupLinearLayout(view.context, LinearLayout.HORIZONTAL))

        grid1 = contentView.findViewById(R.id.popup_1_1)
        grid2 = contentView.findViewById(R.id.popup_2_2)
        grid3 = contentView.findViewById(R.id.popup_3_3)
        grid4 = contentView.findViewById(R.id.popup_4_4)

        grid1.setOnClickListener {
            SharedPreferences.getSharedPreferences().edit().putInt(key, GridConstants.grid1).apply()
            dismiss()
        }

        grid2.setOnClickListener {
            SharedPreferences.getSharedPreferences().edit().putInt(key, GridConstants.grid2).apply()
            dismiss()
        }

        grid3.setOnClickListener {
            SharedPreferences.getSharedPreferences().edit().putInt(key, GridConstants.grid3).apply()
            dismiss()
        }

        grid4.setOnClickListener {
            SharedPreferences.getSharedPreferences().edit().putInt(key, GridConstants.grid4).apply()
            dismiss()
        }

        when (SharedPreferences.getSharedPreferences().getInt(key, GridConstants.grid1)) {
            GridConstants.grid1 -> {
                grid1.isSelected = true
            }
            GridConstants.grid2 -> {
                grid2.isSelected = true
            }
            GridConstants.grid3 -> {
                grid3.isSelected = true
            }
            GridConstants.grid4 -> {
                grid4.isSelected = true
            }
        }

        init(contentView, view)
    }
}