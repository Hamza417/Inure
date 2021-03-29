package app.simple.inure.dialogs

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.animatedbackground.AnimatedBackgroundLinearLayout
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.fragments.ScopedBottomSheetFragment
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.popups.dialogs.SortingStylePopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort

class AppsListConfiguration : ScopedBottomSheetFragment() {

    private lateinit var appsCategoryContainer: AnimatedBackgroundLinearLayout
    private lateinit var sortingStyleContainer: AnimatedBackgroundLinearLayout

    private lateinit var appsCategory: TypeFaceTextView
    private lateinit var sortingStyle: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_main_settings, container, false)

        appsCategory = view.findViewById(R.id.dialog_apps_category)
        sortingStyle = view.findViewById(R.id.dialog_apps_sorting)

        appsCategoryContainer = view.findViewById(R.id.dialog_apps_category_container)
        sortingStyleContainer = view.findViewById(R.id.dialog_apps_sorting_container)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sortingStyle.text = when (MainPreferences.getSortStyle()) {
            Sort.NAME -> getString(R.string.name)
            Sort.INSTALL_DATE -> getString(R.string.install_date)
            Sort.SIZE -> getString(R.string.app_size)
            Sort.PACKAGE_NAME -> getString(R.string.package_name)
            else -> getString(R.string.unknown)
        }

        sortingStyleContainer.setOnClickListener {
            val popup = SortingStylePopup(
                layoutInflater.inflate(R.layout.popup_sorting_style, DynamicCornerLinearLayout(requireContext(), null), true),
                sortingStyle)

            (sortingStyle.compoundDrawables[2] as AnimatedVectorDrawable).start()

            popup.setOnMenuItemClickListener(object : PopupMenuCallback {
                override fun onMenuItemClicked(source: String) {
                    MainPreferences.setSortStyle(source)
                }

                override fun onDismiss() {
                    (sortingStyle.compoundDrawables[2] as AnimatedVectorDrawable).reset()
                }
            })
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(): AppsListConfiguration {
            val args = Bundle()
            val fragment = AppsListConfiguration()
            fragment.arguments = args
            return fragment
        }
    }
}
