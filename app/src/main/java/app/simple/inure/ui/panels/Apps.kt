package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterAppsDetailed
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.apps.AllAppsMenu.Companion.newAppsMenuInstance
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.popups.apps.PopupSortingStyle
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.viewers.XMLViewerTextView
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.panels.AppsViewModel

class Apps : ScopedFragment() {

    private lateinit var appsListRecyclerView: CustomVerticalRecyclerView
    private lateinit var adapter: AdapterAppsDetailed
    private lateinit var model: AppsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_apps, container, false)

        appsListRecyclerView = view.findViewById(R.id.all_apps_recycler_view)

        model = ViewModelProvider(requireActivity())[AppsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoader()

        model.getAppData().observe(viewLifecycleOwner) { it ->
            postponeEnterTransition()
            hideLoader()

            adapter = AdapterAppsDetailed()
            adapter.apps = it

            appsListRecyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapter.setOnItemClickListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openFragmentArc(AppInfo.newInstance(packageInfo, icon.transitionName), icon, "app_info")
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }

                override fun onSearchPressed(view: View) {
                    openFragmentSlide(Search.newInstance(true), "search")
                }

                override fun onFilterPressed(view: View) {
                    PopupAppsCategory(view)
                }

                override fun onSortPressed(view: View) {
                    PopupSortingStyle(view)
                }

                override fun onSettingsPressed(view: View) {
                    childFragmentManager.newAppsMenuInstance().setOnGenerateListClicked {
                        showLoader(manualOverride = true)
                        model.generateAllAppsTXTFile()

                        model.getGeneratedAppData().observe(viewLifecycleOwner) {
                            if (it.isNotNull()) {
                                hideLoader()
                                openFragmentSlide(XMLViewerTextView
                                                      .newInstance(packageInfo = PackageInfo(), /* Empty package info */
                                                                   isManifest = false,
                                                                   pathToXml = it,
                                                                   isRaw = true), "xml_viewer")
                                model.clearGeneratedAppsDataLiveData()
                            }
                        }
                    }
                }
            })
        }

        model.appLoaded.observe(viewLifecycleOwner) { appsEvent ->
            appsEvent.getContentIfNotHandledOrReturnNull()?.let {
                Log.d("Apps", if (it) "Apps Loaded" else "Failed")
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.sortStyle,
            MainPreferences.isSortingReversed,
            MainPreferences.listAppsCategory -> {
                model.loadAppData()
            }
        }
    }

    companion object {
        fun newInstance(loading: Boolean = false): Apps {
            val args = Bundle()
            val fragment = Apps()
            args.putBoolean(BundleConstants.loading, loading)
            fragment.arguments = args
            return fragment
        }
    }
}