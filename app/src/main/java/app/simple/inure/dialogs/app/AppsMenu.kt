package app.simple.inure.dialogs.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.launchThisPackage
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.dialogs.action.Preparing
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.ui.viewers.*
import app.simple.inure.util.FragmentHelper

class AppsMenu : ScopedBottomSheetFragment() {

    private lateinit var copyPackageName: DynamicRippleTextView
    private lateinit var launch: DynamicRippleTextView
    private lateinit var appInformation: DynamicRippleTextView
    private lateinit var send: DynamicRippleTextView
    private lateinit var permissions: DynamicRippleTextView
    private lateinit var activities: DynamicRippleTextView
    private lateinit var services: DynamicRippleTextView
    private lateinit var receivers: DynamicRippleTextView
    private lateinit var providers: DynamicRippleTextView
    private lateinit var manifest: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_apps_menu, container, false)

        copyPackageName = view.findViewById(R.id.copy_package_name)
        launch = view.findViewById(R.id.launch)
        appInformation = view.findViewById(R.id.app_information)
        send = view.findViewById(R.id.send)
        permissions = view.findViewById(R.id.permissions)
        activities = view.findViewById(R.id.activities)
        services = view.findViewById(R.id.services)
        receivers = view.findViewById(R.id.receivers)
        providers = view.findViewById(R.id.providers)
        manifest = view.findViewById(R.id.manifest)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        copyPackageName.setOnClickListener {
            val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Package Name", packageInfo.packageName)
            clipBoard.setPrimaryClip(clipData)
            Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_SHORT).show()
            dismiss()
        }

        launch.setOnClickListener {
            packageInfo.launchThisPackage(requireContext())
        }

        appInformation.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Information.newInstance(packageInfo),
                                        "information")
        }

        send.setOnClickListener {
            Preparing.newInstance(packageInfo)
                .show(childFragmentManager, "send")
        }

        permissions.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Permissions.newInstance(packageInfo),
                                        "permissions")
        }

        activities.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Activities.newInstance(packageInfo),
                                        "activities")
        }

        services.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Services.newInstance(packageInfo),
                                        "services")
        }

        receivers.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Receivers.newInstance(packageInfo),
                                        "receivers")
        }

        providers.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Providers.newInstance(packageInfo),
                                        "providers")
        }

        manifest.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        XMLViewerTextView.newInstance(packageInfo, true, "AndroidManifest.xmlŪ"),
                                        "information")
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): AppsMenu {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = AppsMenu()
            fragment.arguments = args
            return fragment
        }
    }
}