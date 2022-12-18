package app.simple.inure.popups.music

import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.util.SortMusic

class PopupMusicSort(view: View) : BasePopupWindow() {

    private val name: DynamicRippleTextView
    private val date: DynamicRippleTextView
    private val reverse: DynamicRippleTextView
    private val reversedCheckBox: InureCheckBox

    init {
        val contentView = View.inflate(view.context, R.layout.popup_music_sort, PopupLinearLayout(view.context))

        name = contentView.findViewById(R.id.sort_name)
        date = contentView.findViewById(R.id.sort_recently_added)
        reverse = contentView.findViewById(R.id.sort_reversed)
        reversedCheckBox = contentView.findViewById(R.id.sort_reversed_checkbox)

        reversedCheckBox.setChecked(MusicPreferences.getMusicSortReverse())

        name.setOnClickListener {
            setOnClick(SortMusic.NAME)
        }

        date.setOnClickListener {
            setOnClick(SortMusic.DATE)
        }

        reversedCheckBox.setOnCheckedChangeListener { isChecked ->
            MusicPreferences.setMusicSortReverse(isChecked)
        }

        reverse.setOnClickListener {
            reversedCheckBox.toggle()
        }

        when (MusicPreferences.getMusicSort()) {
            SortMusic.NAME -> name.isSelected = true
            SortMusic.DATE -> date.isSelected = true
        }

        init(contentView, view)
    }

    private fun setOnClick(sort: String) {
        MusicPreferences.setMusicSort(sort)
        dismiss()
    }
}