package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBootManager
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.boot.BootManagerSort.Companion.showBootManagerSort
import app.simple.inure.dialogs.bootmanager.BootComponentSelector
import app.simple.inure.dialogs.bootmanager.BootComponentSelector.Companion.showBootComponentSelector
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.BootManagerModel
import app.simple.inure.popups.bootmanager.PopupBootManager
import app.simple.inure.preferences.BootManagerPreferences
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.panels.BootManagerViewModel

class BootManager : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterBootManager: AdapterBootManager? = null
    private var bootManagerViewModel: BootManagerViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_boot_manager, container, false)

        recyclerView = view.findViewById(R.id.boot_manager_recycler_view)
        bootManagerViewModel = ViewModelProvider(requireActivity())[BootManagerViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()

        bootManagerViewModel?.getBootComponentData()?.observe(viewLifecycleOwner) { bootComponentData ->
            hideLoader()
            postponeEnterTransition()

            adapterBootManager = AdapterBootManager(bootComponentData)

            adapterBootManager?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onBootComponentClicked(view: View, bootManagerModel: BootManagerModel, position: Int, icon: ImageView) {
                    childFragmentManager.showBootComponentSelector(bootManagerModel)
                        .setBootComponentSelectorCallbacks(object : BootComponentSelector.Companion.BootComponentSelectorCallbacks {
                            override fun onBootSelected(selectedSet: Set<String>, enable: Boolean) {
                                showLoader(manualOverride = true).also {
                                    if (enable) {
                                        bootManagerViewModel?.enableComponents(selectedSet, bootManagerModel.copy(), position)
                                    } else {
                                        bootManagerViewModel?.disableComponents(selectedSet, bootManagerModel.copy(), position)
                                    }
                                }
                            }
                        })
                }

                override fun onBootComponentLongClicked(view: View, bootManagerModel: BootManagerModel, position: Int, icon: ImageView) {
                    PopupBootManager(requireView()).setOnPopupBootManagerCallbacks(object : PopupBootManager.Companion.PopupBootManagerCallbacks {
                        override fun onEnableAllClicked() {
                            showLoader(manualOverride = true).also {
                                bootManagerViewModel?.enableAllComponents(bootManagerModel.copy(), position)
                            }
                        }

                        override fun onDisableAllClicked() {
                            showLoader(manualOverride = true).also {
                                bootManagerViewModel?.disableAllComponents(bootManagerModel.copy(), position)
                            }
                        }

                        override fun onOpenClicked() {
                            openFragmentArc(
                                    AppInfo.newInstance(
                                            requirePackageManager()
                                                .getPackageInfo(bootManagerModel.packageInfo.packageName)!!), icon, "app_info")
                        }
                    })
                }
            })

            recyclerView.adapter = adapterBootManager

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getBootManagerBottomMenuItems(), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), Preferences.TAG)
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(firstLaunch = true), Search.TAG)
                    }
                    R.drawable.ic_filter -> {
                        childFragmentManager.showBootManagerSort()
                    }
                    R.drawable.ic_refresh -> {
                        showLoader(manualOverride = true).also {
                            bootManagerViewModel?.reloadBootComponentData()
                        }
                    }
                }
            }
        }

        bootManagerViewModel?.getBootManagerModelData()?.observe(viewLifecycleOwner) {
            if (it.isNotNull()) {
                adapterBootManager?.updateItem(it.first, it.second)
                bootManagerViewModel?.clearBootManagerModelData()
                hideLoader()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BootManagerPreferences.appsCategory,
            BootManagerPreferences.filter -> {
                bootManagerViewModel?.reloadBootComponentData()
            }
            BootManagerPreferences.sortingStyle,
            BootManagerPreferences.sortingReversed -> {
                bootManagerViewModel?.sortBootComponentData()
            }
        }
    }

    companion object {
        fun newInstance(): BootManager {
            val args = Bundle()
            val fragment = BootManager()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "BootManager"
    }
}
