package app.simple.inure.ui.panels

import android.app.SearchManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.menus.AdapterMenu
import app.simple.inure.adapters.viewers.AdapterTags
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalledAndEnabled
import app.simple.inure.apk.utils.PackageUtils.isSplitApk
import app.simple.inure.apk.utils.PackageUtils.launchThisPackage
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.theme.ThemeDivider
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.decorations.views.GridRecyclerView
import app.simple.inure.decorations.views.TagsRecyclerView
import app.simple.inure.dialogs.action.ClearCache
import app.simple.inure.dialogs.action.ClearData
import app.simple.inure.dialogs.action.Extract.Companion.launchExtract
import app.simple.inure.dialogs.action.ForceStop
import app.simple.inure.dialogs.action.Hide.Companion.showHide
import app.simple.inure.dialogs.action.Reinstaller
import app.simple.inure.dialogs.action.Reinstaller.Companion.showReinstaller
import app.simple.inure.dialogs.action.Send
import app.simple.inure.dialogs.action.SplitApkSelector.Companion.showSplitApkSelector
import app.simple.inure.dialogs.action.State.Companion.showState
import app.simple.inure.dialogs.action.Uninstaller.Companion.uninstallPackage
import app.simple.inure.dialogs.action.UpdatesUninstaller.Companion.showUpdatesUninstaller
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.appinfo.FdroidStores.Companion.showFdroidStores
import app.simple.inure.dialogs.appinfo.SearchBox.Companion.showSearchBox
import app.simple.inure.dialogs.miscellaneous.StoragePermission
import app.simple.inure.dialogs.miscellaneous.StoragePermission.Companion.showStoragePermissionDialog
import app.simple.inure.dialogs.tags.AddTag.Companion.showAddTagDialog
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.glide.util.ImageLoader.loadAPKIcon
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.appinfo.SearchBoxCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.popups.tags.PopupTagMenu
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.AppInformationPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.ui.editor.NotesEditor
import app.simple.inure.ui.subpanels.TaggedApps
import app.simple.inure.ui.viewers.Activities
import app.simple.inure.ui.viewers.Boot
import app.simple.inure.ui.viewers.Certificate
import app.simple.inure.ui.viewers.Dexs
import app.simple.inure.ui.viewers.Extras
import app.simple.inure.ui.viewers.Features
import app.simple.inure.ui.viewers.Graphics
import app.simple.inure.ui.viewers.Information
import app.simple.inure.ui.viewers.Operations
import app.simple.inure.ui.viewers.Permissions
import app.simple.inure.ui.viewers.Providers
import app.simple.inure.ui.viewers.Receivers
import app.simple.inure.ui.viewers.Resources
import app.simple.inure.ui.viewers.Services
import app.simple.inure.ui.viewers.SharedLibs
import app.simple.inure.ui.viewers.Trackers
import app.simple.inure.ui.viewers.UsageStatistics
import app.simple.inure.ui.viewers.UsageStatisticsGraph
import app.simple.inure.ui.viewers.XML
import app.simple.inure.ui.viewers.XMLWebView
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.InfoStripUtils.getAppInfo
import app.simple.inure.util.MarketUtils
import app.simple.inure.util.PermissionUtils.checkStoragePermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.AppInfoMenuViewModel
import app.simple.inure.viewmodels.panels.TagsViewModel
import app.simple.inure.ui.viewers.SharedPreferences.Companion as SharedPreferences_Alias

class AppInfo : ScopedFragment() {

    private lateinit var icon: AppIconImageView

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var details: TypeFaceTextView
    private lateinit var appInformation: DynamicRippleTextView
    private lateinit var usageStatistics: DynamicRippleTextView
    private lateinit var notes: DynamicRippleTextView
    private lateinit var batteryOptimization: DynamicRippleConstraintLayout
    private lateinit var batteryOptimizationState: TypeFaceTextView
    private lateinit var batteryOptimizationSwitch: Switch
    private lateinit var divider1: ThemeDivider
    private lateinit var tagsRecyclerView: TagsRecyclerView
    private lateinit var meta: GridRecyclerView
    private lateinit var actions: GridRecyclerView
    private lateinit var miscellaneous: GridRecyclerView
    private lateinit var metaLayoutButton: DynamicRippleImageButton
    private lateinit var actionsLayoutButton: DynamicRippleImageButton
    private lateinit var miscellaneousLayoutButton: DynamicRippleImageButton

