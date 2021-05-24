package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterActivities
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.subviewers.ActivityInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.panels.ApkDataViewModel
import com.jaredrummler.apkparser.model.AndroidComponent

class Activities : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var totalActivities: TypeFaceTextView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        recyclerView = view.findViewById(R.id.activities_recycler_view)
        totalActivities = view.findViewById(R.id.total_activities)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getActivities().observe(viewLifecycleOwner, {
            recyclerView.adapter = AdapterActivities(applicationInfo, it)

            totalActivities.text = getString(R.string.total, it.size)

            (recyclerView.adapter as AdapterActivities).setOnActivitiesCallbacks(object : AdapterActivities.ActivitiesCallbacks {
                override fun onActivityClicked(androidComponent: AndroidComponent, packageId: String) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ActivityInfo.newInstance(packageId, applicationInfo),
                                                "activity_info")
                }
            })
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Activities {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Activities()
            fragment.arguments = args
            return fragment
        }
    }
}
