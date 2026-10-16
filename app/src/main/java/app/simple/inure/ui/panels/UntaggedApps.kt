package app.simple.inure.ui.panels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.tags.AdapterUntaggedApps
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.AppMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.viewmodels.panels.UntaggedAppsViewModel

class UntaggedApps : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var untaggedAppsViewModel: UntaggedAppsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tags, container, false)

        recyclerView = view.findViewById(R.id.tags_recycler_view)

        // Hide the bottom menu since we don't need it here
        view.findViewById<View>(R.id.bottom_menu).visibility = View.GONE

        untaggedAppsViewModel = ViewModelProvider(requireActivity())[UntaggedAppsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (untaggedAppsViewModel.getUntaggedApps().value != null) {
            postponeEnterTransition()
        } else {
            startPostponedEnterTransition()
            showLoader(manualOverride = true)
        }

        untaggedAppsViewModel.getUntaggedApps().observe(viewLifecycleOwner) {
            hideLoader()

            with(AdapterUntaggedApps(it)) {
                recyclerView.adapter = this
                setOnAdapterCallbacks(object : AdapterCallbacks {

                    override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                        openAppInfo(packageInfo, icon)
                    }

                    override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                        AppMenu.newInstance(packageInfo)
                            .show(childFragmentManager, AppMenu.TAG)
                    }
                })
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        fun newInstance(): UntaggedApps {
            val args = Bundle()
            val fragment = UntaggedApps()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "untagged_apps"
    }
}
