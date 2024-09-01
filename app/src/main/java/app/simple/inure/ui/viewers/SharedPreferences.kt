package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterResources
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.popups.viewers.PopupSharedPreferences
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.SharedPreferencesViewModel

class SharedPreferences : SearchBarScopedFragment() {

    private lateinit var loader: CustomProgressBar
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var sharedPreferencesViewModel: SharedPreferencesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_shared_prefs, container, false)

        loader = view.findViewById(R.id.loader)
        recyclerView = view.findViewById(R.id.shared_prefs_recycler_view)
        title = view.findViewById(R.id.shared_prefs_title)

        val packageInfoFactory = PackageInfoFactory(packageInfo)
        sharedPreferencesViewModel = ViewModelProvider(this, packageInfoFactory)[SharedPreferencesViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        fullVersionCheck()

        sharedPreferencesViewModel.getSharedPrefs().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            val adapterResources = AdapterResources(it, "")
            setCount(it.size)

            adapterResources.setOnResourceClickListener(object : AdapterResources.ResourceCallbacks {
                override fun onResourceClicked(path: String) {
                    openFragmentSlide(SharedPrefsCode.newInstance(
                            sharedPreferencesViewModel.getSharedPrefsPath() + path, packageInfo), SharedPrefsCode.TAG)
                }

                override fun onResourceLongClicked(path: String, view: View, position: Int) {
                    PopupSharedPreferences(requireView()).setOnPopupNotesMenuCallbackListener(object : PopupSharedPreferences.Companion.PopupSharedPrefsMenuCallback {
                        override fun onOpenClicked() {
                            openFragmentSlide(SharedPrefsCode.newInstance(
                                    sharedPreferencesViewModel.getSharedPrefsPath() + path, packageInfo), SharedPrefsCode.TAG)
                        }

                        override fun onDeleteClicked() {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    sharedPreferencesViewModel.deletePreferences(path, position)
                                }
                            })
                        }
                    })
                }
            })

            recyclerView.adapter = adapterResources
        }

        sharedPreferencesViewModel.getError().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            showError(it)
        }

        sharedPreferencesViewModel.getDeleted().observe(viewLifecycleOwner) {
            if (it != -1) {
                sharedPreferencesViewModel.getSharedPrefs().value?.removeAt(it)
                recyclerView.adapter?.notifyItemRemoved(it)
                sharedPreferencesViewModel.resetDeleted()
            }
        }

        sharedPreferencesViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): SharedPreferences {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = SharedPreferences()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "shared_prefs_viewer"
    }
}
