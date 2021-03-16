package app.simple.inure.ui.app

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.extension.fragments.CoroutineScopedFragment
import app.simple.inure.glide.util.AppIconExtensions.loadAppIcon

class AppInfo : CoroutineScopedFragment() {

    private lateinit var icon: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(requireContext(), requireArguments().getParcelable<ApplicationInfo>("package_name")!!.packageName)
        startPostponedEnterTransition()
    }

    companion object {
        fun newInstance(packageName: ApplicationInfo, transitionName: String): AppInfo {
            val args = Bundle()
            args.putParcelable("package_name", packageName)
            args.putString("transition_name", transitionName)
            val fragment = AppInfo()
            fragment.arguments = args
            return fragment
        }
    }
}