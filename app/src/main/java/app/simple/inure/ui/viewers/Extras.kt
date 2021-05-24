package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterResources
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.APKParser
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.panels.ApkDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Extras : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var total: TypeFaceTextView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_extras, container, false)

        recyclerView = view.findViewById(R.id.extras_recycler_view)
        total = view.findViewById(R.id.total)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getExtras().observe(viewLifecycleOwner, {
            val adapterResources = AdapterResources(APKParser.getExtraFiles(applicationInfo.sourceDir))

            recyclerView.adapter = adapterResources
            total.text = getString(R.string.total, adapterResources.list.size)

            adapterResources.setOnResourceClickListener(object : AdapterResources.ResourceCallbacks {
                override fun onResourceClicked(path: String) {
                    exitTransition = null
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                TextViewer.newInstance(applicationInfo, path),
                                                "text_viewer")
                }
            })
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Extras {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Extras()
            fragment.arguments = args
            return fragment
        }
    }
}