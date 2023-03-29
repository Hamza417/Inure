package app.simple.inure.ui.preferences.subscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterHomeCustomization
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.viewmodels.panels.HomeViewModel

class HomeCustomization : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sub_preferences_home_customization, container, false)

        recyclerView = view.findViewById(R.id.home_customization_recycler_view)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        fullVersionCheck()

        homeViewModel.getCustomizableMenuItems().observe(viewLifecycleOwner) {
            recyclerView.adapter = AdapterHomeCustomization(it)
        }
    }

    companion object {
        fun newInstance(): HomeCustomization {
            val args = Bundle()
            val fragment = HomeCustomization()
            fragment.arguments = args
            return fragment
        }
    }
}