    private lateinit var foldMetaDataMenu: DynamicRippleImageButton
    private lateinit var foldActionsMenu: DynamicRippleImageButton
    private lateinit var foldMiscMenu: DynamicRippleImageButton

    private lateinit var componentsViewModel: AppInfoMenuViewModel
    private lateinit var tagsViewModel: TagsViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    private var metaAdapter: AdapterMenu? = null
    private var actionsAdapter: AdapterMenu? = null
    private var miscellaneousAdapter: AdapterMenu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        details = view.findViewById(R.id.fragment_app_details)
        appInformation = view.findViewById(R.id.app_info_information_tv)
        usageStatistics = view.findViewById(R.id.app_info_storage_tv)
        notes = view.findViewById(R.id.app_info_notes_tv)
        batteryOptimization = view.findViewById(R.id.app_info_battery_optimization)
        batteryOptimizationState = view.findViewById(R.id.battery_optimization_state)
        batteryOptimizationSwitch = view.findViewById(R.id.battery_optimization_switch)
        divider1 = view.findViewById(R.id.app_info_divider_1)
        tagsRecyclerView = view.findViewById(R.id.tags_recycler_view)
        meta = view.findViewById(R.id.app_info_menu)
        actions = view.findViewById(R.id.app_info_options)
        miscellaneous = view.findViewById(R.id.app_info_miscellaneous)
        metaLayoutButton = view.findViewById(R.id.layout_app_info_menu)
        actionsLayoutButton = view.findViewById(R.id.layout_app_info_actions)
        miscellaneousLayoutButton = view.findViewById(R.id.layout_app_info_misc)

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
        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        metaMenuState()
        actionMenuState()
        miscMenuState()

        postponeEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getTags().observe(viewLifecycleOwner) { list ->
            tagsRecyclerView.adapter = AdapterTags(list).apply {
                setOnTagCallbackListener(object : AdapterTags.Companion.TagsCallback {
                    override fun onTagClicked(tag: String) {
                        openFragmentSlide(TaggedApps.newInstance(tag), TaggedApps.TAG)
                    }

                    override fun onTagLongClicked(tag: String) {
                        PopupTagMenu(requireView(), object : PopupTagMenu.Companion.TagsMenuCallback {
                            override fun onDeleteClicked() {
                                tagsViewModel.removeTag(tag, packageInfo) {
                                    this@apply.removeTag(tag)
                                }
                            }
                        })
                    }

                    override fun onAddClicked() {
                        if (fullVersionCheck(goBack = false)) {
                            childFragmentManager.showAddTagDialog().onTag = {
                                tagsViewModel.addTag(it, packageInfo) {
                                    this@apply.addTag(it)
                                }
                            }
                        }
                    }
                })
            }
        }

        if (ConfigurationPreferences.isRootOrShizuku()) {
            if (TrialPreferences.isFullVersion() || TrialPreferences.isWithinTrialPeriod()) {
                batteryOptimization.visible(animate = false)
                divider1.visible(animate = false)

                componentsViewModel.getBatteryOptimization().observe(viewLifecycleOwner) {
                    batteryOptimizationSwitch.isChecked = it.isOptimized

                    if (it.isOptimized) {
                        batteryOptimizationState.setTextWithAnimation(getString(R.string.optimized), 250L)
                    } else {
                        batteryOptimizationState.setTextWithAnimation(getString(R.string.not_optimized), 250L)
                    }

                    batteryOptimizationSwitch.setOnSwitchCheckedChangeListener { isChecked ->
                        if (isChecked) {
                            componentsViewModel.setBatteryOptimization(packageInfo, true)
                        } else {
                            componentsViewModel.setBatteryOptimization(packageInfo, false)
                        }
                    }

                    batteryOptimization.setOnClickListener {
                        batteryOptimizationSwitch.toggle()
                    }
                }
            } else {
                batteryOptimization.gone()
                divider1.gone()
            }
        } else {
            batteryOptimization.gone()
            divider1.gone()
        }

