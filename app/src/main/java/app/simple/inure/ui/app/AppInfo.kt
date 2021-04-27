package app.simple.inure.ui.app

import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
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
import app.simple.inure.util.PackageUtils
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.ui.viewers.*
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.FragmentHelper.openFragment
import app.simple.inure.viewmodels.AppInfoMenuData
import app.simple.inure.viewmodels.AppSize

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
    private val options: AppInfoMenuData by viewModels()

    private var spanCount = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        appInformation = view.findViewById(R.id.app_info_information_tv)
        storage = view.findViewById(R.id.app_info_storage_tv)
        menu = view.findViewById(R.id.app_info_menu)
        pie = view.findViewById(R.id.pie)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        options.getMenuOptions().observe(requireActivity(), {
            postponeEnterTransition()

            spanCount = if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                3
            } else {
                6
            }

            adapterAppInfoMenu = AdapterAppInfoMenu(it)
            adapterAppInfoMenu.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            menu.layoutManager = GridLayoutManager(requireContext(), spanCount, GridLayoutManager.VERTICAL, false)
            menu.adapter = adapterAppInfoMenu
            menu.scheduleLayoutAnimation()

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapterAppInfoMenu.setOnAppInfoMenuCallback(object : AdapterAppInfoMenu.AppInfoMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.manifest) -> {
                            if (ConfigurationPreferences.isXmlViewerTextView()) {
                                openFragment(requireActivity().supportFragmentManager,
                                             XMLViewerTextView.newInstance(applicationInfo, true, null),
                                             icon, "manifest")
                            } else {
                                openFragment(requireActivity().supportFragmentManager,
                                             XMLViewerWebView.newInstance(applicationInfo, true, null),
                                             icon, "manifest")
                            }
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
                        getString(R.string.permissions) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Permissions.newInstance(applicationInfo),
                                         icon, "permissions")
                        }
                        getString(R.string.certificate) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Certificate.newInstance(applicationInfo),
                                         icon, "certificate")
                        }
                        getString(R.string.broadcasts) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Broadcasts.newInstance(applicationInfo),
                                         icon, "broadcasts")
                        }
                        getString(R.string.resources) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Resources.newInstance(applicationInfo),
                                         icon, "resources")
                        }
                        getString(R.string.uses_feature) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Features.newInstance(applicationInfo),
                                         icon, "uses_feature")
                        }
                    }
                }
            })
        })

        model.getTotalAppSize().observe(requireActivity(), {
            val x = applicationInfo.sourceDir.getDirectoryLength().toDouble() / it.toDouble() * 100.0
            pie.value = (x * (360.0 / 100.0)).toFloat()
        })

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(requireContext(), applicationInfo.packageName)

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
