package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.ActivityLaunchFactory
import app.simple.inure.viewmodels.dialogs.ActivityLauncherViewModel

class ActivityLauncher : ScopedActionDialogBottomFragment() {

    private lateinit var activityLaunchFactory: ActivityLaunchFactory
    private lateinit var activityLauncherViewModel: ActivityLauncherViewModel

    private var packageId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        packageId = requireArguments().getString(BundleConstants.packageId)!!

        activityLaunchFactory = ActivityLaunchFactory(packageInfo, packageId!!)
        activityLauncherViewModel = ViewModelProvider(this, activityLaunchFactory)[ActivityLauncherViewModel::class.java]

        activityLauncherViewModel.getSuccessStatus().observe(viewLifecycleOwner) {
            when (it) {
                "Done" -> {
                    loader.loaded()
                    status.setText(R.string.launched)
                }
                "Failed" -> {
                    loader.error()
                    status.setText(R.string.failed)
                }
            }
        }

        activityLauncherViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, packageId: String): ActivityLauncher {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.packageId, packageId)
            val fragment = ActivityLauncher()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "ActivityLauncher"
    }
}
