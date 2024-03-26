package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.ui.preferences.subscreens.InformationCustomization
import app.simple.inure.ui.preferences.subscreens.InstallerCustomization

class LayoutsScreen : ScopedFragment() {

    private lateinit var installerVisibilityCustomization: DynamicRippleRelativeLayout
    private lateinit var infoVisibilityCustomization: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_layouts, container, false)

        installerVisibilityCustomization = view.findViewById(R.id.installer_visibility_customization)
        infoVisibilityCustomization = view.findViewById(R.id.info_visibility_customization)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        installerVisibilityCustomization.setOnClickListener {
            openFragmentSlide(InstallerCustomization.newInstance(), "installer_visibility")
        }

        infoVisibilityCustomization.setOnClickListener {
            openFragmentSlide(InformationCustomization.newInstance(), "info_visibility")
        }
    }

    companion object {
        fun newInstance(): LayoutsScreen {
            val args = Bundle()
            val fragment = LayoutsScreen()
            fragment.arguments = args
            return fragment
        }
    }
}
