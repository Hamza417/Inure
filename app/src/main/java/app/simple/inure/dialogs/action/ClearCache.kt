package app.simple.inure.dialogs.action

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getPackageSize
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.ClearCacheViewModelFactory
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.viewmodels.dialogs.ClearCacheViewModel

class ClearCache : ScopedActionDialogBottomFragment() {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val size = getCacheSize()
        Log.i(TAG, "Cache Size: ${size.toSize()} for ${packageInfo.packageName}")

        with(ViewModelProvider(this, ClearCacheViewModelFactory(packageInfo))[ClearCacheViewModel::class.java]) {
            getResults().observe(viewLifecycleOwner) {

            }

            getSuccessStatus().observe(viewLifecycleOwner) {
                when (it) {
                    "Done" -> {
                        loader.loaded()
                        val sizeNow = getCacheSize()
                        val postSize = size - sizeNow
                        if (postSize > 0) {
                            status.text = postSize.toSize() + " " + getString(R.string.cleared)
                        } else {
                            status.setText(R.string.no_cache_found)
                        }
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

    private fun getCacheSize(): Long {
        return with(packageInfo.getPackageSize(requireContext())) {
            cacheSize
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

        const val TAG = "ClearCache"
    }
}
