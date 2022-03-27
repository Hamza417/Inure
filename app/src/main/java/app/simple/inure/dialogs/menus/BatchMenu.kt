package app.simple.inure.dialogs.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.util.FragmentHelper

class BatchMenu : ScopedBottomSheetFragment() {

    private lateinit var moveSelectionOnTop: SwitchView
    private lateinit var highlightSelected: SwitchView
    private lateinit var openSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_menu, container, false)

        moveSelectionOnTop = view.findViewById(R.id.move_selection_on_top)
        highlightSelected = view.findViewById(R.id.highlight_selected)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moveSelectionOnTop.setChecked(BatchPreferences.isSelectionOnTop())
        highlightSelected.setChecked(BatchPreferences.isSelectedBatchHighlighted())

        moveSelectionOnTop.setOnSwitchCheckedChangeListener {
            BatchPreferences.setMoveSelectionOnTop(it)
        }

        highlightSelected.setOnSwitchCheckedChangeListener {
            BatchPreferences.setHighlightSelectedBatch(it)
        }

        openSettings.setOnClickListener {
            (parentFragment as ScopedFragment).clearExitTransition()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        MainPreferencesScreen.newInstance(),
                                        "prefs_screen")
        }
    }

    companion object {
        fun newInstance(): BatchMenu {
            val args = Bundle()
            val fragment = BatchMenu()
            fragment.arguments = args
            return fragment
        }
    }
}