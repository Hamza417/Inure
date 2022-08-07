package app.simple.inure.dialogs.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.util.SpannableStringBuilder

class PieHoleRadius : ScopedBottomSheetFragment() {

    private lateinit var value: TypeFaceTextView
    private lateinit var seekBar: ThemeSeekBar
    private lateinit var set: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var lastValue = 0F
    private val factor = 10F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_pie_hole_radius, container, false)

        value = view.findViewById(R.id.radius_textview)
        seekBar = view.findViewById(R.id.radius_seekbar)
        set = view.findViewById(R.id.radius_set)
        cancel = view.findViewById(R.id.radius_cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        value.text = SpannableStringBuilder.buildSpannableString("${AnalyticsPreferences.getPieHoleRadiusValue()}%", 2)
        seekBar.max = 990
        seekBar.updateProgress(AnalyticsPreferences.getPieHoleRadiusValue().toInt() * factor.toInt())
        lastValue = AnalyticsPreferences.getPieHoleRadiusValue()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                value.text = SpannableStringBuilder.buildSpannableString("${progress / factor}%", 2)
                if (fromUser) AnalyticsPreferences.setPieHoleRadiusValue(progress / factor)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        set.setOnClickListener {
            dismiss()
        }

        cancel.setOnClickListener {
            AnalyticsPreferences.setPieHoleRadiusValue(lastValue).also {
                dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AnalyticsMenu.newInstance()
            .show(parentFragmentManager, "analytics_menu")
    }

    companion object {
        fun newInstance(): PieHoleRadius {
            val args = Bundle()
            val fragment = PieHoleRadius()
            fragment.arguments = args
            return fragment
        }
    }
}