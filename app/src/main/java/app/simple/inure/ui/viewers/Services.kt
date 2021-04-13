package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterServices
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.getServices
import com.jaredrummler.apkparser.model.AndroidComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Services : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        recyclerView = view.findViewById(R.id.services_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        launch {

            var list: List<AndroidComponent>

            withContext(Dispatchers.Default) {
                delay(500)
                list = requireArguments().getParcelable<ApplicationInfo>("application_info")?.getServices()!!

                list.sortedBy {
                    it.name
                }
            }

            recyclerView.adapter = AdapterServices(list)
        }
    }

    override fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

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