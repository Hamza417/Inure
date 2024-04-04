package app.simple.inure.ui.panels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBatch
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.IntentConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.batch.BatchActions.Companion.showBatchActions
import app.simple.inure.dialogs.batch.BatchBatteryOptimization.Companion.showBatchBatteryOptimization
import app.simple.inure.dialogs.batch.BatchExtract.Companion.showBatchExtract
import app.simple.inure.dialogs.batch.BatchForceStop.Companion.showBatchForceStop
import app.simple.inure.dialogs.batch.BatchMenu
import app.simple.inure.dialogs.batch.BatchMenu.Companion.showBatchMenu
import app.simple.inure.dialogs.batch.BatchProfiles
import app.simple.inure.dialogs.batch.BatchProfiles.Companion.showBatchProfiles
import app.simple.inure.dialogs.batch.BatchSaveProfile.Companion.showBatchProfileSave
import app.simple.inure.dialogs.batch.BatchSort.Companion.showBatchSort
import app.simple.inure.dialogs.batch.BatchState.Companion.showBatchStateDialog
import app.simple.inure.dialogs.batch.BatchUninstaller
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.dialogs.miscellaneous.GenerateAppData.Companion.showGeneratedDataTypeSelector
import app.simple.inure.dialogs.miscellaneous.StoragePermission
import app.simple.inure.dialogs.miscellaneous.StoragePermission.Companion.showStoragePermissionDialog
import app.simple.inure.dialogs.tags.AddTag.Companion.showAddTagDialog
import app.simple.inure.dialogs.tags.AddedTag.Companion.showAddedApps
import app.simple.inure.dialogs.tags.TagPicker
import app.simple.inure.dialogs.tags.TagPicker.Companion.showTagPicker
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.models.BatchProfile
import app.simple.inure.models.Tag
import app.simple.inure.popups.batch.PopupBatchState
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.inure.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.inure.services.BatchExtractService
import app.simple.inure.ui.subpanels.BatchSelectedApps
import app.simple.inure.ui.subpanels.BatchTracker
import app.simple.inure.ui.viewers.HtmlViewer
import app.simple.inure.ui.viewers.JSON
import app.simple.inure.ui.viewers.Markdown
import app.simple.inure.ui.viewers.XMLViewerTextView
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.PermissionUtils.checkStoragePermission
import app.simple.inure.viewmodels.panels.BatchViewModel
import app.simple.inure.viewmodels.panels.TagsViewModel

