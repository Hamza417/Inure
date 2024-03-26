package app.simple.inure.ui.preferences.subscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterInstallerCustomization
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.models.VisibilityCustomizationModel
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.util.NullSafety.isNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InstallerCustomization : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sub_preferences_home_customization, container, false)

        recyclerView = view.findViewById(R.id.home_customization_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        fullVersionCheck()

        if (savedInstanceState.isNull()) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val list = ArrayList<VisibilityCustomizationModel>()

                list.add(VisibilityCustomizationModel(R.string.information, -1, InstallerPreferences.isInfoVisible))
                list.add(VisibilityCustomizationModel(R.string.changes, -1, InstallerPreferences.isChangesVisible))
                list.add(VisibilityCustomizationModel(R.string.permissions, -1, InstallerPreferences.isPermissionsVisible))
                list.add(VisibilityCustomizationModel(R.string.certificate, -1, InstallerPreferences.isCertificateVisible))
                list.add(VisibilityCustomizationModel(R.string.manifest, -1, InstallerPreferences.isManifestVisible))
                list.add(VisibilityCustomizationModel(R.string.trackers, -1, InstallerPreferences.isTrackersVisible))

                launch(Dispatchers.Main) {
                    recyclerView.adapter = AdapterInstallerCustomization(list)
                }
            }
        }
    }

    companion object {
        fun newInstance(): InstallerCustomization {
            val args = Bundle()
            val fragment = InstallerCustomization()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "installer_visibility"
    }
}
