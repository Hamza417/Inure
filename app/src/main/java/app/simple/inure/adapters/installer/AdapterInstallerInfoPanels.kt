package app.simple.inure.adapters.installer

import androidx.fragment.app.Fragment
import app.simple.inure.extensions.adapters.BaseFragmentStateAdapter
import app.simple.inure.ui.installer.*
import java.io.File

class AdapterInstallerInfoPanels(fragment: Fragment, file: File, private val titles: Array<String>) : BaseFragmentStateAdapter(fragment) {

    private val fragments = arrayListOf(
            Information.newInstance(file),
            Permissions.newInstance(file),
            Manifest.newInstance(file),
            Certificate.newInstance(file),
            Trackers.newInstance(file),
    )

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