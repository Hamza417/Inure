package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginTop
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBatch
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.batch.DialogBatchSelectedApps
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.dialogs.menus.BatchMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.ui.app.AppInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.BatchViewModel

class Batch : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
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

        batchMenuContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.apply {
                    setPadding(paddingLeft,
                               paddingTop,
                               paddingRight,
                               batchMenuContainer.measuredHeight +
                                       batchMenuContainer.marginTop +
                                       paddingBottom)
                }

                batchMenuContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        batchViewModel.getAppData().observe(viewLifecycleOwner) {
            adapterBatch = AdapterBatch(it)

            adapterBatch?.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                AppInfo.newInstance(packageInfo, icon.transitionName),
                                                icon, "app_info")
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }

                override fun onSearchPressed(view: View) {
                    clearTransitions()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                Search.newInstance(true),
                                                "search")
                }

                override fun onSettingsPressed(view: View) {
                    BatchMenu.newInstance()
                        .show(childFragmentManager, "batch_menu")
                }

                override fun onBatchChanged(batchPackageInfo: BatchPackageInfo) {
                    batchViewModel.updateBatchItem(batchPackageInfo)
                }
            })

            recyclerView.adapter = adapterBatch

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        delete.setOnClickListener {

        }

        send.setOnClickListener {

        }

        extract.setOnClickListener {

        }

        checklist.setOnClickListener {
            val d = DialogBatchSelectedApps.newInstance(adapterBatch!!.getCurrentAppsList())

            d.setOnBatchSelectedAppsCallbacks(object : DialogBatchSelectedApps.Companion.BatchSelectedAppsCallbacks {
                override fun onBatchChanged(batchPackageInfo: BatchPackageInfo) {
                    adapterBatch?.updateBatchItem(batchPackageInfo)
                }
            })

            d.show(childFragmentManager, "batch_selected_apps")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BatchPreferences.moveSelectionTop -> {
                adapterBatch?.moveSelectedItemsToTheTop()
            }
            BatchPreferences.highlightSelected -> {
                adapterBatch?.updateSelectionsHighlights(BatchPreferences.isSelectedBatchHighlighted())
            }
        }
    }

    companion object {
        fun newInstance(): Batch {
            val args = Bundle()
            val fragment = Batch()
            fragment.arguments = args
            return fragment
        }
    }
}