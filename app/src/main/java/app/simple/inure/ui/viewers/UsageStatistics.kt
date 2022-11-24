package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterAppUsageStats
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.miscellaneous.UsageStatsPermission
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.AppStatisticsViewModelFactory
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.AppStatisticsViewModel

class UsageStatistics : ScopedFragment() {

    private lateinit var back: DynamicRippleImageButton
    private lateinit var loader: CustomProgressBar
    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var appStatisticsViewModel: AppStatisticsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_app_statistics, container, false)

        recyclerView = view.findViewById(R.id.usage_recycler_view)
        loader = view.findViewById(R.id.loader)
        back = view.findViewById(R.id.app_info_back_button)

        appStatisticsViewModel = ViewModelProvider(this, AppStatisticsViewModelFactory(packageInfo))[AppStatisticsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullVersionCheck()
        startPostponedEnterTransition()
        doPermissionChecks()

        back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun doPermissionChecks() {
        if (requireContext().checkForUsageAccessPermission()) {
            observeData()
        } else {
            val dialog = UsageStatsPermission.newInstance()

            dialog.setOnUsageStatsPermissionCallbackListener(object : UsageStatsPermission.Companion.UsageStatsPermissionCallbacks {
                override fun onClosedAfterGrant() {
                    observeData()
                }
            })

            dialog.show(childFragmentManager, "usage_stats_permission")
        }
    }

    private fun observeData() {
        appStatisticsViewModel.getUsageData().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            val adapterAppUsageStats = AdapterAppUsageStats(it)
            recyclerView.adapter = adapterAppUsageStats
        }

        appStatisticsViewModel.getError().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            showError(it)
        }

        appStatisticsViewModel.warning.observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            showWarning(it)
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): UsageStatistics {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            val fragment = UsageStatistics()
            fragment.arguments = args
            return fragment
        }
    }
}
