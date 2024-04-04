package app.simple.inure.dialogs.batch

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.database.instances.BatchProfileDatabase
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.batch.BatchSort.Companion.showBatchSort
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.BatchPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatchMenu : ScopedBottomSheetFragment() {

    private lateinit var moveSelectionOnTop: Switch
    private lateinit var highlightSelected: Switch
    private lateinit var loadSelectionProfile: DynamicRippleLinearLayout
    private lateinit var saveSelectionProfile: DynamicRippleTextView
    private lateinit var createSelectionFromTags: DynamicRippleTextView
    private lateinit var currentProfile: TypeFaceTextView
    private lateinit var openSettings: DynamicRippleTextView
    private lateinit var filter: DynamicRippleImageButton

    private lateinit var batchMenuListener: BatchMenuListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_batch, container, false)

        moveSelectionOnTop = view.findViewById(R.id.move_selection_on_top)
        highlightSelected = view.findViewById(R.id.highlight_selected)
        loadSelectionProfile = view.findViewById(R.id.load_selection_profile)
        saveSelectionProfile = view.findViewById(R.id.save_selection_profile)
        createSelectionFromTags = view.findViewById(R.id.create_selection_from_tags)
        currentProfile = view.findViewById(R.id.current_profile)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)
        filter = view.findViewById(R.id.filter)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moveSelectionOnTop.isChecked = BatchPreferences.isSelectionOnTop()
        highlightSelected.isChecked = BatchPreferences.isSelectedBatchHighlighted()
        setCurrentProfileName()

        moveSelectionOnTop.setOnSwitchCheckedChangeListener {
            BatchPreferences.setMoveSelectionOnTop(it)
        }

        highlightSelected.setOnSwitchCheckedChangeListener {
            BatchPreferences.setHighlightSelectedBatch(it)
        }

        loadSelectionProfile.setOnClickListener {
            batchMenuListener.onLoadProfile().also {
                dismiss()
            }
        }

        saveSelectionProfile.setOnClickListener {
            batchMenuListener.onSaveProfile()
            dismiss()
        }

        createSelectionFromTags.setOnClickListener {
            batchMenuListener.onTagPicker().also {
                dismiss()
            }
        }

        openSettings.setOnClickListener {
            openSettings()
        }

        filter.setOnClickListener {
            parentFragmentManager.showBatchSort()
            dismiss()
        }
    }

    private fun setCurrentProfileName() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            BatchProfileDatabase.getInstance(requireContext())?.let {
                it.batchProfileDao()?.getBatchProfileName(BatchPreferences.getLastSelectedProfile())?.let {
                    withContext(Dispatchers.Main) {
                        this@BatchMenu.currentProfile.text = it
                    }
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            BatchPreferences.LAST_SELECTED_PROFILE -> {
                setCurrentProfileName()
            }
        }
    }

    fun setBatchMenuListener(batchMenuListener: BatchMenuListener) {
        this.batchMenuListener = batchMenuListener
    }

    companion object {
        fun newInstance(): BatchMenu {
            val args = Bundle()
            val fragment = BatchMenu()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchMenu(): BatchMenu {
            val dialog = newInstance()
            dialog.show(this, "batch_menu")
            return dialog
        }

        interface BatchMenuListener {
            fun onSaveProfile()
            fun onLoadProfile()
            fun onTagPicker()
        }
    }
}
