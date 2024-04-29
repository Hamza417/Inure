package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.ReinstallerViewModelFactory
import app.simple.inure.viewmodels.dialogs.ReinstallerViewModel

class Reinstaller : ScopedActionDialogBottomFragment() {

    private lateinit var reinstallerViewModel: ReinstallerViewModel
    private var reinstallerCallbacks: ReinstallerCallbacks? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        status.setText(R.string.reinstalling)

        reinstallerViewModel = ViewModelProvider(this, ReinstallerViewModelFactory(packageInfo))[ReinstallerViewModel::class.java]

        reinstallerViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        reinstallerViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }

        reinstallerViewModel.getSuccessStatus().observe(viewLifecycleOwner) {
            when (it) {
                "Done" -> {
                    loader.loaded()
                    status.setText(R.string.done)

                    reinstallerCallbacks?.onReinstallSuccess()
                }
                "Failed" -> {
                    loader.loaded()
                    status.setText(R.string.failed)
                }
            }
        }
    }

    fun setReinstallerCallbacks(reinstallerCallbacks: ReinstallerCallbacks) {
        this.reinstallerCallbacks = reinstallerCallbacks
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Reinstaller {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Reinstaller()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showReinstaller(packageInfo: PackageInfo): Reinstaller {
            val reinstaller = newInstance(packageInfo)
            reinstaller.show(this, reinstaller.tag)
            return reinstaller
        }

        interface ReinstallerCallbacks {
            fun onReinstallSuccess()
        }
    }
}
