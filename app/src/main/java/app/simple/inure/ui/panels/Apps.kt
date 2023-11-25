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
import app.simple.inure.adapters.ui.AdapterApps
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.apps.AppsPanelMenu.Companion.newAppsMenuInstance
import app.simple.inure.dialogs.apps.AppsSort.Companion.showAppsSortDialog
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.dialogs.miscellaneous.GenerateAppData.Companion.showGeneratedDataTypeSelector
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.ui.viewers.HtmlViewer
import app.simple.inure.ui.viewers.JSON
import app.simple.inure.ui.viewers.Markdown
import app.simple.inure.ui.viewers.XMLViewerTextView
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.StringUtils.endsWithAny
import app.simple.inure.viewmodels.panels.AppsViewModel

class Apps : ScopedFragment() {

    private lateinit var appsListRecyclerView: CustomVerticalRecyclerView
    private lateinit var adapter: AdapterApps
    private lateinit var appsViewModel: AppsViewModel

    private val generatedDataTextExtension = arrayOf(".xml", ".txt", ".csv")
    private val markdownExtensions = arrayOf(".md", ".markdown")
    private val jsonExtension = ".json"
    private val htmlExtension = ".html"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_apps, container, false)

        appsListRecyclerView = view.findViewById(R.id.all_apps_recycler_view)
        appsViewModel = ViewModelProvider(requireActivity())[AppsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoader()
        postponeEnterTransition()

        appsViewModel.getAppData().observe(viewLifecycleOwner) { it ->
            postponeEnterTransition()
            hideLoader()

            adapter = AdapterApps(it)
            appsListRecyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapter.setOnItemClickListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openFragmentArc(AppInfo.newInstance(packageInfo), icon, "app_info")
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getAllAppsBottomMenuItems(), appsListRecyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_filter -> {
                        childFragmentManager.showAppsSortDialog()
                    }

                    R.drawable.ic_settings -> {
                        childFragmentManager.newAppsMenuInstance().setOnGenerateListClicked {
                            if ((appsViewModel.getAppData().value ?: it).isNotEmpty()) {
                                childFragmentManager.showGeneratedDataTypeSelector().onGenerateData {
                                    showLoader(manualOverride = true)
                                    kotlin.runCatching {
                                        appsViewModel.generateAppsData(appsViewModel.getAppData().value ?: it)

                                    }.onFailure {
                                        showWarning(it.message ?: "Failed to generate data", goBack = false)
                                    }
                                }
                            } else {
                                showWarning("ERR: empty list", goBack = false)
                            }
                        }
                    }

                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), "search")
                    }

                    R.drawable.ic_refresh -> {
                        showLoader(manualOverride = true)
                        appsViewModel.refreshPackageData()
                    }
                }
            }
        }

        appsViewModel.getGeneratedDataPath().observe(viewLifecycleOwner) { path ->
            if (path.isNotNull()) {
                hideLoader()

                /**
                 * We are using empty [PackageInfo] object because we don't want to show
                 * any app info in the toolbar, it's a file viewer for data generated by
                 * the app itself.
                 *
                 * isRaw = true because we are using the viewer for special purpose
                 * of viewing the generated data
                 */
                when {
                    path.endsWithAny(*generatedDataTextExtension) -> {
                        openFragmentSlide(XMLViewerTextView.newInstance(
                                PackageInfo(), false, path, true), "xml_viewer")
                    }

                    path.endsWith(htmlExtension) -> {
                        openFragmentSlide(HtmlViewer.newInstance(
                                PackageInfo(), path, true), "web_page")
                    }

                    path.endsWith((jsonExtension)) -> {
                        openFragmentSlide(JSON.newInstance(
                                PackageInfo(), path, true), "json_viewer")
                    }

                    path.endsWithAny(*markdownExtensions) -> {
                        openFragmentSlide(Markdown.newInstance(
                                PackageInfo(), path, true), "markdown_viewer")
                    }
                }

                appsViewModel.clearGeneratedAppsDataLiveData()
            } else {
                hideLoader()
            }
        }

        appsViewModel.appLoaded.observe(viewLifecycleOwner) { appsEvent ->
            appsEvent.getContentIfNotHandledOrReturnNull()?.let {
                // Log.d("Apps", if (it) "Apps Loaded" else "Failed")
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppsPreferences.sortStyle,
            AppsPreferences.isSortingReversed,
            AppsPreferences.appsCategory,
            AppsPreferences.appsFilter,
            AppsPreferences.combineFilter,
            AppsPreferences.appsType -> {
                appsViewModel.loadAppData()
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