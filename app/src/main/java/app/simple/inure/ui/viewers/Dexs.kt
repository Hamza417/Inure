package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterDexData
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.DexDataViewModel

class Dexs : ScopedFragment() {

    private lateinit var dexDataViewModel: DexDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dex_data, container, false)

        recyclerView = view.findViewById(R.id.dexs_recycler_view)

        packageInfo = requireArguments().getParcelable("application_info")!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        dexDataViewModel = ViewModelProvider(this, packageInfoFactory).get(DexDataViewModel::class.java)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dexDataViewModel.getDexClasses().observe(viewLifecycleOwner, {
            val adapter = AdapterDexData(it)
            recyclerView.adapter = adapter
        })

        dexDataViewModel.getError().observe(viewLifecycleOwner, {
            val e = Error.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : app.simple.inure.dialogs.miscellaneous.ErrorPopup.Companion.Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Dexs {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Dexs()
            fragment.arguments = args
            return fragment
        }
    }
}