class Batch : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterBatch: AdapterBatch? = null
    private var batchExtractService: BatchExtractService? = null
    private var batchExtractServiceConnection: ServiceConnection? = null

    private var isServiceBound = false

    private lateinit var batchViewModel: BatchViewModel
    private lateinit var tagsViewModel: TagsViewModel

    private var appUninstallObserver = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d("Batch", "Uninstalled successfully")
            }
            Activity.RESULT_CANCELED -> {
                Log.d("Batch", "Uninstall cancelled")
            }
            else -> {
                Log.d("Batch", "Uninstall failed")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_batch, container, false)

        recyclerView = view.findViewById(R.id.batch_recycler_view)
        batchViewModel = ViewModelProvider(requireActivity())[BatchViewModel::class.java]
        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (fullVersionCheck()) {
            if (batchViewModel.shouldShowLoader()) {
                showLoader(manualOverride = true)
            }
        }

        batchViewModel.getBatchData().observe(viewLifecycleOwner) {
            adapterBatch = AdapterBatch(it)

            hideLoader()

            adapterBatch?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openFragmentArc(AppInfo.newInstance(packageInfo), icon, "app_info")
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }

                override fun onBatchChanged(batchPackageInfo: BatchPackageInfo) {
                    batchViewModel.updateBatchItem(batchPackageInfo, updateSelected = true)
                    setupBottomMenu()
                }
            })

            recyclerView.adapter = adapterBatch
            setupBottomMenu()

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    private fun setupBottomMenu() {
        if (bottomRightCornerMenu?.menuAdapter?.itemCount == getBatchMenuItems().size) {
            return
        } else {
            bottomRightCornerMenu?.setInitialized(false)
        }

        bottomRightCornerMenu?.initBottomMenuWithRecyclerView(getBatchMenuItems(), recyclerView) { id, _ ->
            when (id) {
                R.drawable.ic_filter -> {
                    childFragmentManager.showBatchSort()
                }

                R.drawable.ic_search -> {
                    openFragmentSlide(Search.newInstance(true), "search")
                }

                R.drawable.ic_settings -> {
                    childFragmentManager.showBatchMenu().setBatchMenuListener(object : BatchMenu.Companion.BatchMenuListener {
                        override fun onSaveProfile() {
                            val packages = adapterBatch?.getCurrentAppsList()?.map {
                                it.packageInfo.packageName + "_" + it.dateSelected
                            } as ArrayList<String>

                            if (packages.isNotEmpty()) {
                                childFragmentManager.showBatchProfileSave(packages)
                            } else {
                                showWarning("ERR: No apps selected", goBack = false)
                            }
                        }

                        override fun onLoadProfile() {
                            childFragmentManager.showBatchProfiles().setOnProfileSelected(object : BatchProfiles.Companion.BatchProfilesCallback {
                                override fun onProfileSelected(profile: BatchProfile) {
                                    unregisterSharedPreferenceChangeListener()

                                    // showLoader(manualOverride = true)
                                    if (BatchPreferences.setAppsCategory(profile.appType)) {
                                        if (BatchPreferences.setSortStyle(profile.sortStyle)) {
                                            if (BatchPreferences.setReverseSorting(profile.isReversed)) {
                                                if (BatchPreferences.setAppsFilter(profile.filterStyle)) {
                                                    if (BatchPreferences.setLastSelectedProfile(profile.id)) {
                                                        batchViewModel.loadBatchProfile(profile.id)
                                                        registerSharedPreferenceChangeListener()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        }

                        override fun onTagPicker() {
                            childFragmentManager.showTagPicker().setTagPickerCallbacks(object : TagPicker.Companion.TagPickerCallbacks {
                                override fun onTagPicked(tag: Tag) {
                                    adapterBatch?.createSelectionFromTags(tag)
                                }
                            })
                        }
                    })
                }

                R.drawable.ic_extension -> {
                    childFragmentManager.showBatchActions().setBatchActionCallbackListener { iconId, view ->
                        when (iconId) {
                            R.drawable.ic_select_all -> {
                                showLoader(manualOverride = true)

                                if (adapterBatch?.isAllSelected() == true) {
                                    batchViewModel.deselectAllBatchItems()
                                } else {
                                    batchViewModel.selectAllBatchItems()
                                }
                            }

                            R.drawable.ic_delete -> {
                                if (adapterBatch?.getSelectedAppsCount()!! < adapterBatch?.itemCount!!.minus(1)) { // We're subtracting one because header
                                    childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                        override fun onSure() {
                                            if (ConfigurationPreferences.isUsingRoot() || ConfigurationPreferences.isUsingShizuku()) {
                                                BatchUninstaller.newInstance(adapterBatch!!.getCurrentAppsList())
                                                    .show(childFragmentManager, "batch_uninstaller")
                                            } else {
                                                for (app in adapterBatch?.getCurrentAppsList()!!) {
                                                    @Suppress("DEPRECATION") val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                                                    intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                                                    intent.putExtra(IntentConstants.EXTRA_PACKAGE_NAME, app.packageInfo.packageName)
                                                    intent.data = Uri.parse("package:${app.packageInfo.packageName}")
                                                    appUninstallObserver.launch(intent)
                                                }
                                            }
                                        }
                                    })
                                } else {
                                    showWarning("RESTRICTION: Cannot uninstall all apps at once", goBack = false)
                                }
                            }

                            R.drawable.ic_hide_source -> {
                                if (adapterBatch?.getSelectedAppsCount()!! < adapterBatch?.itemCount!!.minus(1)) { // We're subtracting one because header
                                    PopupBatchState(view).setOnPopupBatchStateCallbacks(object : PopupBatchState.Companion.PopupBatchStateCallbacks {
                                        override fun onEnableAll() {
                                            onSure {
                                                childFragmentManager.showBatchStateDialog(adapterBatch!!.getCurrentAppsList(), true) {
                                                    for (app in adapterBatch!!.getCurrentAppsList()) {
                                                        adapterBatch!!.updateBatchItem(app)
                                                    }
                                                }
                                            }
                                        }

                                        override fun onDisableAll() {
                                            onSure {
                                                childFragmentManager.showBatchStateDialog(adapterBatch!!.getCurrentAppsList(), false) {
                                                    for (app in adapterBatch!!.getCurrentAppsList()) {
                                                        adapterBatch!!.updateBatchItem(app)
                                                    }
                                                }
                                            }
                                        }
                                    })
                                } else {
                                    showWarning("RESTRICTION: Cannot change state of all apps at once", goBack = false)
                                }
                            }

                            R.drawable.ic_send -> {
                                /* no-op */
                            }

                            R.drawable.ic_downloading -> {
                                if (batchExtractService?.isExtracting()?.invert() == true) {
                                    childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                        override fun onSure() {
                                            if (requireContext().checkStoragePermission()) {
                                                initiateExtractProcess()
                                            } else {
                                                childFragmentManager.showStoragePermissionDialog()
                                                    .setStoragePermissionCallbacks(object : StoragePermission.Companion.StoragePermissionCallbacks {
                                                        override fun onStoragePermissionGranted() {
                                                            initiateExtractProcess()
                                                        }
                                                    })
                                            }
                                        }
                                    })
                                } else {
                                    showWarning("ERR: a process is already running", goBack = false)
                                }
                            }

                            R.drawable.ic_text_snippet -> {
                                childFragmentManager.showGeneratedDataTypeSelector().onGenerateData {
                                    showLoader(manualOverride = true)
                                    adapterBatch?.getCurrentAppsList()?.let {
                                        batchViewModel.generateAppsData(it)
                                    }
                                }
                            }

                            R.drawable.ic_tags -> {
                                childFragmentManager.showAddTagDialog().onTag = {
                                    tagsViewModel.addMultipleAppsToTag(adapterBatch?.getCurrentAppsList()!!, it) {
                                        showLoader(manualOverride = true)
                                        postDelayed {
                                            hideLoader()
                                            childFragmentManager.showAddedApps(it)
                                        }
                                    }
                                }
                            }

                            R.drawable.ic_settings_power -> {
                                childFragmentManager.showBatchBatteryOptimization(adapterBatch?.getCurrentAppsList()!!)
                            }

                            R.drawable.ic_radiation_nuclear -> {
                                openFragmentSlide(
                                        BatchTracker.newInstance(adapterBatch?.getCurrentAppsList()!!.map {
                                            it.packageInfo.packageName
                                        } as ArrayList<String>), "batch_tracker")
                            }

                            R.drawable.ic_checklist -> {
                                openFragmentSlide(BatchSelectedApps.newInstance(), "batch_selected_apps")
                            }

                            R.drawable.ic_refresh -> {
                                showLoader(manualOverride = true)
                                batchViewModel.refreshPackageData()
                            }

                            R.drawable.ic_close -> {
                                onSure {
                                    childFragmentManager.showBatchForceStop(adapterBatch?.getCurrentAppsList()!!)
                                }
                            }

                            R.drawable.ic_broom -> {
                                onSure {
                                    showLoader(manualOverride = true)

                                    batchViewModel.getClearedCacheSize().observe(viewLifecycleOwner) {
                                        hideLoader()

                                        if (it.isNotNull()) {
                                            showWarning(getString(R.string.cleared).plus(" ${it.toSize()}"), goBack = false)
                                        }
                                    }

                                    batchViewModel.clearSelectedAppsCache(adapterBatch?.getCurrentAppsList()!!)
                                }
                            }
                        }
                    }
                }
            }
        }

        batchViewModel.getGeneratedDataPath().observe(viewLifecycleOwner) {
            if (it.isNotNull()) {
                hideLoader()
                when {
                    it.endsWith(".xml") ||
                            it.endsWith(".txt") ||
                            it.endsWith(".csv") -> {
                        openFragmentSlide(
                                XMLViewerTextView
                                    .newInstance(packageInfo = PackageInfo(), /* Empty package info */
                                                 isManifest = false,
                                                 pathToXml = it,
                                                 isRaw = true), "xml_viewer")
                    }

                    it.endsWith(".html") -> {
                        openFragmentSlide(
                                HtmlViewer
                                    .newInstance(packageInfo = PackageInfo(), it,
                                                 isRaw = true), "web_page")
                    }

                    it.endsWith(".json") -> {
                        openFragmentSlide(
                                JSON.newInstance(packageInfo = PackageInfo(),
                                                 path = it,
                                                 isRaw = true), "json_viewer")
                    }

                    it.endsWith(".md") -> {
                        openFragmentSlide(
                                Markdown.newInstance(packageInfo = PackageInfo(),
                                                     path = it,
                                                     isRaw = true), "markdown_viewer")
                    }
                }

                batchViewModel.clearGeneratedAppsDataLiveData()
            } else {
                hideLoader()
            }
        }

        batchExtractServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: android.content.ComponentName?, service: android.os.IBinder?) {
                val binder = service as BatchExtractService.BatchExtractServiceBinder
                batchExtractService = binder.getService()
                isServiceBound = true
            }

            override fun onServiceDisconnected(name: android.content.ComponentName?) {
                isServiceBound = false
            }
        }

        bindService()
    }

    private fun getBatchMenuItems(): ArrayList<Pair<Int, Int>> {
        for (batch in adapterBatch?.getCurrentAppsList()!!) {
            if (batch.isSelected) {
                return BottomMenuConstants.getBatchSelectedMenu()
            }
        }

        return BottomMenuConstants.getBatchUnselectedMenu()
    }

    private fun initiateExtractProcess() {
        if (isServiceBound) {
            try {
                batchExtractService!!.setAppList(adapterBatch!!.getCurrentAppsList())
                childFragmentManager.showBatchExtract()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                showWarning("ERR: ${e.message}", goBack = false)
            }
        }
    }

    private fun bindService() {
        if (!isServiceBound) {
            val intent = BatchExtractService.newIntent(requireContext())
            requireContext().startService(intent)
            requireContext().bindService(intent, batchExtractServiceConnection!!, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        bindService()
    }

    override fun onPause() {
        super.onPause()
        if (isServiceBound) {
            try {
                requireContext().unbindService(batchExtractServiceConnection!!)
            } catch (e: IllegalStateException) {
                Log.d("Batch", "BatchExtractService not bound")
            } catch (e: IllegalArgumentException) {
                Log.e("Batch", "BatchExtractService not registered")
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BatchPreferences.MOVE_SELECTIONS_TO_TOP -> {
                adapterBatch?.moveSelectedItemsToTheTop()
            }

            BatchPreferences.HIGHLIGHT_SELECTED -> {
                adapterBatch?.updateSelectionsHighlights(BatchPreferences.isSelectedBatchHighlighted())
            }

            BatchPreferences.IS_SORTING_REVERSED,
            BatchPreferences.LIST_APPS_CATEGORY,
            BatchPreferences.SORT_STYLE,
            BatchPreferences.LIST_APPS_FILTER -> {
                batchViewModel.refresh()
            }
            BatchPreferences.LAST_SELECTED_PROFILE -> {
                Log.d("Batch", "Profile changed")
            }
        }
    }

    companion object {
        fun newInstance(loading: Boolean = false): Batch {
            val args = Bundle()
            val fragment = Batch()
            args.putBoolean(BundleConstants.loading, loading)
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Batch"
    }
}
