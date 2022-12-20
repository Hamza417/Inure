package app.simple.inure.ui.panels

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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.menus.AdapterMenu
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.launchThisPackage
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.pinchandzoom.ZoomItemAnimator
import app.simple.inure.decorations.pinchandzoom.ZoomingRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.GridRecyclerView
import app.simple.inure.dialogs.action.*
import app.simple.inure.dialogs.action.Extract.Companion.launchExtract
import app.simple.inure.dialogs.app.Sure
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.appinfo.AppInfoMenu
import app.simple.inure.dialogs.miscellaneous.StoragePermission
import app.simple.inure.dialogs.miscellaneous.StoragePermission.Companion.newStoragePermissionInstance
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.popups.appinfo.PopupMenuLayout
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.AppInformationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.ui.viewers.*
import app.simple.inure.util.MarketUtils
import app.simple.inure.util.PermissionUtils.checkStoragePermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.AppInfoMenuViewModel

class AppInfo : ScopedFragment() {

    private lateinit var icon: ImageView

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var appInformation: DynamicRippleTextView
    private lateinit var usageStatistics: DynamicRippleTextView
    private lateinit var notes: DynamicRippleTextView
    private lateinit var meta: GridRecyclerView
    private lateinit var actions: GridRecyclerView
    private lateinit var miscellaneous: GridRecyclerView

    private lateinit var foldMetaDataMenu: DynamicRippleImageButton
    private lateinit var foldActionsMenu: DynamicRippleImageButton
    private lateinit var foldMiscMenu: DynamicRippleImageButton

    private lateinit var componentsViewModel: AppInfoMenuViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    private var metaAdapter: AdapterMenu? = null
    private var actionsAdapter: AdapterMenu? = null
    private var miscellaneousAdapter: AdapterMenu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        settings = view.findViewById(R.id.settings_button)
        appInformation = view.findViewById(R.id.app_info_information_tv)
        usageStatistics = view.findViewById(R.id.app_info_storage_tv)
        notes = view.findViewById(R.id.app_info_notes_tv)
        meta = view.findViewById(R.id.app_info_menu)
        actions = view.findViewById(R.id.app_info_options)
        miscellaneous = view.findViewById(R.id.app_info_miscellaneous)

        if (AccessibilityPreferences.isAnimationReduced()) {
            meta.layoutAnimation = null
            actions.layoutAnimation = null
            miscellaneous.layoutAnimation = null
        }

