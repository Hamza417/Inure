package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterActivities
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.subviewers.ActivityInfo
import app.simple.inure.util.APKParser.getActivities
import app.simple.inure.util.FragmentHelper
import com.jaredrummler.apkparser.model.AndroidComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Activities : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var totalActivities: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        recyclerView = view.findViewById(R.id.activities_recycler_view)
        totalActivities = view.findViewById(R.id.total_activities)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        launch {

            var list: List<AndroidComponent>

            withContext(Dispatchers.Default) {
                list = applicationInfo.getActivities()!!

                list.sortedBy {
                    it.name.substring(it.name.lastIndexOf(".") + 1)
                }
            }

            recyclerView.adapter = AdapterActivities(applicationInfo, list)

            totalActivities.text = getString(R.string.total, list.size)

            (recyclerView.adapter as AdapterActivities).setOnActivitiesCallbacks(object : AdapterActivities.ActivitiesCallbacks {
                override fun onActivityClicked(androidComponent: AndroidComponent, packageId: String) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ActivityInfo.newInstance(packageId, applicationInfo),
                                                "activity_info")
                }
            })
        }
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
