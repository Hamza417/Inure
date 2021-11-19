package app.simple.inure.ui.app

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
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
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.action.ForceStop
import app.simple.inure.dialogs.action.State
import app.simple.inure.dialogs.action.Uninstaller
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.dialogs.miscellaneous.Preparing
import app.simple.inure.dialogs.miscellaneous.ShellExecutorDialog
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.popups.app.PopupSure
import app.simple.inure.preferences.AppInfoPanelPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.ui.viewers.*
import app.simple.inure.util.FragmentHelper.openFragment
import app.simple.inure.util.MarketUtils
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.InfoPanelMenuData


class AppInfo : ScopedFragment() {

    private lateinit var icon: ImageView

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var appInformation: DynamicRippleTextView
    private lateinit var storage: DynamicRippleTextView
    private lateinit var directories: DynamicRippleTextView
    private lateinit var meta: RecyclerView
    private lateinit var actions: RecyclerView
    private lateinit var miscellaneous: RecyclerView

    private lateinit var foldMetaDataMenu: DynamicRippleImageButton
    private lateinit var foldActionsMenu: DynamicRippleImageButton
    private lateinit var foldMiscMenu: DynamicRippleImageButton

    private lateinit var componentsViewModel: InfoPanelMenuData
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        appInformation = view.findViewById(R.id.app_info_information_tv)
        storage = view.findViewById(R.id.app_info_storage_tv)
        directories = view.findViewById(R.id.app_info_directories_tv)
        meta = view.findViewById(R.id.app_info_menu)
        actions = view.findViewById(R.id.app_info_options)
        miscellaneous = view.findViewById(R.id.app_info_miscellaneous)

        foldMetaDataMenu = view.findViewById(R.id.fold_app_info_menu)
        foldActionsMenu = view.findViewById(R.id.fold_app_info_actions)
        foldMiscMenu = view.findViewById(R.id.fold_app_info_misc)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        componentsViewModel =
            ViewModelProvider(this, packageInfoFactory).get(InfoPanelMenuData::class.java)

