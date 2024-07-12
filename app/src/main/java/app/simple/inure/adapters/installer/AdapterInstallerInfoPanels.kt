package app.simple.inure.adapters.installer

import androidx.fragment.app.Fragment
import app.simple.inure.extensions.adapters.BaseFragmentStateAdapter
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.ui.installer.Certificate
import app.simple.inure.ui.installer.Changes
import app.simple.inure.ui.installer.Information
import app.simple.inure.ui.installer.Manifest
import app.simple.inure.ui.installer.Permissions
import app.simple.inure.ui.installer.Trackers
import java.io.File

class AdapterInstallerInfoPanels(fragment: Fragment, file: File, private val titles: Array<String>) : BaseFragmentStateAdapter(fragment) {

    private val fragments = arrayListOf<Fragment>().also {
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_INFO_VISIBLE)) {
            it.add(Information.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_CHANGES_VISIBLE)) {
            it.add(Changes.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_PERMISSIONS_VISIBLE)) {
            it.add(Permissions.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_MANIFEST_VISIBLE)) {
            it.add(Manifest.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_CERTIFICATE_VISIBLE)) {
            it.add(Certificate.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_TRACKERS_VISIBLE)) {
            it.add(Trackers.newInstance(file))
        }
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): String {
        return titles[position]
    }
}
