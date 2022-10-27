package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterSharedLibs
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.SharedLibrariesViewModel

class SharedLibs : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var sharedLibrariesViewModel: SharedLibrariesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_shared_libs, container, false)

        recyclerView = view.findViewById(R.id.shared_libs_recycler_view)

        val factory = PackageInfoFactory(packageInfo)
        sharedLibrariesViewModel = ViewModelProvider(this, factory)[SharedLibrariesViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        sharedLibrariesViewModel.sharedLibraries.observe(viewLifecycleOwner) {
            recyclerView.adapter = AdapterSharedLibs(it)
        }

        sharedLibrariesViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        sharedLibrariesViewModel.notFound.observe(viewLifecycleOwner) {
            showWarning(R.string.no_libraries_found)
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): SharedLibs {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = SharedLibs()
            fragment.arguments = args
            return fragment
        }
    }
}