        metaMenuState()
        actionMenuState()
        miscMenuState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getMenuItems().observe(viewLifecycleOwner, {
            postponeEnterTransition()

            if (AppInfoPanelPreferences.isMetaMenuFolded()) {
                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
                return@observe
            }

            val adapterMenu = AdapterMenu(it)
            adapterMenu.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            meta.layoutManager =
                GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
            meta.adapter = adapterMenu
            meta.scheduleLayoutAnimation()

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapterMenu.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.manifest) -> {
                            if (ConfigurationPreferences.isXmlViewerTextView()) {
                                openFragment(requireActivity().supportFragmentManager,
                                             XMLViewerTextView.newInstance(packageInfo, true, "AndroidManifest.xml"),
                                             icon, "manifest")
                            } else {
                                openFragment(requireActivity().supportFragmentManager,
                                             XMLViewerWebView.newInstance(packageInfo, true, "AndroidManifest.xml"),
                                             icon, "manifest")
                            }
                        }
                        getString(R.string.services) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Services.newInstance(packageInfo),
                                         icon, "services")
                        }
                        getString(R.string.activities) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Activities.newInstance(packageInfo),
                                         icon, "activities")
                        }
                        getString(R.string.providers) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Providers.newInstance(packageInfo),
                                         icon, "providers")
                        }
                        getString(R.string.permissions) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Permissions.newInstance(packageInfo),
                                         icon, "permissions")
                        }
                        getString(R.string.certificate) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Certificate.newInstance(packageInfo),
                                         icon, "certificate")
                        }
                        getString(R.string.receivers) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Receivers.newInstance(packageInfo),
                                         icon, "broadcasts")
                        }
                        getString(R.string.resources) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Resources.newInstance(packageInfo),
                                         icon, "resources")
                        }
                        getString(R.string.uses_feature) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Features.newInstance(packageInfo),
                                         icon, "uses_feature")
                        }
                        getString(R.string.graphics) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Graphics.newInstance(packageInfo),
                                         icon, "graphics")
                        }
                        getString(R.string.extras) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Extras.newInstance(packageInfo),
                                         icon, "extras")
                        }
                        getString(R.string.dex_classes) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Dexs.newInstance(packageInfo),
                                         icon, "dexs")
                        }
                    }
                }
            })
        })

        componentsViewModel.getMenuOptions().observe(viewLifecycleOwner, {

            if (AppInfoPanelPreferences.isActionMenuFolded()) return@observe

            val adapterAppInfoMenu = AdapterMenu(it)
            actions.layoutManager =
                GridLayoutManager(requireContext(), getInteger(R.integer.span_count), GridLayoutManager.VERTICAL, false)
            actions.adapter = adapterAppInfoMenu
            actions.scheduleLayoutAnimation()

            adapterAppInfoMenu.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.launch) -> {
                            packageInfo.launchThisPackage(requireActivity())
                        }
                        getString(R.string.uninstall) -> {
                            if (ConfigurationPreferences.isUsingRoot()) {
                                PopupSure(icon).onSure = {
                                    val p = Uninstaller.newInstance(packageInfo)

                                    p.listener = {
                                        requireActivity().supportFragmentManager.popBackStackImmediate()
                                    }

                                    p.show(childFragmentManager, "uninstaller")
                                }
                            } else {
                                val p = Uninstaller.newInstance(packageInfo)

                                p.listener = {
                                    requireActivity().supportFragmentManager.popBackStackImmediate()
                                }

                                p.show(childFragmentManager, "uninstaller")
                            }
                        }
                        getString(R.string.send) -> {
                            Preparing.newInstance(packageInfo)
                                .show(childFragmentManager, "prepare_send_files")
                        }
                        getString(R.string.clear_data) -> {
                            PopupSure(icon).onSure = {
                                ShellExecutorDialog.newInstance("pm clear ${packageInfo.packageName}")
                                    .show(parentFragmentManager, "shell_executor")
                            }
                        }
                        getString(R.string.clear_cache) -> {
                            PopupSure(icon).onSure = {
                                ShellExecutorDialog.newInstance(
                                        "rm -r -v /data/data/${packageInfo.packageName}/cache " +
                                                "& rm -r -v /data/data/${packageInfo.packageName}/app_cache " +
                                                "& rm -r -v /data/data/${packageInfo.packageName}/app_texture " +
                                                "& rm -r -v /data/data/${packageInfo.packageName}/app_webview " +
                                                "& rm -r -v /data/data/${packageInfo.packageName}/code_cache",
                                )
                                    .show(parentFragmentManager, "shell_executor")
                            }
                        }
                        getString(R.string.force_stop) -> {
                            PopupSure(icon).onSure = {
                                ForceStop.newInstance(packageInfo)
                                    .show(childFragmentManager, "force_stop")
                            }
                        }
                        getString(R.string.disable),
                        getString(R.string.enable) -> {
                            PopupSure(icon).onSure = {
                                val f =
                                    State.newInstance(requireContext().packageManager.getPackageInfo(packageInfo.packageName, 0))

                                f.onSuccess = {
                                    componentsViewModel.loadActionOptions()
                                }

                                f.show(childFragmentManager, "state")
                            }
                        }
                        getString(R.string.open_in_settings) -> {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageInfo.packageName, null)
                            })
                        }
                    }
                }
            })
        })

        componentsViewModel.getMiscellaneousItems().observe(viewLifecycleOwner, {

            if (AppInfoPanelPreferences.isMiscMenuFolded()) return@observe

            val adapterAppInfoMenu = AdapterMenu(it)
            miscellaneous.layoutManager =
                GridLayoutManager(requireContext(), getInteger(R.integer.span_count), GridLayoutManager.VERTICAL, false)
            miscellaneous.adapter = adapterAppInfoMenu
            miscellaneous.scheduleLayoutAnimation()

            adapterAppInfoMenu.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.play_store) -> {
                            MarketUtils.openAppOnPlayStore(requireContext(), packageInfo.packageName)
                        }
                        getString(R.string.amazon) -> {
                            MarketUtils.openAppOnAmazonStore(requireContext(), packageInfo.packageName)
                        }
                        getString(R.string.fdroid) -> {
                            MarketUtils.openAppOnFdroid(requireContext(), packageInfo.packageName)
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
        icon.loadAppIcon(packageInfo.packageName)

        name.text = packageInfo.applicationInfo.name
        packageId.text = PackageUtils.getApplicationVersion(requireContext(), packageInfo)

        appInformation.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Information.newInstance(packageInfo),
                         "information")
        }

        storage.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Storage.newInstance(packageInfo),
                         getString(R.string.storage))
        }

        directories.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Directories.newInstance(packageInfo),
                         getString(R.string.directories))
        }

        foldMetaDataMenu.setOnClickListener {
            AppInfoPanelPreferences.setMetaMenuFold(!AppInfoPanelPreferences.isMetaMenuFolded())
        }

        foldActionsMenu.setOnClickListener {
            AppInfoPanelPreferences.setActionMenuFold(!AppInfoPanelPreferences.isActionMenuFolded())
        }

        foldMiscMenu.setOnClickListener {
            AppInfoPanelPreferences.setMiscMenuFold(!AppInfoPanelPreferences.isMiscMenuFolded())
        }
    }

    private fun metaMenuState() {
        if (AppInfoPanelPreferences.isMetaMenuFolded()) {
            meta.gone()
            meta.adapter = null
            foldMetaDataMenu.animate().rotation(-90F).start()
        } else {
            componentsViewModel.loadMetaOptions()
            meta.visible(false)
            foldMetaDataMenu.animate().rotation(0F).start()
        }
    }

    private fun actionMenuState() {
        if (AppInfoPanelPreferences.isActionMenuFolded()) {
            actions.gone()
            actions.adapter = null
            foldActionsMenu.animate().rotation(-90F).start()
        } else {
            componentsViewModel.loadActionOptions()
            actions.visible(false)
            foldActionsMenu.animate().rotation(0F).start()
        }
    }

    private fun miscMenuState() {
        if (AppInfoPanelPreferences.isMiscMenuFolded()) {
            miscellaneous.gone()
            miscellaneous.adapter = null
            foldMiscMenu.animate().rotation(-90F).start()
        } else {
            componentsViewModel.loadMiscellaneousItems()
            miscellaneous.visible(false)
            foldMiscMenu.animate().rotation(0F).start()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppInfoPanelPreferences.metaMenuState -> {
                metaMenuState()
            }
            AppInfoPanelPreferences.actionMenuState -> {
                actionMenuState()
            }
            AppInfoPanelPreferences.miscMenuState -> {
                miscMenuState()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, transitionName: String): AppInfo {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.transitionName, transitionName)
            val fragment = AppInfo()
            fragment.arguments = args
            return fragment
        }
    }
}