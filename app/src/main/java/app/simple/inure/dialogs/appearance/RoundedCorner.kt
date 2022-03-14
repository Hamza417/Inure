package app.simple.inure.dialogs.appearance

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.SpannableStringBuilder.buildSpannableString
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class RoundedCorner : ScopedBottomSheetFragment() {

    private lateinit var radiusValue: TextView
    private lateinit var radiusSeekBar: ThemeSeekBar
    private lateinit var cancel: DynamicRippleTextView
    private lateinit var set: DynamicRippleTextView
    private lateinit var cornerFrameLayout: DynamicCornerLinearLayout

    private var objectAnimator: ObjectAnimator? = null
    private var lastCornerValue = 0F
    private val factor = 10F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_app_corner, container, false)

        radiusValue = view.findViewById(R.id.app_corner_radius_textview)
        radiusSeekBar = view.findViewById(R.id.app_corner_radius_seekbar)
        cancel = view.findViewById(R.id.app_corner_radius_cancel)
        set = view.findViewById(R.id.app_corner_radius_set)
        cornerFrameLayout = view.findViewById(R.id.app_corner_dialog_container)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lastCornerValue = AppearancePreferences.getCornerRadius() * 5
        radiusValue.text = buildSpannableString("${AppearancePreferences.getCornerRadius()} px", 2)
        radiusSeekBar.max = 990
        radiusSeekBar.updateSeekbar((AppearancePreferences.getCornerRadius() * factor).toInt())

        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                println(progress)
                radiusValue.text = buildSpannableString("${progress / factor} px", 2)
                updateBackground(progress / factor)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                objectAnimator?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        set.setOnClickListener {
            AppearancePreferences.setCornerRadius(radiusSeekBar.progress / factor)
            this.dismiss()
        }

        cancel.setOnClickListener {
            this.dismiss()
        }
    }

    private fun updateBackground(radius: Float) {
        val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()

        cornerFrameLayout.background = MaterialShapeDrawable(shapeAppearanceModel)
    }

    companion object {
        fun newInstance(): RoundedCorner {
            val args = Bundle()
            val fragment = RoundedCorner()
            fragment.arguments = args
            return fragment
        }
    }
}
