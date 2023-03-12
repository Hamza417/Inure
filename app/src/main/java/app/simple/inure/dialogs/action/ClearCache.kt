package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.ClearCacheViewModelFactory
import app.simple.inure.viewmodels.dialogs.ClearCacheViewModel

class ClearCache : ScopedActionDialogBottomFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(ViewModelProvider(this, ClearCacheViewModelFactory(packageInfo))[ClearCacheViewModel::class.java]) {
            getResults().observe(viewLifecycleOwner) {

            }

            getSuccessStatus().observe(viewLifecycleOwner) {
                when (it) {
                    "Done" -> {
                        loader.loaded()
                        status.setText(R.string.cleared)
                    }
                    "Failed" -> {
                        loader.error()
                        status.setText(R.string.failed)
                    }
                }
            }

            getWarning().observe(viewLifecycleOwner) {
                showWarning(it)
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): ClearCache {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = ClearCache()
            fragment.arguments = args
            return fragment
        }
    }
}