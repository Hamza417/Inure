package app.simple.inure.ui.preferences.subscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.activities.association.*
import app.simple.inure.adapters.preferences.AdapterComponentManager
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComponentManager : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sub_preferences_component, container, false)

        recyclerView = view.findViewById(R.id.component_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val list = arrayListOf(
                    Triple(R.mipmap.ic_launcher, R.string.image_viewer, ImageActivity::class.java),
                    Triple(R.mipmap.ic_audio_player, R.string.audio_player, AudioPlayerActivity::class.java),
                    // Triple(R.mipmap.ic_terminal, R.string.terminal, TerminalAlias::class.java),
                    Triple(R.mipmap.ic_launcher, R.string.information, AppInfoActivity::class.java),
                    Triple(R.mipmap.ic_launcher, R.string.app_information, AppDetailsActivity::class.java),
                    Triple(R.mipmap.ic_launcher, R.string.installer, ApkInstallerActivity::class.java),
                    Triple(R.mipmap.ic_launcher, R.string.manifest, ManifestAssociationActivity::class.java),
                    Triple(R.mipmap.ic_launcher, R.string.text_viewer, TextViewerActivity::class.java),
                    Triple(R.mipmap.ic_launcher, R.string.ttf_viewer, TTFViewerActivity::class.java),
                    Triple(R.mipmap.ic_terminal, R.string.execute, BashAssociation::class.java),
            )

            list.sortBy {
                getString(it.second)
            }

            withContext(Dispatchers.Main) {
                recyclerView.adapter = AdapterComponentManager(list)
            }
        }
    }

    companion object {
        fun newInstance(): ComponentManager {
            val args = Bundle()
            val fragment = ComponentManager()
            fragment.arguments = args
            return fragment
        }
    }
}