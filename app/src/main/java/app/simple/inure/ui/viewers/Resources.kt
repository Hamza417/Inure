package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterResources
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Resources : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_resources, container, false)

        recyclerView = view.findViewById(R.id.resources_recycler_view)
        packageInfo = requireArguments().getParcelable("application_info")!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getResources().observe(viewLifecycleOwner, {
            val adapterResources = AdapterResources(it)

            recyclerView.adapter = adapterResources

            adapterResources.setOnResourceClickListener(object : AdapterResources.ResourceCallbacks {
                override fun onResourceClicked(path: String) {
                    clearExitTransition()

                    if (ConfigurationPreferences.isXmlViewerTextView()) {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerTextView.newInstance(packageInfo, false, path),
                                                    "tv_xml")
                    } else {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerWebView.newInstance(packageInfo, false, path),
                                                    "wv_xml")
                    }
                }

                override fun onResourceLongClicked(path: String) {
                    clearExitTransition()

                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                TextViewer.newInstance(packageInfo, path),
                                                "txt_tv_xml")
                }
            })
        })

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Resources {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Resources()
            fragment.arguments = args
            return fragment
        }
    }
}