        foldMetaDataMenu = view.findViewById(R.id.fold_app_info_menu)
        foldActionsMenu = view.findViewById(R.id.fold_app_info_actions)
        foldMiscMenu = view.findViewById(R.id.fold_app_info_misc)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory)[AppInfoMenuViewModel::class.java]

        metaMenuState()
        actionMenuState()
        miscMenuState()

        postponeEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getMenuItems().observe(viewLifecycleOwner) {
            if (AppInformationPreferences.isMetaMenuFolded()) return@observe

            metaAdapter = AdapterMenu(it)
            metaAdapter?.setHasStableIds(true)

            when (AppInformationPreferences.getMenuLayout()) {
                PopupMenuLayout.HORIZONTAL -> {
                    meta.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                }
                PopupMenuLayout.GRID -> {
                    meta.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
                }
            }

            meta.adapter = metaAdapter
            meta.scheduleLayoutAnimation()

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            metaAdapter?.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: Int, icon: ImageView) {
                    when (source) {
                        R.string.manifest -> {
                            if (DevelopmentPreferences.get(DevelopmentPreferences.isWebViewXmlViewer)) {
                                openFragmentArc(XMLViewerWebView.newInstance(packageInfo, true, "AndroidManifest.xml"), icon, "manifest")
                            } else {
                                openFragmentArc(XMLViewerTextView.newInstance(packageInfo, true, "AndroidManifest.xml"), icon, "manifest")
                            }
                        }
                        R.string.services -> {
                            openFragmentArc(Services.newInstance(packageInfo), icon, "services")
                        }
                        R.string.activities -> {
                            openFragmentArc(Activities.newInstance(packageInfo), icon, "activities")
                        }
                        R.string.providers -> {
                            openFragmentArc(Providers.newInstance(packageInfo), icon, "providers")
                        }
                        R.string.permissions -> {
                            openFragmentArc(Permissions.newInstance(packageInfo), icon, "permissions")
                        }
                        R.string.certificate -> {
                            openFragmentArc(Certificate.newInstance(packageInfo, null), icon, "certificate")
                        }
                        R.string.receivers -> {
                            openFragmentArc(Receivers.newInstance(packageInfo), icon, "broadcasts")
                        }
                        R.string.resources -> {
                            openFragmentArc(Resources.newInstance(packageInfo), icon, "resources")
                        }
                        R.string.uses_feature -> {
                            openFragmentArc(Features.newInstance(packageInfo), icon, "uses_feature")
                        }
                        R.string.graphics -> {
                            openFragmentArc(Graphics.newInstance(packageInfo), icon, "graphics")
                        }
                        R.string.extras -> {
                            openFragmentArc(Extras.newInstance(packageInfo), icon, "extras")
                        }
                        R.string.shared_libs -> {
                            openFragmentArc(SharedLibs.newInstance(packageInfo), icon, "shared_libs")
                        }
                        R.string.dex_classes -> {
                            openFragmentArc(Dexs.newInstance(packageInfo), icon, "dexs")
                        }
                        R.string.trackers -> {
                            openFragmentArc(Trackers.newInstance(packageInfo), icon, "trackers")
                        }
                        R.string.operations -> {
                            openFragmentArc(Operations.newInstance(packageInfo), icon, "ops")
                        }
                        R.string.shared_prefs -> {
                            openFragmentArc(app.simple.inure.ui.viewers.SharedPreferences.newInstance(packageInfo), icon, "shared_prefs")
                        }
                    }
                }
            })
        }

        componentsViewModel.getMenuOptions().observe(viewLifecycleOwner) {
            if (AppInformationPreferences.isActionMenuFolded()) return@observe

            actionsAdapter = AdapterMenu(it)

            when (AppInformationPreferences.getMenuLayout()) {
                PopupMenuLayout.HORIZONTAL -> {
                    actions.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                }
                PopupMenuLayout.GRID -> {
                    actions.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
                }
            }

            actions.adapter = actionsAdapter
            actions.scheduleLayoutAnimation()

            actionsAdapter?.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: Int, icon: ImageView) {
                    when (source) {
                        R.string.launch -> {
                            packageInfo.launchThisPackage(requireActivity())
                        }
                        R.string.uninstall -> {
                            val sure = Sure.newInstance()

                            sure.setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    val uninstaller = Uninstaller.newInstance(packageInfo)

                                    uninstaller.listener = {
                                        requireActivity().supportFragmentManager.popBackStackImmediate()
                                    }

                                    uninstaller.show(childFragmentManager, "uninstaller")
                                }
                            })

                            sure.show(childFragmentManager, "sure")
                        }
                        R.string.send -> {
                            Preparing.newInstance(packageInfo).show(childFragmentManager, "prepare_send_files")
                        }
                        R.string.clear_data -> {
                            val p = Sure.newInstance()
                            p.setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    ClearData.newInstance(packageInfo).show(parentFragmentManager, "shell_executor")
                                }
                            })

                            p.show(childFragmentManager, "sure")
                        }
                        R.string.clear_cache -> {
                            val p = Sure.newInstance()
                            p.setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    ClearCache.newInstance(packageInfo).show(parentFragmentManager, "clear_cache")
                                }
                            })

                            p.show(childFragmentManager, "sure")
                        }
                        R.string.force_stop -> {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    ForceStop.newInstance(packageInfo).show(childFragmentManager, "force_stop")
                                }
                            })
                        }
                        R.string.disable, R.string.enable -> {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    val f = State.newInstance(getPackageInfo(packageInfo.packageName))

                                    f.onSuccess = {
                                        componentsViewModel.loadActionOptions()
                                    }

                                    f.show(childFragmentManager, "state")
                                }
                            })
                        }
                        R.string.open_in_settings -> {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageInfo.packageName, null)
                            })
                        }
                        R.string.change_logs -> {
                            openFragmentSlide(WebPage.newInstance(getString(R.string.change_logs)), "change_logs")
                        }
                    }
                }
            })
        }

        componentsViewModel.getMiscellaneousItems().observe(viewLifecycleOwner) {

            if (AppInformationPreferences.isMiscMenuFolded()) return@observe

            miscellaneousAdapter = AdapterMenu(it)

            when (AppInformationPreferences.getMenuLayout()) {
                PopupMenuLayout.HORIZONTAL -> {
                    miscellaneous.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                }
                PopupMenuLayout.GRID -> {
                    miscellaneous.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
                }
            }

            miscellaneous.adapter = miscellaneousAdapter
            miscellaneous.scheduleLayoutAnimation()

            miscellaneousAdapter?.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: Int, icon: ImageView) {
                    when (source) {
                        R.string.extract -> {
                            if (requireContext().checkStoragePermission()) {
                                childFragmentManager.launchExtract(packageInfo)
                            } else {
                                childFragmentManager.newStoragePermissionInstance().setStoragePermissionCallbacks(object : StoragePermission.Companion.StoragePermissionCallbacks {
                                    override fun onStoragePermissionGranted() {
                                        childFragmentManager.launchExtract(packageInfo)
                                    }
                                })
                            }
                        }
                        R.string.play_store -> {
                            MarketUtils.openAppOnPlayStore(requireContext(), packageInfo.packageName)
                        }
                        R.string.amazon -> {
                            MarketUtils.openAppOnAmazonStore(requireContext(), packageInfo.packageName)
                        }
                        R.string.fdroid -> {
                            MarketUtils.openAppOnFdroid(requireContext(), packageInfo.packageName)
                        }
                    }
                }
            })
        }

        componentsViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(packageInfo.packageName, packageInfo.applicationInfo.enabled)

        name.text = packageInfo.applicationInfo.name
        packageId.text = PackageUtils.getApplicationVersion(requireContext(), packageInfo)

        settings.setOnClickListener {
            AppInfoMenu.newInstance()
                .show(childFragmentManager, "app_info_menu")
        }

        appInformation.setOnClickListener {
            openFragmentSlide(Information.newInstance(packageInfo), "information")
        }

        usageStatistics.setOnClickListener {
            openFragmentSlide(UsageStatistics.newInstance(packageInfo), "storage")
        }

        notes.setOnClickListener {
            openFragmentSlide(NotesEditor.newInstance(packageInfo), "notes_viewer")
        }

        foldMetaDataMenu.setOnClickListener {
            AppInformationPreferences.setMetaMenuFold(!AppInformationPreferences.isMetaMenuFolded())
        }

        foldActionsMenu.setOnClickListener {
            AppInformationPreferences.setActionMenuFold(!AppInformationPreferences.isActionMenuFolded())
        }

        foldMiscMenu.setOnClickListener {
            AppInformationPreferences.setMiscMenuFold(!AppInformationPreferences.isMiscMenuFolded())
        }
    }

    private fun metaMenuState() {
        if (AppInformationPreferences.isMetaMenuFolded()) {
            meta.gone()
            meta.adapter = null
            foldMetaDataMenu.animate().rotation(-90F).start()
        } else {
            meta.visible(false)
            foldMetaDataMenu.animate().rotation(0F).start()
        }
    }

    private fun actionMenuState() {
        if (AppInformationPreferences.isActionMenuFolded()) {
            actions.gone()
            actions.adapter = null
            foldActionsMenu.animate().rotation(-90F).start()
        } else {
            actions.visible(false)
            foldActionsMenu.animate().rotation(0F).start()
        }
    }

    private fun miscMenuState() {
        if (AppInformationPreferences.isMiscMenuFolded()) {
            miscellaneous.gone()
            miscellaneous.adapter = null
            foldMiscMenu.animate().rotation(-90F).start()
        } else {
            miscellaneous.visible(false)
            foldMiscMenu.animate().rotation(0F).start()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppInformationPreferences.metaMenuState -> {
                metaMenuState()
                componentsViewModel.loadMetaOptions()
            }
            AppInformationPreferences.actionMenuState -> {
                actionMenuState()
                componentsViewModel.loadActionOptions()
            }
            AppInformationPreferences.miscMenuState -> {
                miscMenuState()
                componentsViewModel.loadMiscellaneousItems()
            }
            AppInformationPreferences.menuLayout -> {
                /**
                 * Load all the menus back again
                 */
                componentsViewModel.loadMiscellaneousItems()
                componentsViewModel.loadMetaOptions()
                componentsViewModel.loadActionOptions()
            }
        }
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