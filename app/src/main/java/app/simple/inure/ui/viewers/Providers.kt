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
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.getActivities
import app.simple.inure.util.APKParser.getProviders
import com.jaredrummler.apkparser.model.AndroidComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.jar.Manifest

class Providers : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var total: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_provider, container, false)

        recyclerView = view.findViewById(R.id.providers_recycler_view)
        total = view.findViewById(R.id.total)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        launch {

            var list: List<AndroidComponent>

            withContext(Dispatchers.Default) {
                list = requireArguments().getParcelable<ApplicationInfo>("application_info")?.getProviders()!!

                list.sortedBy {
                    it.name
                }
            }

            recyclerView.adapter = AdapterServices(list)
            total.text = getString(R.string.total, list.size)
        }
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Providers {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Providers()
            fragment.arguments = args
            return fragment
        }
    }
}
