package app.simple.inure.dialogs.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.InstallerPreferences

class InstallerOptions : ScopedBottomSheetFragment() {

    private lateinit var packagename: DynamicCornerEditText
    private lateinit var grantRuntimePermissions: CheckBox
    private lateinit var versionCodeDowngrade: CheckBox
    private lateinit var testPackages: CheckBox
    private lateinit var bypassTargetSDK: CheckBox
    private lateinit var replaceExisting: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_installer_options, container, false)

        packagename = view.findViewById(R.id.installer_package_name)
        grantRuntimePermissions = view.findViewById(R.id.grant_all_permissions)
        versionCodeDowngrade = view.findViewById(R.id.allow_downgrade)
        testPackages = view.findViewById(R.id.allow_test_packages)
        bypassTargetSDK = view.findViewById(R.id.bypass_target_sdk)
        replaceExisting = view.findViewById(R.id.replace_existing)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        packagename.setText(InstallerPreferences.getInstallerPackageName(requireContext()))
        grantRuntimePermissions.isChecked = InstallerPreferences.isGrantRuntimePermissions()
        versionCodeDowngrade.isChecked = InstallerPreferences.isVersionCodeDowngrade()
        testPackages.isChecked = InstallerPreferences.isTestPackages()
        bypassTargetSDK.isChecked = InstallerPreferences.isBypassLowTargetSdk()
        replaceExisting.isChecked = InstallerPreferences.isReplaceExisting()

        packagename.doOnTextChanged { text, _, _, _ ->
            InstallerPreferences.setInstallerPackageName(text.toString())
        }

        grantRuntimePermissions.setOnCheckedChangeListener { isChecked ->
            InstallerPreferences.setGrantRuntimePermissions(isChecked)
        }

        versionCodeDowngrade.setOnCheckedChangeListener { isChecked ->
            InstallerPreferences.setVersionCodeDowngrade(isChecked)
        }

        testPackages.setOnCheckedChangeListener { isChecked ->
            InstallerPreferences.setTestPackages(isChecked)
        }

        bypassTargetSDK.setOnCheckedChangeListener { isChecked ->
            InstallerPreferences.setBypassLowTargetSdk(isChecked)
        }

        replaceExisting.setOnCheckedChangeListener { isChecked ->
            InstallerPreferences.setReplaceExisting(isChecked)
        }
    }

    companion object {
        fun newInstance(): InstallerOptions {
            val args = Bundle()
            val fragment = InstallerOptions()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showInstallerOptions(): InstallerOptions {
            val fragment = newInstance()
            fragment.show(this, TAG)
            return fragment
        }

        const val TAG = "InstallerOptions"
    }
}