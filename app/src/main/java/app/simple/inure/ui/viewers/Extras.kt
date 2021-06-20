package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterExtras
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Extras : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
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
            val adapterExtras = AdapterExtras(APKParser.getExtraFiles(applicationInfo.sourceDir))

            recyclerView.adapter = adapterExtras
            total.text = getString(R.string.total, adapterExtras.list.size)

            adapterExtras.setOnResourceClickListener(object : AdapterExtras.ExtrasCallbacks {
                override fun onResourceClicked(path: String) {
                    exitTransition = null
                    when {
                        path.endsWith(".ttf") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Font.newInstance(applicationInfo, path),
                                                        "ttf_viewer")
                        }
                        path.endsWith(".html") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        HtmlViewer.newInstance(applicationInfo, path),
                                                        "html_viewer")
                        }
                        path.endsWith(".java") ||
                                path.endsWith(".css") ||
                                path.endsWith(".json") ||
                                path.endsWith(".proto") ||
                                path.endsWith(".js") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        CodeViewer.newInstance(applicationInfo, path, path.substring(path.lastIndexOf(".") + 1, path.length)),
                                                        "code_viewer")
                        }
                        else -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(applicationInfo, path),
                                                        "text_viewer")
                        }
                    }

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