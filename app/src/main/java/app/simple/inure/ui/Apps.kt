package app.simple.inure.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.AppsAdapter
import app.simple.inure.decorations.views.StateAwareRecyclerView
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.AppData

class Apps : Fragment() {

    private lateinit var appsListRecyclerView: StateAwareRecyclerView
    private val model: AppData by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_apps, container, false)

        appsListRecyclerView = view.findViewById(R.id.all_apps_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        model.getAppData().observe(requireActivity(), {
            val appsAdapter = AppsAdapter(it)
            appsAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            appsListRecyclerView.adapter = appsAdapter
            appsListRecyclerView.scheduleLayoutAnimation()
        })

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(): Apps {
            val args = Bundle()
            val fragment = Apps()
            fragment.arguments = args
            return fragment
        }
    }
}