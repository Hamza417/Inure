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
import app.simple.inure.popups.PopupOtherApps

class OtherAppsScreen : ScopedFragment() {

    private lateinit var felicity: DynamicRippleRelativeLayout
    private lateinit var peristyle: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_other_apps, container, false)

        felicity = view.findViewById(R.id.felicity_music_player)
        peristyle = view.findViewById(R.id.peristyle)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        felicity.setOnClickListener {
            PopupOtherApps(requireView()).setPopupOtherAppsCallbacks(object : PopupOtherApps.Companion.PopupOtherAppsCallbacks {
                override fun onGithubClicked() {
                    val url = "https://github.com/Hamza417/Felicity"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }

                override fun onFdroidClicked() {
                    showWarning("Felicity is not available on F-Droid yet, please use the GitHub link to download the app.", false)
                }

                override fun onIzzyondroidClicked() {
                    showWarning("Felicity is not available on IzzyOnDroid yet, please use the GitHub link to download the app.", false)
                }
            })
        }

        peristyle.setOnClickListener {
            PopupOtherApps(requireView()).setPopupOtherAppsCallbacks(object : PopupOtherApps.Companion.PopupOtherAppsCallbacks {
                override fun onGithubClicked() {
                    val url = "https://github.com/Hamza417/Peristyle"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }

                override fun onFdroidClicked() {
                    val url = "https://f-droid.org/en/packages/app.simple.peri/"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }

                override fun onIzzyondroidClicked() {
                    val url = "https://apt.izzysoft.de/fdroid/index/apk/app.simple.peri/"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }
            })
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
