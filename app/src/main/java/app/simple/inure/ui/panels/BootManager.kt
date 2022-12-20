package app.simple.inure.ui.panels

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
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.BootManagerModel
import app.simple.inure.popups.bootmanager.PopupBootManager
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
            postponeEnterTransition()

            adapterBootManager = AdapterBootManager(bootComponentData)

            adapterBootManager?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onBootComponentClicked(view: View, bootManagerModel: BootManagerModel, position: Int, icon: ImageView) {
                    PopupBootManager(view).setOnPopupBootManagerCallbacks(object : PopupBootManager.Companion.PopupBootManagerCallbacks {
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
                            openFragmentArc(AppInfo.newInstance(requirePackageManager().getPackageInfo(bootManagerModel.packageName)!!,
                                                                icon.transitionName), icon, "app_info")
                        }
                    })
                }

                override fun onBootComponentLongClicked(view: View, bootManagerModel: BootManagerModel, position: Int, icon: ImageView) {
                    AppsMenu.newInstance(requirePackageManager().getPackageInfo(bootManagerModel.packageName)!!)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            recyclerView.adapter = adapterBootManager

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getBootManagerBottomMenuItems(), recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), "preferences")
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(firstLaunch = true), "search")
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

    companion object {
        fun newInstance(): BootManager {
            val args = Bundle()
            val fragment = BootManager()
            fragment.arguments = args
            return fragment
        }
    }
}