package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.dialogs.configuration.DateFormat
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.FormattingPreferences

class FormattingScreen : ScopedFragment() {

    private lateinit var useBinaryFormat: SwitchView
    private lateinit var loadLargeStrings: SwitchView
    private lateinit var dateFormat: DynamicRippleConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_formatting, container, false)

        startPostponedEnterTransition()

        useBinaryFormat = view.findViewById(R.id.configuration_use_binary_format)
        loadLargeStrings = view.findViewById(R.id.configuration_lift_string_limit)
        dateFormat = view.findViewById(R.id.date_format_container)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadLargeStrings.setChecked(FormattingPreferences.isLoadingLargeStrings())
        useBinaryFormat.setChecked(FormattingPreferences.getSizeType() == "binary")

        loadLargeStrings.setOnSwitchCheckedChangeListener {
            FormattingPreferences.setLoadLargeStrings(it)
        }

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