        componentsViewModel.getComponentsOptions().observe(viewLifecycleOwner) {
            when (AppInformationPreferences.getMetaMenuLayout()) {
                AppInformationPreferences.MENU_LAYOUT_HORIZONTAL -> {
                    meta.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                    metaLayoutButton.setImageResource(R.drawable.ic_list_horizontal_16dp)
                }

                AppInformationPreferences.MENU_LAYOUT_GRID -> {
                    meta.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
                    metaLayoutButton.setImageResource(R.drawable.ic_grid_2_16dp)
                }
            }

            if (AppInformationPreferences.isMetaMenuFolded()) {
                startPostponedEnterTransition()
                return@observe
            }

            metaAdapter = AdapterMenu(it, AppInformationPreferences.getMetaMenuLayout())
            metaAdapter?.setHasStableIds(true)

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
                                openFragmentArc(XMLWebView.newInstance(
                                        packageInfo, "AndroidManifest.xml"), icon, XMLWebView.TAG)
                            } else {
                                openFragmentArc(XML.newInstance(
                                        packageInfo, true, "AndroidManifest.xml"), icon, XMLWebView.TAG)
                            }
                        }

                        R.string.services -> {
                            openFragmentArc(Services.newInstance(packageInfo), icon, Services.TAG)
                        }

                        R.string.activities -> {
                            openFragmentArc(Activities.newInstance(packageInfo), icon, Activities.TAG)
                        }

                        R.string.providers -> {
                            openFragmentArc(Providers.newInstance(packageInfo), icon, Providers.TAG)
                        }

                        R.string.permissions -> {
                            openFragmentArc(Permissions.newInstance(packageInfo), icon, Permissions.TAG)
                        }

                        R.string.certificate -> {
                            openFragmentArc(Certificate.newInstance(packageInfo, null), icon, Certificate.TAG)
                        }

                        R.string.receivers -> {
                            openFragmentArc(Receivers.newInstance(packageInfo), icon, Receivers.TAG)
                        }

                        R.string.resources -> {
                            openFragmentArc(Resources.newInstance(packageInfo), icon, Resources.TAG)
                        }

                        R.string.uses_feature -> {
                            openFragmentArc(Features.newInstance(packageInfo), icon, Features.TAG)
                        }

                        R.string.graphics -> {
                            openFragmentArc(Graphics.newInstance(packageInfo), icon, Graphics.TAG)
                        }

                        R.string.extras -> {
                            openFragmentArc(Extras.newInstance(packageInfo), icon, Extras.TAG)
                        }

                        R.string.shared_libs -> {
                            openFragmentArc(SharedLibs.newInstance(packageInfo), icon, SharedLibs.TAG)
                        }

                        R.string.dex_classes -> {
                            openFragmentArc(Dexs.newInstance(packageInfo), icon, Dexs.TAG)
                        }

                        R.string.trackers -> {
                            openFragmentArc(Trackers.newInstance(packageInfo), icon, Trackers.TAG)
                        }

                        R.string.operations -> {
                            openFragmentArc(Operations.newInstance(packageInfo), icon, Operations.TAG)
                        }

                        R.string.boot -> {
                            openFragmentArc(Boot.newInstance(packageInfo), icon, Boot.TAG)
                        }

                        R.string.shared_prefs -> {
                            openFragmentArc(SharedPreferences_Alias.newInstance(packageInfo), icon, SharedPreferences_Alias.TAG)
                        }
                    }
                }
            })
        }

        componentsViewModel.getActionsOptions().observe(viewLifecycleOwner) { it ->

            when (AppInformationPreferences.getActionMenuLayout()) {
                AppInformationPreferences.MENU_LAYOUT_HORIZONTAL -> {
                    actions.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                    actionsLayoutButton.setImageResource(R.drawable.ic_list_horizontal_16dp)
                }

                AppInformationPreferences.MENU_LAYOUT_GRID -> {
                    actions.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
                    actionsLayoutButton.setImageResource(R.drawable.ic_grid_2_16dp)
                }
            }

            if (AppInformationPreferences.isActionMenuFolded()) return@observe

            actionsAdapter = AdapterMenu(it, AppInformationPreferences.getActionMenuLayout())

            actions.adapter = actionsAdapter
            actions.scheduleLayoutAnimation()

            actionsAdapter?.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: Int, icon: ImageView) {
                    when (source) {
                        R.string.launch -> {
                            try {
                                packageInfo.launchThisPackage(requireActivity())
                            } catch (e: NullPointerException) {
                                showWarning(e.message ?: getString(R.string.error))
                            } catch (e: NameNotFoundException) {
                                showWarning(e.message ?: getString(R.string.error))
                            } catch (e: SecurityException) {
                                showWarning(e.message ?: getString(R.string.error))
                            }
                        }

                        R.string.uninstall -> {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    childFragmentManager.uninstallPackage(packageInfo) {
                                        requireActivity().supportFragmentManager.popBackStackImmediate()
                                    }
                                }
                            })
                        }

                        R.string.uninstall_updates -> {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    childFragmentManager.showUpdatesUninstaller(packageInfo) {
                                        componentsViewModel.unsetUpdateFlag()
                                        componentsViewModel.loadActionOptions()
                                    }
                                }
                            })
                        }

                        R.string.reinstall -> {
                            val wasAppInstalled = requirePackageManager().isPackageInstalledAndEnabled(packageInfo.packageName)

                            onSure {
                                childFragmentManager.showReinstaller(packageInfo)
                                    .setReinstallerCallbacks(object : Reinstaller.Companion.ReinstallerCallbacks {
                                        override fun onReinstallSuccess() {
                                            if (wasAppInstalled.invert()) {
                                                icon.loadAppIcon(packageInfo.packageName, enabled = true)
                                                componentsViewModel.loadActionOptions()
                                            }
                                        }
                                    })
                            }
                        }

                        R.string.install -> {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireActivity().applicationContext,
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ packageInfo.applicationInfo.sourceDir.toFile())

                            openFragmentArc(Installer.newInstance(
                                    uri, this@AppInfo.icon.transitionName), this@AppInfo.icon, Installer.TAG)
                        }

                        R.string.send -> {
                            Send.newInstance(packageInfo).show(childFragmentManager, Send.TAG)
                        }

                        R.string.clear_data -> {
                            onSure {
                                ClearData.newInstance(packageInfo).show(parentFragmentManager, ClearData.TAG)
                            }
                        }

                        R.string.clear_cache -> {
                            onSure {
                                ClearCache.newInstance(packageInfo).show(parentFragmentManager, ClearCache.TAG)
                            }
                        }

                        R.string.force_stop -> {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    ForceStop.newInstance(packageInfo).show(childFragmentManager, ForceStop.TAG)
                                }
                            })
                        }

                        R.string.disable, R.string.enable -> {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    childFragmentManager.showState(packageInfo).onSuccess = {
                                        componentsViewModel.loadActionOptions()
                                    }
                                }
                            })
                        }

                        R.string.visible, R.string.hidden -> {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    childFragmentManager.showHide(packageInfo).onSuccess = {
                                        componentsViewModel.loadActionOptions()
                                    }
                                }
                            })
                        }

                        R.string.open_in_settings -> {
                            try {
                                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageInfo.packageName, null)
                                })
                            } catch (e: SecurityException) {
                                showWarning(e.message ?: getString(R.string.error))
                            }
                        }

                        R.string.change_logs -> {
                            openFragmentSlide(WebPage.newInstance(getString(R.string.change_logs)), WebPage.TAG)
                        }

                        R.string.credits -> {
                            openFragmentSlide(WebPage.newInstance(getString(R.string.credits)), WebPage.TAG)
                        }

                        R.string.translate -> {
                            openFragmentSlide(WebPage.newInstance(getString(R.string.translate)), WebPage.TAG)
                        }

                        R.string.preferences -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                if (packageInfo.packageName == requireContext().packageName) {
                                    openFragmentArc(Preferences.newInstance(), icon, Preferences.TAG)
                                } else {
                                    runCatching {
                                        requirePackageManager().queryIntentActivities(Intent(Intent.ACTION_APPLICATION_PREFERENCES), 0)
                                            .forEach {
                                                if (it.activityInfo.packageName == packageInfo.packageName) {
                                                    startActivity(Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
                                                        setClassName(packageInfo.packageName, it.activityInfo.name)
                                                    })
                                                }
                                            }
                                    }.onFailure {
                                        showWarning(it.message ?: getString(R.string.error), goBack = false)
                                    }
                                }
                            }
                        }

                        R.string.search -> {
                            childFragmentManager.showSearchBox(SearchBoxCallbacks { query ->
                                runCatching {
                                    requirePackageManager().queryIntentActivities(Intent(Intent.ACTION_SEARCH), 0)
                                        .forEach {
                                            if (it.activityInfo.packageName == packageInfo.packageName) {
                                                startActivity(Intent(Intent.ACTION_SEARCH).apply {
                                                    setClassName(packageInfo.packageName, it.activityInfo.name)
                                                    putExtra(SearchManager.QUERY, query)
                                                })
                                            }
                                        }
                                }.onFailure {
                                    showWarning(it.message ?: getString(R.string.error), goBack = false)
                                }
                            })
                        }

                        R.string.manage_space -> {
                            runCatching {
                                startActivity(Intent().apply {
                                    setClassName(packageInfo.packageName, packageInfo.applicationInfo.manageSpaceActivityName)
                                })
                            }.onFailure {
                                showWarning(it.message ?: getString(R.string.error))
                            }
                        }
                    }
                }
            })
        }

        componentsViewModel.getTrackers().observe(viewLifecycleOwner) {
            val details = requireContext().getAppInfo(packageInfo)

            if (details.isEmpty()) {
                details.append(getString(R.string.trackers_count, it))
            } else {
                details.append(" | ")
                details.append(getString(R.string.trackers_count, it))
            }

            this.details.alpha = 0F
            this.details.text = details
            this.details.animate().alpha(1F).start()
        }

        componentsViewModel.getMiscellaneousItems().observe(viewLifecycleOwner) {
            when (AppInformationPreferences.getMiscMenuLayout()) {
                AppInformationPreferences.MENU_LAYOUT_HORIZONTAL -> {
                    miscellaneous.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                    miscellaneousLayoutButton.setImageResource(R.drawable.ic_list_horizontal_16dp)
                }

                AppInformationPreferences.MENU_LAYOUT_GRID -> {
                    miscellaneous.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
                    miscellaneousLayoutButton.setImageResource(R.drawable.ic_grid_2_16dp)
                }
            }

            if (AppInformationPreferences.isMiscMenuFolded()) return@observe

            miscellaneousAdapter = AdapterMenu(it, AppInformationPreferences.getMiscMenuLayout())

            miscellaneous.adapter = miscellaneousAdapter
            miscellaneous.scheduleLayoutAnimation()

            miscellaneousAdapter?.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: Int, icon: ImageView) {
                    when (source) {
                        R.string.extract -> {
                            if (requireContext().checkStoragePermission()) {
                                if (packageInfo.isSplitApk()) {
                                    childFragmentManager.showSplitApkSelector(packageInfo)
                                } else {
                                    childFragmentManager.launchExtract(packageInfo, emptySet())
                                }
                            } else {
                                childFragmentManager.showStoragePermissionDialog()
                                    .setStoragePermissionCallbacks(object : StoragePermission.Companion.StoragePermissionCallbacks {
                                        override fun onStoragePermissionGranted() {
                                            childFragmentManager.launchExtract(packageInfo, emptySet())
                                        }
                                    })
                            }
                        }

                        R.string.play_store -> {
                            try {
                                MarketUtils.openAppOnPlayStore(requireContext(), packageInfo.packageName)
                            } catch (e: Exception) {
                                showWarning(e.message ?: getString(R.string.error))
                            }
                        }

                        R.string.fdroid -> {
                            childFragmentManager.showFdroidStores(packageInfo)
                        }
                    }
                }
            })
        }

        componentsViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        icon.transitionName = packageInfo.packageName

        try {
            icon.loadAppIcon(packageInfo.packageName,
                             packageInfo.applicationInfo.enabled,
                             packageInfo.applicationInfo.sourceDir.toFile())
        } catch (e: NullPointerException) {
            icon.loadAPKIcon(packageInfo.applicationInfo.sourceDir)
        }

        name.apply {
            text = packageInfo.applicationInfo.name
            setFOSSIcon(FOSSParser.isPackageFOSS(packageInfo))
        }
        packageId.text = packageInfo.packageName

        appInformation.setOnClickListener {
            openFragmentSlide(Information.newInstance(packageInfo), Information.TAG)
        }

        usageStatistics.setOnClickListener {
            if (DevelopmentPreferences.get(DevelopmentPreferences.useOldStyleUsageStatsPanel)) {
                openFragmentSlide(UsageStatistics.newInstance(packageInfo), UsageStatistics.TAG)
            } else {
                openFragmentSlide(UsageStatisticsGraph.newInstance(packageInfo), UsageStatisticsGraph.TAG)
            }
        }

        notes.setOnClickListener {
            openFragmentSlide(NotesEditor.newInstance(packageInfo), NotesEditor.TAG)
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

        metaLayoutButton.setOnClickListener {
            when (AppInformationPreferences.getMetaMenuLayout()) {
                AppInformationPreferences.MENU_LAYOUT_HORIZONTAL -> {
                    AppInformationPreferences.setMetaMenuLayout(AppInformationPreferences.MENU_LAYOUT_GRID)
                }

                AppInformationPreferences.MENU_LAYOUT_GRID -> {
                    AppInformationPreferences.setMetaMenuLayout(AppInformationPreferences.MENU_LAYOUT_HORIZONTAL)
                }
            }
        }

        actionsLayoutButton.setOnClickListener {
            when (AppInformationPreferences.getActionMenuLayout()) {
                AppInformationPreferences.MENU_LAYOUT_HORIZONTAL -> {
                    AppInformationPreferences.setActionMenuLayout(AppInformationPreferences.MENU_LAYOUT_GRID)
                }

                AppInformationPreferences.MENU_LAYOUT_GRID -> {
                    AppInformationPreferences.setActionMenuLayout(AppInformationPreferences.MENU_LAYOUT_HORIZONTAL)
                }
            }
        }

        miscellaneousLayoutButton.setOnClickListener {
            when (AppInformationPreferences.getMiscMenuLayout()) {
                AppInformationPreferences.MENU_LAYOUT_HORIZONTAL -> {
                    AppInformationPreferences.setMiscMenuLayout(AppInformationPreferences.MENU_LAYOUT_GRID)
                }

                AppInformationPreferences.MENU_LAYOUT_GRID -> {
                    AppInformationPreferences.setMiscMenuLayout(AppInformationPreferences.MENU_LAYOUT_HORIZONTAL)
                }
            }
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

            AppInformationPreferences.metaMenuLayout -> {
                componentsViewModel.loadMetaOptions()
            }

            AppInformationPreferences.actionMenuLayout -> {
                componentsViewModel.loadActionOptions()
            }

            AppInformationPreferences.miscMenuLayout -> {
                componentsViewModel.loadMiscellaneousItems()
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): AppInfo {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = AppInfo()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "app_info"
    }
}
