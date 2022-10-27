package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterDexData
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.DexDataViewModel

class Dexs : ScopedFragment() {

    private lateinit var dexDataViewModel: DexDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dex_data, container, false)

        recyclerView = view.findViewById(R.id.dexs_recycler_view)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        dexDataViewModel = ViewModelProvider(this, packageInfoFactory)[DexDataViewModel::class.java]

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dexDataViewModel.getDexClasses().observe(viewLifecycleOwner) {
            val adapter = AdapterDexData(it)
            recyclerView.adapter = adapter
        }

        dexDataViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Dexs {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Dexs()
            fragment.arguments = args
            return fragment
        }
    }
}