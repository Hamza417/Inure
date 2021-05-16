package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterPermissions
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.getPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Permissions : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var totalPermissions: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_permissions, container, false)

        recyclerView = view.findViewById(R.id.permissions_recycler_view)
        totalPermissions = view.findViewById(R.id.total_permissions)
        recyclerView.setHasFixedSize(true)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch {
            val adapterPermissions: AdapterPermissions
            var k: MutableList<String>

            withContext(Dispatchers.Default) {
                k = applicationInfo.getPermissions()
                adapterPermissions = AdapterPermissions(k, applicationInfo)
            }

            recyclerView.adapter = adapterPermissions
            totalPermissions.text = getString(R.string.total, k.size)
        }
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