package app.simple.inure.ui.preferences.mainscreens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.extensions.fragments.ScopedFragment

class OtherAppsScreen : ScopedFragment() {

    private lateinit var felicity: DynamicRippleRelativeLayout
    private lateinit var positional: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_other_apps, container, false)

        felicity = view.findViewById(R.id.felicity_music_player)
        positional = view.findViewById(R.id.positional)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        felicity.setOnClickListener {
            val url = "https://play.google.com/store/apps/details?id=app.simple.felicity"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }

        positional.setOnClickListener {
            val url = "https://play.google.com/store/apps/details?id=app.simple.positional"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
    }

    companion object {
        fun newInstance(): OtherAppsScreen {
            val args = Bundle()
            val fragment = OtherAppsScreen()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "OtherAppsScreen"
    }
}