package app.simple.inure.ui.preferences.subscreens

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterInformationCustomization
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.util.InfoStripUtils.setAppInfo

class InformationCustomization : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var icon: AppIconImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView
    private lateinit var details: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sub_preferences_info_visibility, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        icon = view.findViewById(R.id.app_icon)
        name = view.findViewById(R.id.name)
        packageName = view.findViewById(R.id.package_id)
        details = view.findViewById(R.id.details)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        icon.loadAppIcon(requireContext().packageName, true)
        details.setAppInfo(getInurePackageInfo())
        name.setText(R.string.app_name_full)
        packageName.text = BuildConfig.APPLICATION_ID

        val list = arrayListOf(
                Pair(R.string.version_name, SortConstant.INFO_VERSION),
                Pair(R.string.application_type, SortConstant.INFO_TYPE),
                Pair(R.string.size, SortConstant.INFO_SIZE),
                Pair(R.string.state, SortConstant.INFO_STATE),
                Pair(R.string.apps_category, SortConstant.INFO_CATEGORY),
                Pair(R.string.package_type, SortConstant.INFO_PACKAGE_TYPE),
                Pair(R.string.minimum_sdk, SortConstant.INFO_MIN_SDK),
                Pair(R.string.target_sdk, SortConstant.INFO_TARGET_SDK),
                Pair(R.string.install_date, SortConstant.INFO_INSTALL_DATE),
                Pair(R.string.update_date, SortConstant.INFO_UPDATE_DATE),
        )

        recyclerView.adapter = AdapterInformationCustomization(list)
    }

    private fun getInurePackageInfo(): PackageInfo {
        packageInfo = requirePackageManager().getPackageInfo(requireContext().packageName, 0)
        return packageInfo
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AppsPreferences.INFO_CUSTOM_FILTER -> {
                details.setAppInfo(packageInfo)
            }
        }
    }

    companion object {
        fun newInstance(): InformationCustomization {
            val args = Bundle()
            val fragment = InformationCustomization()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "InformationCustomization"
    }
}
