package app.simple.inure.ui.app

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterAppInfoMenu
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.Pie
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.app.Information
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.AppIconExtensions.loadAppIcon
import app.simple.inure.packagehelper.PackageUtils
import app.simple.inure.ui.viewers.Activities
import app.simple.inure.ui.viewers.Providers
import app.simple.inure.ui.viewers.Services
import app.simple.inure.ui.viewers.XMLViewerWebView
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.FragmentHelper.openFragment
import app.simple.inure.viewmodels.AppSize
import java.util.jar.Manifest


class AppInfo : ScopedFragment() {

    private lateinit var icon: ImageView

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var appInformation: DynamicRippleTextView
    private lateinit var storage: DynamicRippleTextView
    private lateinit var menu: RecyclerView
    private lateinit var pie: Pie

    private lateinit var applicationInfo: ApplicationInfo
    private lateinit var adapterAppInfoMenu: AdapterAppInfoMenu
    private val model: AppSize by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        appInformation = view.findViewById(R.id.app_info_information_tv)
        storage = view.findViewById(R.id.app_info_storage_tv)
        menu = view.findViewById(R.id.app_info_menu)
        pie = view.findViewById(R.id.pie)

        val list = listOf(
            Pair(R.drawable.ic_permission, requireContext().getString(R.string.permissions)),
            Pair(R.drawable.ic_activities, requireContext().getString(R.string.activities)),
            Pair(R.drawable.ic_services, requireContext().getString(R.string.services)),
            Pair(R.drawable.ic_certificate, requireContext().getString(R.string.certificate)),
            Pair(R.drawable.ic_resources, requireContext().getString(R.string.resources)),
            Pair(R.drawable.ic_broadcast, requireContext().getString(R.string.broadcasts)),
            Pair(R.drawable.ic_provider, requireContext().getString(R.string.providers)),
            Pair(R.drawable.ic_xml, requireContext().getString(R.string.manifest))
        )

        applicationInfo = requireArguments().getParcelable("application_info")!!
        adapterAppInfoMenu = AdapterAppInfoMenu(list)
        menu.layoutManager = GridLayoutManager(requireContext(), 4, GridLayoutManager.VERTICAL, false)
        menu.adapter = adapterAppInfoMenu

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.getTotalAppSize().observe(requireActivity(), {
            val x = applicationInfo.sourceDir.getDirectoryLength().toDouble() / it.toDouble() * 100.0
            pie.value = (x * (360.0 / 100.0)).toFloat()
        })

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(requireContext(), applicationInfo.packageName)
        startPostponedEnterTransition()

        name.text = applicationInfo.name
        packageId.text = PackageUtils.getApplicationVersion(requireContext(), applicationInfo)

        appInformation.setOnClickListener {
            Information.newInstance(applicationInfo)
                    .show(childFragmentManager, "information")
        }

        storage.setOnClickListener {
            Storage.newInstance(applicationInfo)
                    .show(childFragmentManager, "storage")
        }

        adapterAppInfoMenu.setOnAppInfoMenuCallback(object : AdapterAppInfoMenu.AppInfoMenuCallbacks {
            override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                when (source) {
                    getString(R.string.manifest) -> {
                        openFragment(requireActivity().supportFragmentManager,
                                     XMLViewerWebView.newInstance(applicationInfo),
                                     icon, "services")
                    }
                    getString(R.string.services) -> {
                        openFragment(requireActivity().supportFragmentManager,
                                     Services.newInstance(applicationInfo),
                                     icon, "services")
                    }
                    getString(R.string.activities) -> {
                        openFragment(requireActivity().supportFragmentManager,
                                     Activities.newInstance(applicationInfo),
                                     icon, "activities")
                    }
                    getString(R.string.providers) -> {
                        openFragment(requireActivity().supportFragmentManager,
                                     Providers.newInstance(applicationInfo),
                                     icon, "providers")
                    }
                }
            }
        })
    }

    override fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        /* no-op */
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo, transitionName: String): AppInfo {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            args.putString("transition_name", transitionName)
            val fragment = AppInfo()
            fragment.arguments = args
            return fragment
        }
    }
}
