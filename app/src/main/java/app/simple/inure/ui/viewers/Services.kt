package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterServices
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Services : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var total: TypeFaceTextView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        recyclerView = view.findViewById(R.id.services_recycler_view)
        total = view.findViewById(R.id.total)
        applicationInfo = requireArguments().getParcelable("application_info")!!
        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getServices().observe(viewLifecycleOwner, {
            recyclerView.adapter = AdapterServices(it)
            total.text = getString(R.string.total, it.size)
        })

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            ErrorPopup.newInstance(it)
                    .show(childFragmentManager, "apk_error_window")
            total.text = getString(R.string.failed)
            total.setTextColor(Color.RED)
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Services {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Services()
            fragment.arguments = args
            return fragment
        }
    }
}
