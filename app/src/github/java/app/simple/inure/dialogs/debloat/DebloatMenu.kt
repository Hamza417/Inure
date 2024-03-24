package app.simple.inure.dialogs.debloat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.DebloatPreferences
import com.google.android.material.button.MaterialButtonToggleGroup

class DebloatMenu : ScopedBottomSheetFragment() {

    private lateinit var highlighterGroup: MaterialButtonToggleGroup
    private lateinit var openSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_debloat, container, false)

        highlighterGroup = view.findViewById(R.id.highlighter)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtonState()

        highlighterGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            val checkedButtonIds = group.checkedButtonIds

            DebloatPreferences.setRecommendedHighlight(checkedButtonIds.contains(R.id.recommended))
            DebloatPreferences.setAdvancedHighlight(checkedButtonIds.contains(R.id.advanced))
            DebloatPreferences.setExpertHighlight(checkedButtonIds.contains(R.id.expert))
            DebloatPreferences.setUnsafeHighlight(checkedButtonIds.contains(R.id.unsafe))
            DebloatPreferences.setUnlistedHighlight(checkedButtonIds.contains(R.id.unlisted))
        }

        openSettings.setOnClickListener {
            // Open settings
        }
    }

    private fun setButtonState() {
        if (DebloatPreferences.getRecommendedHighlight()) {
            highlighterGroup.check(R.id.recommended)
        }

        if (DebloatPreferences.getAdvancedHighlight()) {
            highlighterGroup.check(R.id.advanced)
        }

        if (DebloatPreferences.getExpertHighlight()) {
            highlighterGroup.check(R.id.expert)
        }

        if (DebloatPreferences.getUnsafeHighlight()) {
            highlighterGroup.check(R.id.unsafe)
        }

        if (DebloatPreferences.getUnlistedHighlight()) {
            highlighterGroup.check(R.id.unlisted)
        }
    }

    companion object {
        fun newInstance(): DebloatMenu {
            val args = Bundle()
            val fragment = DebloatMenu()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showDebloatMenu() {
            DebloatMenu().show(this, "debloat_menu")
        }
    }
}
