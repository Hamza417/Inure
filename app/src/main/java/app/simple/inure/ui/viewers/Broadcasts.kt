package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterServices
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.getBroadcasts
import com.jaredrummler.apkparser.model.AndroidComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Broadcasts : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_broadcasts, container, false)

        recyclerView = view.findViewById(R.id.broadcast_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        launch {
            delay(500)
            var list: List<AndroidComponent>

            withContext(Dispatchers.Default) {
                list = requireArguments().getParcelable<ApplicationInfo>("application_info")?.getBroadcasts()!!

                list.sortedBy {
                    it.name
                }
            }

            recyclerView.adapter = AdapterServices(list)
        }
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Broadcasts {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Broadcasts()
            fragment.arguments = args
            return fragment
        }
    }
}