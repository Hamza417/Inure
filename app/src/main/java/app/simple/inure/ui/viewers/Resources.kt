package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterResources
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.APKParser
import app.simple.inure.util.FragmentHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Resources : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var applicationInfo: ApplicationInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_resources, container, false)

        recyclerView = view.findViewById(R.id.resources_recycler_view)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        launch {
            val adapterResources: AdapterResources

            withContext(Dispatchers.IO) {
                adapterResources = AdapterResources(APKParser.getXmlFiles(applicationInfo.sourceDir))
            }

            recyclerView.adapter = adapterResources

            adapterResources.setOnResourceClickListener(object : AdapterResources.ResourceCallbacks {
                override fun onResourceClicked(path: String) {
                    if (ConfigurationPreferences.isXmlViewerTextView()) {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerTextView.newInstance(applicationInfo, false, path),
                                                     "tv_xml")
                    } else {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerWebView.newInstance(applicationInfo, false, path),
                                                     "wv_xml")
                    }
                }
            })
        }
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Resources {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Resources()
            fragment.arguments = args
            return fragment
        }
    }
}