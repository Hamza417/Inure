package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.ActivityLaunchFactory
import app.simple.inure.viewmodels.dialogs.ActivityLauncherViewModel

class ActivityLauncher : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var status: TypeFaceTextView

    private lateinit var activityLaunchFactory: ActivityLaunchFactory
    private lateinit var activityLauncherViewModel: ActivityLauncherViewModel

    private var packageId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dalog_activity_launcher, container, false)

        loader = view.findViewById(R.id.loader)
        status = view.findViewById(R.id.activity_launcher_result)

        packageId = requireArguments().getString(BundleConstants.packageId)!!

        activityLaunchFactory = ActivityLaunchFactory(packageInfo, packageId!!)
        activityLauncherViewModel = ViewModelProvider(this, activityLaunchFactory)[ActivityLauncherViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }
}