package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedFragment

class VirusTotal : ScopedFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showWarning("This feature is not available in your build.")
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): VirusTotal {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = VirusTotal()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "VirusTotal"
    }
}