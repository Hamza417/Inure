package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterDexData
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.viewers.DexDataViewModel

class Dexs : ScopedFragment() {

    private lateinit var dexDataViewModel: DexDataViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var total: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dex_data, container, false)

        recyclerView = view.findViewById(R.id.dexs_recycler_view)
        total = view.findViewById(R.id.total_dexs)

        applicationInfo = requireArguments().getParcelable("application_info")!!
        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        dexDataViewModel = ViewModelProvider(this, applicationInfoFactory).get(DexDataViewModel::class.java)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dexDataViewModel.getDexClasses().observe(viewLifecycleOwner, {
            val adapter = AdapterDexData(it)
            total.text = it.size.toString()
            recyclerView.adapter = adapter
        })

        dexDataViewModel.getError().observe(viewLifecycleOwner, {
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
            total.text = getString(R.string.failed)
            total.setTextColor(Color.RED)
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Dexs {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Dexs()
            fragment.arguments = args
            return fragment
        }
    }
}