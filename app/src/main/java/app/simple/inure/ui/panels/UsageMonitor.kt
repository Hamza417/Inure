package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterPermissionMonitor
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.app.AppMenu
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.UsageMonitorPreferences
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.panels.UsageMonitorViewModel

class UsageMonitor : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var usageMonitorViewModel: UsageMonitorViewModel
    private var adapterPermissionMonitor: AdapterPermissionMonitor? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_usage_monitor, container, false)

        recyclerView = view.findViewById(R.id.usage_monitor_recycler_view)
        options = view.findViewById(R.id.usage_monitor_option_btn)
        search = view.findViewById(R.id.usage_monitor_search_btn)
        searchBox = view.findViewById(R.id.usage_monitor_search)
        title = view.findViewById(R.id.usage_monitor_title)

        usageMonitorViewModel = ViewModelProvider(this)[UsageMonitorViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        fullVersionCheck()
        searchBoxState(false, UsageMonitorPreferences.isSearchVisible())

        usageMonitorViewModel.getPermissionUsageData().observe(viewLifecycleOwner) { permissionUsages ->
            adapterPermissionMonitor = AdapterPermissionMonitor(permissionUsages, searchBox.text.toString().trim())
            setCount(permissionUsages.size)

            adapterPermissionMonitor?.setOnPermissionMonitorCallbackListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            recyclerView.adapter = adapterPermissionMonitor

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            if (permissionUsages.isEmpty()) {
                showWarning(R.string.no_permission_usage_found)
            }
        }

        usageMonitorViewModel.getIsServiceRunning().observe(viewLifecycleOwner) { isRunning ->
            // Update UI based on service state if needed
        }

        usageMonitorViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it, false)
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                usageMonitorViewModel.loadPermissionUsageData(text.toString().trim())
            }
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                UsageMonitorPreferences.setSearchVisibility(!UsageMonitorPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }

        options.setOnClickListener {
            // TODO: Show options menu
        }
    }

    override fun onResume() {
        super.onResume()
        // Start live monitoring when panel is visible (if not already running)
        if (usageMonitorViewModel.getIsServiceRunning().value != true) {
            usageMonitorViewModel.startMonitoring()
        }
    }

    override fun onPause() {
        super.onPause()
        // Service continues running in background
        // Only stop when fragment is destroyed or user explicitly stops it
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop the service here - let it run as a foreground service
        // User can stop it manually via the options menu or it will stop when app is fully closed
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            UsageMonitorPreferences.USAGE_MONITOR_SEARCH -> {
                searchBoxState(true, UsageMonitorPreferences.isSearchVisible())
            }
            UsageMonitorPreferences.USAGE_MONITOR_EXCLUDE_SYSTEM,
            UsageMonitorPreferences.USAGE_MONITOR_FILTER_PERMISSION,
            UsageMonitorPreferences.USAGE_MONITOR_SHOW_INACTIVE,
            UsageMonitorPreferences.USAGE_MONITOR_TIME_THRESHOLD -> {
                usageMonitorViewModel.loadPermissionUsageData(searchBox.text.toString().trim())
            }
        }
    }

    companion object {
        fun newInstance(): UsageMonitor {
            val args = Bundle()
            val fragment = UsageMonitor()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "usage_monitor"
    }
}
