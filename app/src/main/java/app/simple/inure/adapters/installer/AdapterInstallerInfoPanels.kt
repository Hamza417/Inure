package app.simple.inure.adapters.installer

import androidx.fragment.app.Fragment
import app.simple.inure.extensions.adapters.BaseFragmentStateAdapter
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.ui.installer.*
import java.io.File

class AdapterInstallerInfoPanels(fragment: Fragment, file: File, private val titles: Array<String>) : BaseFragmentStateAdapter(fragment) {

    private val fragments = arrayListOf<Fragment>().also {
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.isInfoVisible)) {
            it.add(Information.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.isChangesVisible)) {
            it.add(Changes.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.isPermissionsVisible)) {
            it.add(Permissions.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.isManifestVisible)) {
            it.add(Manifest.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.isCertificateVisible)) {
            it.add(Certificate.newInstance(file))
        }
        if (InstallerPreferences.getPanelVisibility(InstallerPreferences.isTrackersVisible)) {
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