package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBatch
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeDivider
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.batch.BatchMenu
import app.simple.inure.dialogs.batch.BatchUninstaller
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.dialogs.miscellaneous.StoragePermission
import app.simple.inure.dialogs.miscellaneous.StoragePermission.Companion.newStoragePermissionInstance
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.interfaces.menus.BottomMenuCallbacks
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.popups.batch.PopupBatchAppsCategory
import app.simple.inure.popups.batch.PopupBatchSortingStyle
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.actions.BatchExtract.Companion.showBatchExtract
import app.simple.inure.ui.subpanels.BatchSelectedApps
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.PermissionUtils.checkStoragePermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.BatchViewModel

class Batch : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var divider: ThemeDivider
    private lateinit var batchMenuContainer: DynamicCornerLinearLayout
    private lateinit var delete: DynamicRippleImageButton
    private lateinit var send: DynamicRippleImageButton
    private lateinit var extract: DynamicRippleImageButton
    private lateinit var checklist: DynamicRippleImageButton

    private var adapterBatch: AdapterBatch? = null
    private lateinit var batchViewModel: BatchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_batch, container, false)

        recyclerView = view.findViewById(R.id.batch_recycler_view)
        divider = view.findViewById(R.id.divider)
        batchMenuContainer = view.findViewById(R.id.batch_menu_container)
        delete = view.findViewById(R.id.delete)
        send = view.findViewById(R.id.send)
        extract = view.findViewById(R.id.extract)
        checklist = view.findViewById(R.id.menu)

        batchViewModel = ViewModelProvider(requireActivity())[BatchViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullVersionCheck()
        showLoader()

        batchViewModel.getAppData().observe(viewLifecycleOwner) {
            adapterBatch = AdapterBatch(it)

            batchMenuState(it)
            hideLoader()

            adapterBatch?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openFragmentArc(AppInfo.newInstance(packageInfo, icon.transitionName), icon, "app_info")
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }

                override fun onBatchChanged(batchPackageInfo: BatchPackageInfo) {
                    batchViewModel.updateBatchItem(batchPackageInfo)
                    batchMenuState(adapterBatch!!.getCurrentAppsList())
                }
            })

            recyclerView.adapter = adapterBatch

            bottomMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getAllAppsBottomMenuItems(), recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_sort -> {
                        PopupBatchSortingStyle(view)
                    }
                    R.drawable.ic_filter -> {
                        PopupBatchAppsCategory(view)
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), "search")
                    }
                    R.drawable.ic_settings -> {
                        BatchMenu.newInstance()
                            .show(childFragmentManager, "batch_menu")
                    }
                }
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        delete.setOnClickListener {
            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                override fun onSure() {
                    BatchUninstaller.newInstance(adapterBatch!!.getCurrentAppsList())
                        .show(childFragmentManager, "batch_uninstaller")
                }
            })
        }

        send.setOnClickListener {

        }

        extract.setOnClickListener {
            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                override fun onSure() {
                    if (requireContext().checkStoragePermission()) {
                        childFragmentManager.showBatchExtract(adapterBatch?.getCurrentAppsList()!!)
                    } else {
                        childFragmentManager.newStoragePermissionInstance().setStoragePermissionCallbacks(object : StoragePermission.Companion.StoragePermissionCallbacks {
                            override fun onStoragePermissionGranted() {
                                childFragmentManager.showBatchExtract(adapterBatch?.getCurrentAppsList()!!)
                            }
                        })
                    }
                }
            })
        }

        checklist.setOnClickListener {
            openFragmentSlide(BatchSelectedApps.newInstance(), "batch_selected_apps")
        }
    }

    private fun batchMenuState(arrayList: ArrayList<BatchPackageInfo>) {
        for (batch in arrayList) {
            if (batch.isSelected) {
                batchMenuContainer.visible(false)
                divider.visible(animate = false)
                return
            }
        }

        batchMenuContainer.gone()
        divider.gone()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BatchPreferences.moveSelectionTop -> {
                adapterBatch?.moveSelectedItemsToTheTop()
            }
            BatchPreferences.highlightSelected -> {
                adapterBatch?.updateSelectionsHighlights(BatchPreferences.isSelectedBatchHighlighted())
            }
            BatchPreferences.isSortingReversed,
            BatchPreferences.listAppsCategory,
            BatchPreferences.sortStyle -> {
                batchViewModel.refresh()
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
    }
}