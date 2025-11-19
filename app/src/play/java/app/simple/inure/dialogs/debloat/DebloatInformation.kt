package app.simple.inure.dialogs.debloat

import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

@Suppress("unused")
class DebloatInformation : ScopedBottomSheetFragment() {

    fun setDebloatInfoListener(listener: DebloatInfoListener) {
        /* no-op */
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): DebloatInformation {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = DebloatInformation()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showDebloatInfoDialog(packageInfo: PackageInfo): DebloatInformation {
            val dialog = newInstance(packageInfo)
            dialog.show(this, TAG)
            return dialog
        }

        interface DebloatInfoListener {
            fun onUninstallRequested()
            fun onDisableRequested()
        }

        private const val TAG = "DebloatInformation"
    }
}