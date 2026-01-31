package app.simple.inure.dialogs.appinfo

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.MarketUtils

class FdroidStores : ScopedBottomSheetFragment() {

    private lateinit var fdroid: DynamicRippleTextView
    private lateinit var izzyOnDroid: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_fdroid_stores, container, false)

        fdroid = view.findViewById(R.id.fdroid)
        izzyOnDroid = view.findViewById(R.id.izzyondroid)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fdroid.setOnClickListener {
            MarketUtils.openAppOnFdroid(requireContext(), packageInfo.packageName)
        }

        izzyOnDroid.setOnClickListener {
            "https://apt.izzysoft.de/fdroid/index/apk/${packageInfo.packageName}"
                .asUri()
                .openInBrowser(requireContext())

        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): FdroidStores {
            val args = Bundle()
            val fragment = FdroidStores()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showFdroidStores(packageInfo: PackageInfo) {
            newInstance(packageInfo)
                .show(this, "fdroid_stores")
        }
    }
}