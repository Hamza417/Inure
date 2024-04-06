package app.simple.inure.dialogs.apps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.dialogs.apps.AppsSort.Companion.showAppsSortDialog
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.dialog.AllAppsMenuCallbacks

class AppsMenu : ScopedBottomSheetFragment() {

    private lateinit var generateList: DynamicRippleLinearLayoutWithFactor
    private lateinit var openSettings: DynamicRippleTextView
    private lateinit var filter: DynamicRippleImageButton

    private var onGenerateListClicked: AllAppsMenuCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_all_apps, container, false)

        generateList = view.findViewById(R.id.dialog_export_app_list)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)
        filter = view.findViewById(R.id.filter)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateList.setOnClickListener {
            onGenerateListClicked?.onAllAppsGenerateListClicked().also {
                dismiss()
            }
        }

        openSettings.setOnClickListener {
            openSettings()
        }

        filter.setOnClickListener {
            parentFragmentManager.showAppsSortDialog()
            dismiss()
        }
    }

    fun setOnGenerateListClicked(onGenerateListClicked: AllAppsMenuCallbacks) {
        this.onGenerateListClicked = onGenerateListClicked
    }

    companion object {
        fun newInstance(): AppsMenu {
            return AppsMenu()
        }

        fun FragmentManager.newAppsMenuInstance(): AppsMenu {
            val appsMenu = AppsMenu()
            appsMenu.show(this, TAG)
            return appsMenu
        }

        private const val TAG = "AllAppsMenu"
    }
}
