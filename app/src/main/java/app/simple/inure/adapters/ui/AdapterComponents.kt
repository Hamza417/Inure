package app.simple.inure.adapters.ui

import android.content.pm.PackageInfo
import androidx.fragment.app.Fragment
import app.simple.inure.extensions.adapters.BaseFragmentStateAdapter
import app.simple.inure.ui.viewers.Activities
import app.simple.inure.ui.viewers.Certificate
import app.simple.inure.ui.viewers.Permissions
import app.simple.inure.ui.viewers.Providers
import app.simple.inure.ui.viewers.Receivers
import app.simple.inure.ui.viewers.Services

class AdapterComponents(fragment: Fragment, packageInfo: PackageInfo, private val titles: Array<String>) : BaseFragmentStateAdapter(fragment) {

    private val fragments = arrayListOf<Fragment>().also {
        it.add(Permissions.newInstance(packageInfo))
        it.add(Activities.newInstance(packageInfo))
        it.add(Services.newInstance(packageInfo))
        it.add(Receivers.newInstance(packageInfo))
        it.add(Providers.newInstance(packageInfo))
        it.add(Certificate.newInstance(packageInfo, null))
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