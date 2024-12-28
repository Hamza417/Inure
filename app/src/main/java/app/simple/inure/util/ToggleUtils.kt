package app.simple.inure.util

import app.simple.inure.decorations.views.Chip
import com.google.android.material.chip.ChipGroup

object ToggleUtils {

    fun ChipGroup.uncheck(id: Int) {
        this.findViewById<Chip>(id).isChecked = false
    }
}