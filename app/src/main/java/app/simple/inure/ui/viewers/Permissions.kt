package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterPermissions
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.panels.ApkDataViewModel

class Permissions : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var totalPermissions: TypeFaceTextView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_permissions, container, false)

        recyclerView = view.findViewById(R.id.permissions_recycler_view)
        totalPermissions = view.findViewById(R.id.total_permissions)
        recyclerView.setHasFixedSize(true)

        applicationInfo = requireArguments().getParcelable("application_info")!!
        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(ApkDataViewModel::class.java)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getPermissions().observe(viewLifecycleOwner, {
            recyclerView.adapter = AdapterPermissions(it, applicationInfo)
            totalPermissions.text = getString(R.string.total, it.size)
        })

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            ErrorPopup.newInstance(it)
                    .show(childFragmentManager, "apk_error_window")
            totalPermissions.text = getString(R.string.failed)
            totalPermissions.setTextColor(Color.RED)
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Permissions {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Permissions()
            fragment.arguments = args
            return fragment
        }
    }
}