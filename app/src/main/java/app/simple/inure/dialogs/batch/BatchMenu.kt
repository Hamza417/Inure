package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.dialogs.batch.BatchSort.Companion.showBatchSort
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.BatchPreferences

class BatchMenu : ScopedBottomSheetFragment() {

    private lateinit var moveSelectionOnTop: SwitchView
    private lateinit var highlightSelected: SwitchView
    private lateinit var loadSelectionProfile: DynamicRippleLinearLayout
    private lateinit var saveSelectionProfile: DynamicRippleTextView
    private lateinit var openSettings: DynamicRippleTextView
    private lateinit var filter: DynamicRippleImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_batch, container, false)

        moveSelectionOnTop = view.findViewById(R.id.move_selection_on_top)
        highlightSelected = view.findViewById(R.id.highlight_selected)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)
        filter = view.findViewById(R.id.filter)

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
            openSettings()
        }

        filter.setOnClickListener {
            parentFragmentManager.showBatchSort()
            dismiss()
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