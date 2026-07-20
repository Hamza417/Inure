package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterRecentExits
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.RecentExitsViewModel

class RecentExits : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var progress: CustomProgressBar
    private lateinit var count: TypeFaceTextView

    private lateinit var recentExitsViewModel: RecentExitsViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_recent_exits, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        progress = view.findViewById(R.id.progress)
        count = view.findViewById(R.id.count)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        recentExitsViewModel = ViewModelProvider(this, packageInfoFactory)[RecentExitsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()
        count.text = getString(R.string.total, 0)

        recentExitsViewModel.getExitReasons().observe(viewLifecycleOwner) {
            progress.gone(animate = true)
            count.text = getString(R.string.total, it.size)
            val adapterRecentExits = AdapterRecentExits(it, packageInfo)
            recyclerView.adapter = adapterRecentExits
        }

        recentExitsViewModel.error.observe(viewLifecycleOwner) {
            showError(it, goBack = true)
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): RecentExits {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            val fragment = RecentExits()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "RecentExits"
    }
}
