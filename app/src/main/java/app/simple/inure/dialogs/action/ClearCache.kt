package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.ClearCacheViewModelFactory
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.viewmodels.dialogs.ClearCacheViewModel
import app.simple.inure.viewmodels.dialogs.ClearCacheViewModel.Companion.ClearCacheState
import kotlinx.coroutines.launch

class ClearCache : ScopedActionDialogBottomFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this, ClearCacheViewModelFactory(packageInfo))[ClearCacheViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ClearCacheState.Loading -> {
                        loader.start()
                    }
                    is ClearCacheState.Done -> {
                        loader.loaded()
                        if (state.clearedBytes > 0) {
                            status.text = buildString {
                                append(state.clearedBytes.toSize())
                                append(" ")
                                append(getString(R.string.cleared))
                            }
                        } else {
                            status.setText(R.string.no_cache_found)
                        }
                    }
                    is ClearCacheState.Failed -> {
                        loader.error()
                        status.setText(R.string.failed)
                    }
                }
            }
        }

        viewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): ClearCache {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            val fragment = ClearCache()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "ClearCache"
    }
}
