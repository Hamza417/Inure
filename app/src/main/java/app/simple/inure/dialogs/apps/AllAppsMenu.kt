package app.simple.inure.dialogs.apps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.GridTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.dialog.AllAppsMenuCallbacks
import app.simple.inure.popups.app.PopupGridSelection
import app.simple.inure.preferences.MainPreferences

class AllAppsMenu : ScopedBottomSheetFragment() {

    private lateinit var grid: GridTextView
    private lateinit var generateList: DynamicRippleLinearLayoutWithFactor
    private lateinit var openSettings: DynamicRippleTextView

    private var onGenerateListClicked: AllAppsMenuCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_all_apps_menu, container, false)

        grid = view.findViewById(R.id.popup_grid)
        generateList = view.findViewById(R.id.dialog_export_app_list)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        grid.setGridText(MainPreferences.getGridType())

        grid.setOnClickListener {
            PopupGridSelection(it, MainPreferences.gridType)
        }

        generateList.setOnClickListener {
            onGenerateListClicked?.onAllAppsGenerateListClicked().also {
                dismiss()
            }
        }

        openSettings.setOnClickListener {
            openSettings()
        }
    }

    fun setOnGenerateListClicked(onGenerateListClicked: AllAppsMenuCallbacks) {
        this.onGenerateListClicked = onGenerateListClicked
    }

    companion object {
        fun newInstance(): AllAppsMenu {
            return AllAppsMenu()
        }

        fun FragmentManager.newAppsMenuInstance(): AllAppsMenu {
            val allAppsMenu = AllAppsMenu()
            allAppsMenu.show(this, TAG)
            return allAppsMenu
        }

        private const val TAG = "AllAppsMenu"
    }
}