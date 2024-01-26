package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterDebloat
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.debloat.DebloatFilter.Companion.showDebloatFilter
import app.simple.inure.dialogs.menus.AppsMenu.Companion.showAppsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.models.Bloat
import app.simple.inure.viewmodels.panels.DebloatViewModel

class Debloat : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var debloatViewModel: DebloatViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_debloat, container, false)

        recyclerView = view.findViewById(R.id.debloat_recycler_view)
        debloatViewModel = ViewModelProvider(requireActivity())[DebloatViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()
        postponeEnterTransition()

        debloatViewModel.getBloatList().observe(viewLifecycleOwner) {
            val adapterDebloat = AdapterDebloat(it)

            adapterDebloat.setAdapterDebloatCallback(object : AdapterDebloat.Companion.AdapterDebloatCallback {
                override fun onBloatSelected(bloat: Bloat) {
                    bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.getDebloatMenu(adapterDebloat.isAnyItemSelected()))
                }

                override fun onBloatLongPressed(bloat: Bloat) {
                    childFragmentManager.showAppsMenu(bloat.packageInfo)
                }
            })

            recyclerView.adapter = adapterDebloat

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getDebloatMenu(adapterDebloat.isAnyItemSelected()), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_delete -> {
                        // TODO - Delete
                    }
                    R.drawable.ic_refresh -> {
                        debloatViewModel.refreshBloatList()
                    }
                    R.drawable.ic_filter -> {
                        childFragmentManager.showDebloatFilter()
                    }
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), "preferences")
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(firstLaunch = true), "search")
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): Debloat {
            val args = Bundle()
            val fragment = Debloat()
            fragment.arguments = args
            return fragment
        }
    }
}