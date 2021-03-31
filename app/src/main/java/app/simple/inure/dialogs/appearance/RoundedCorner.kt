package app.simple.inure.dialogs.appearance

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.SpannableStringBuilder.buildSpannableString
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class RoundedCorner : ScopedBottomSheetFragment() {

    private lateinit var radiusValue: TextView
    private lateinit var radiusSeekBar: SeekBar
    private lateinit var cancel: Button
    private lateinit var set: Button
    private lateinit var cornerFrameLayout: DynamicCornerLinearLayout

    private var objectAnimator: ObjectAnimator? = null
    private var lastCornerValue = 0
    private var isValueSet = false

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

        lastCornerValue = AppearancePreferences.getCornerRadius() * 5
        radiusValue.text = buildSpannableString("${AppearancePreferences.getCornerRadius()} px", 2)
        radiusSeekBar.max = 400
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            radiusSeekBar.min = 25
        }
        updateSeekbar(AppearancePreferences.getCornerRadius() * 5)

        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress_: Int, fromUser: Boolean) {
                val progress = if (progress_ < 25) 25 else progress_

                radiusValue.text = buildSpannableString("${progress / 5F} px", 2)
                updateBackground(progress / 5F)

                if (fromUser) AppearancePreferences.setCornerRadius(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                objectAnimator?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        set.setOnClickListener {
            isValueSet = true
            this.dismiss()
        }

        cancel.setOnClickListener {
            isValueSet = false
            this.dismiss()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

    override fun onDismiss(dialog: DialogInterface) {
        if (isValueSet) {
            requireActivity().recreate()
        } else {
            AppearancePreferences.setCornerRadius(lastCornerValue)
        }
        super.onDismiss(dialog)
    }

    private fun updateBackground(radius: Float) {
        val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()

        cornerFrameLayout.background = MaterialShapeDrawable(shapeAppearanceModel)
    }

    private fun updateSeekbar(value: Int) {
        objectAnimator = ObjectAnimator.ofInt(radiusSeekBar, "progress", radiusSeekBar.progress, value)
        objectAnimator?.duration = 1000L
        objectAnimator?.interpolator = DecelerateInterpolator(1.5F)
        objectAnimator?.setAutoCancel(true)
        objectAnimator?.start()
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
