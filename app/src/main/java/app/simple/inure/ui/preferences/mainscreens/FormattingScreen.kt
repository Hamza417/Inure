package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.dialogs.configuration.DateFormat
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.ui.preferences.subscreens.InformationCustomization

class FormattingScreen : ScopedFragment() {

    private lateinit var useBinaryFormat: Switch
    private lateinit var dateFormat: DynamicRippleConstraintLayout
    private lateinit var infoVisibilityCustomization: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_formatting, container, false)

        startPostponedEnterTransition()

        useBinaryFormat = view.findViewById(R.id.configuration_use_binary_format)
        dateFormat = view.findViewById(R.id.date_format_container)
        infoVisibilityCustomization = view.findViewById(R.id.info_visibility_customization)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        useBinaryFormat.isChecked = FormattingPreferences.getSizeType() == "binary"

        useBinaryFormat.setOnSwitchCheckedChangeListener {
            if (it) {
                FormattingPreferences.setSizeType("binary")
            } else {
                FormattingPreferences.setSizeType("si")
            }
        }

        dateFormat.setOnClickListener {
            DateFormat.newInstance().show(childFragmentManager, "date_format")
        }

        infoVisibilityCustomization.setOnClickListener {
            openFragmentSlide(InformationCustomization.newInstance(), "info_visibility")
        }
    }

    companion object {
        fun newInstance(): FormattingScreen {
            val args = Bundle()
            val fragment = FormattingScreen()
            fragment.arguments = args
            return fragment
        }
    }
}
