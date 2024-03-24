package app.simple.inure.dialogs.debloat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.Button
import app.simple.inure.dialogs.debloat.DebloatSort.Companion.showDebloatFilter
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.Bloat
import app.simple.inure.preferences.DebloatPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import com.google.android.material.button.MaterialButtonToggleGroup

class DebloatMenu : ScopedBottomSheetFragment() {

    private lateinit var highlighterGroup: MaterialButtonToggleGroup
    private lateinit var recommended: Button
    private lateinit var advanced: Button
    private lateinit var expert: Button
    private lateinit var unsafe: Button
    private lateinit var unlisted: Button
    private lateinit var filter: DynamicRippleImageButton
    private lateinit var openSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_debloat, container, false)

        highlighterGroup = view.findViewById(R.id.highlighter)
        recommended = view.findViewById(R.id.recommended)
        advanced = view.findViewById(R.id.advanced)
        expert = view.findViewById(R.id.expert)
        unsafe = view.findViewById(R.id.unsafe)
        unlisted = view.findViewById(R.id.unlisted)
        filter = view.findViewById(R.id.filter)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtonState()

        if (DevelopmentPreferences.get(DevelopmentPreferences.useCorrespondingColorOnHighlight)) {
            with(Bloat()) {
                recommended.setButtonCheckedColor(recommendedColor)
                advanced.setButtonCheckedColor(advancedColor)
                expert.setButtonCheckedColor(expertColor)
                unsafe.setButtonCheckedColor(unsafeColor)
                unlisted.setButtonCheckedColor(unlistedColor)
            }
        }

        highlighterGroup.addOnButtonCheckedListener { group, _, _ ->
            val checkedButtonIds = group.checkedButtonIds

            DebloatPreferences.setRecommendedHighlight(checkedButtonIds.contains(R.id.recommended))
            DebloatPreferences.setAdvancedHighlight(checkedButtonIds.contains(R.id.advanced))
            DebloatPreferences.setExpertHighlight(checkedButtonIds.contains(R.id.expert))
            DebloatPreferences.setUnsafeHighlight(checkedButtonIds.contains(R.id.unsafe))
            DebloatPreferences.setUnlistedHighlight(checkedButtonIds.contains(R.id.unlisted))
        }

        openSettings.setOnClickListener {
            openSettings()
        }

        filter.setOnClickListener {
            parentFragmentManager.showDebloatFilter().also {
                dismiss()
            }
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
