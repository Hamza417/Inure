package app.simple.inure.ui.app

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.AppIconExtensions.loadAppIcon
import app.simple.inure.packagehelper.PackageUtils

class AppInfo : ScopedFragment() {

    private lateinit var icon: ImageView

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView

    private lateinit var applicationInfo: ApplicationInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(requireContext(), applicationInfo.packageName)
        startPostponedEnterTransition()

        name.text = applicationInfo.name
        packageId.text = PackageUtils.getApplicationVersion(requireContext(), applicationInfo)
    }

    override fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        /* no-op */
    }

    companion object {
        fun newInstance(packageName: ApplicationInfo, transitionName: String): AppInfo {
            val args = Bundle()
            args.putParcelable("application_info", packageName)
            args.putString("transition_name", transitionName)
            val fragment = AppInfo()
            fragment.arguments = args
            return fragment
        }
    }
}
