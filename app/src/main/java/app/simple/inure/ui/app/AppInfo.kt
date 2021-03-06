package app.simple.inure.ui.app

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.menus.AdapterMenu
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.launchThisPackage
import app.simple.inure.apk.utils.PackageUtils.uninstallThisPackage
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.dialogs.miscellaneous.Preparing
import app.simple.inure.dialogs.miscellaneous.ShellExecutorDialog
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.popups.app.PopupSure
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.ui.viewers.*
import app.simple.inure.util.FragmentHelper.openFragment
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.panels.AllAppsData
import app.simple.inure.viewmodels.panels.InfoPanelMenuData


class AppInfo : ScopedFragment() {

    private lateinit var icon: ImageView

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var appInformation: DynamicRippleTextView
    private lateinit var storage: DynamicRippleTextView
    private lateinit var directories: DynamicRippleTextView
    private lateinit var menu: RecyclerView
    private lateinit var options: RecyclerView

    private lateinit var adapterMenu: AdapterMenu
    private lateinit var componentsViewModel: InfoPanelMenuData
    private lateinit var applicationInfoFactory: ApplicationInfoFactory
    private lateinit var allAppsData: AllAppsData

    private var spanCount = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        appInformation = view.findViewById(R.id.app_info_information_tv)
        storage = view.findViewById(R.id.app_info_storage_tv)
        directories = view.findViewById(R.id.app_info_directories_tv)
        menu = view.findViewById(R.id.app_info_menu)
        options = view.findViewById(R.id.app_info_options)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        spanCount = if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            3
        } else {
            6
        }

        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(InfoPanelMenuData::class.java)
        allAppsData = ViewModelProvider(requireActivity()).get(AllAppsData::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getMenuItems().observe(viewLifecycleOwner, {
            postponeEnterTransition()

            adapterMenu = AdapterMenu(it)
            adapterMenu.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            menu.layoutManager = GridLayoutManager(requireContext(), spanCount)
            //menu.addItemDecoration(GridSpacingItemDecoration(spanCount, resources.getDimensionPixelSize(R.dimen.dialog_padding), true, 0))
            menu.adapter = adapterMenu
            menu.scheduleLayoutAnimation()

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapterMenu.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.manifest) -> {
                            if (ConfigurationPreferences.isXmlViewerTextView()) {
                                openFragment(requireActivity().supportFragmentManager,
                                             XMLViewerTextView.newInstance(applicationInfo, true, "AndroidManifest.xml"),
                                             icon, "manifest")
                            } else {
                                openFragment(requireActivity().supportFragmentManager,
                                             XMLViewerWebView.newInstance(applicationInfo, true, "AndroidManifest.xml"),
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
                        getString(R.string.receivers) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Receivers.newInstance(applicationInfo),
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
                        getString(R.string.graphics) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Graphics.newInstance(applicationInfo),
                                         icon, "graphics")
                        }
                        getString(R.string.extras) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Extras.newInstance(applicationInfo),
                                         icon, "extras")
                        }
                        getString(R.string.dex_classes) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Dexs.newInstance(applicationInfo),
                                         icon, "dexs")
                        }
                    }
                }
            })
        })

        componentsViewModel.getMenuOptions().observe(requireActivity(), {
            val adapterAppInfoMenu = AdapterMenu(it)
            options.layoutManager = GridLayoutManager(requireContext(), spanCount, GridLayoutManager.VERTICAL, false)
            options.adapter = adapterAppInfoMenu
            options.scheduleLayoutAnimation()

            adapterAppInfoMenu.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.launch) -> {
                            applicationInfo.launchThisPackage(requireActivity())
                        }
                        getString(R.string.uninstall) -> {
                            if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
                                val popupMenu = PopupSure(layoutInflater.inflate(R.layout.popup_sure, PopupLinearLayout(requireContext()), true), icon)
                                popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                    override fun onMenuItemClicked(source: String) {
                                        when (source) {
                                            getString(R.string.yes) -> {
                                                val f = ShellExecutorDialog.newInstance("pm uninstall -k --user 0 ${applicationInfo.packageName}")

                                                f.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                                    override fun onCommandExecuted(result: String) {
                                                        if (result == "Success") {
                                                            onAppUninstalled(true)
                                                        }
                                                    }
                                                })

                                                f.show(parentFragmentManager, "shell_executor")
                                            }
                                        }
                                    }
                                })
                            } else {
                                if (ConfigurationPreferences.isUsingRoot()) {
                                    val popupMenu = PopupSure(layoutInflater.inflate(R.layout.popup_sure, PopupLinearLayout(requireContext()), true), icon)
                                    popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                        override fun onMenuItemClicked(source: String) {
                                            when (source) {
                                                getString(R.string.yes) -> {
                                                    val f = ShellExecutorDialog.newInstance("pm uninstall ${applicationInfo.packageName}")

                                                    f.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                                        override fun onCommandExecuted(result: String) {
                                                            if (result == "Success") {
                                                                onAppUninstalled(true)
                                                            }
                                                        }
                                                    })

                                                    f.show(parentFragmentManager, "shell_executor")
                                                }
                                            }
                                        }
                                    })
                                } else {
                                    applicationInfo.uninstallThisPackage(appUninstallObserver, -1)
                                }
                            }
                        }
                        getString(R.string.send) -> {
                            Preparing.newInstance(applicationInfo)
                                    .show(childFragmentManager, "prepare_send_files")
                        }
                        getString(R.string.clear_data) -> {
                            val popupMenu = PopupSure(layoutInflater.inflate(R.layout.popup_sure, PopupLinearLayout(requireContext()), true), icon)
                            popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                override fun onMenuItemClicked(source: String) {
                                    when (source) {
                                        getString(R.string.yes) -> {
                                            ShellExecutorDialog.newInstance("pm clear ${applicationInfo.packageName}")
                                                    .show(parentFragmentManager, "shell_executor")
                                        }
                                    }
                                }
                            })
                        }
                        getString(R.string.clear_cache) -> {
                            val popupMenu = PopupSure(layoutInflater.inflate(R.layout.popup_sure, PopupLinearLayout(requireContext()), true), icon)
                            popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                override fun onMenuItemClicked(source: String) {
                                    when (source) {
                                        getString(R.string.yes) -> {
                                            ShellExecutorDialog.newInstance(
                                                "rm -r -v /data/data/${applicationInfo.packageName}/cache " +
                                                        "& rm -r -v /data/data/${applicationInfo.packageName}/app_cache " +
                                                        "& rm -r -v /data/data/${applicationInfo.packageName}/app_texture " +
                                                        "& rm -r -v /data/data/${applicationInfo.packageName}/app_webview " +
                                                        "& rm -r -v /data/data/${applicationInfo.packageName}/code_cache",
                                            )
                                                    .show(parentFragmentManager, "shell_executor")
                                        }
                                    }
                                }
                            })
                        }
                        getString(R.string.force_stop) -> {
                            val popupMenu = PopupSure(layoutInflater.inflate(R.layout.popup_sure, PopupLinearLayout(requireContext()), true), icon)
                            popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                override fun onMenuItemClicked(source: String) {
                                    when (source) {
                                        getString(R.string.yes) -> {
                                            ShellExecutorDialog.newInstance("am force-stop ${applicationInfo.packageName}")
                                                    .show(parentFragmentManager, "shell_executor")
                                        }
                                    }
                                }
                            })
                        }
                        getString(R.string.disable) -> {
                            val popupMenu = PopupSure(layoutInflater.inflate(R.layout.popup_sure, PopupLinearLayout(requireContext()), true), icon)
                            popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                override fun onMenuItemClicked(source: String) {
                                    when (source) {
                                        getString(R.string.yes) -> {
                                            val f = ShellExecutorDialog.newInstance("pm disable ${applicationInfo.packageName}")

                                            f.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                                override fun onCommandExecuted(result: String) {
                                                    if (result.contains("disabled")) {
                                                        componentsViewModel.loadOptions()
                                                    }
                                                }
                                            })

                                            f.show(parentFragmentManager, "shell_executor")
                                        }
                                    }
                                }
                            })
                        }
                        getString(R.string.enable) -> {
                            val f = ShellExecutorDialog.newInstance("pm enable ${applicationInfo.packageName}")

                            f.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                override fun onCommandExecuted(result: String) {
                                    if (result.contains("enabled")) {
                                        componentsViewModel.loadOptions()
                                    }
                                }
                            })

                            f.show(parentFragmentManager, "shell_executor")
                        }
                        getString(R.string.open_in_settings) -> {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", applicationInfo.packageName, null)
                            })
                        }
                    }
                }
            })
        })

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(applicationInfo.packageName)

        name.text = applicationInfo.name
        packageId.text = PackageUtils.getApplicationVersion(requireContext(), applicationInfo)

        appInformation.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Information.newInstance(applicationInfo),
                         "information")
        }

        storage.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Storage.newInstance(applicationInfo),
                         getString(R.string.storage))
        }

        directories.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Directories.newInstance(applicationInfo),
                         getString(R.string.directories))
        }
    }

    override fun onAppUninstalled(result: Boolean) {
        if (result) {
            with(allAppsData) {
                loadAppData()

                appLoaded.observe(viewLifecycleOwner, { appsEvent ->
                    appsEvent.getContentIfNotHandledOrReturnNull()?.let {
                        if (it) {
                            requireActivity().supportFragmentManager
                                    .popBackStack()
                        }
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
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
