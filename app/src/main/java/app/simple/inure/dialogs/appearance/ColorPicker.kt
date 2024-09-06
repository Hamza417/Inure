package app.simple.inure.dialogs.appearance

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterPickedColors
import app.simple.inure.decorations.colorpicker.ColorPickerView
import app.simple.inure.decorations.corners.DynamicCornerAccentColor
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ColorPickerPreferences
import app.simple.inure.util.ColorUtils.toHexColor
import app.simple.inure.util.ConditionUtils.invert

class ColorPicker : ScopedBottomSheetFragment() {

    private lateinit var colorPickerView: ColorPickerView
    private lateinit var colorsRecyclerView: CustomHorizontalRecyclerView
    private lateinit var hex: DynamicCornerEditText
    private lateinit var strip: DynamicCornerAccentColor
    private lateinit var set: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_color_picker, container, false)

        colorPickerView = view.findViewById(R.id.color_picker_view)
        colorsRecyclerView = view.findViewById(R.id.colors_recycler_view)
        hex = view.findViewById(R.id.color_hex_code)
        strip = view.findViewById(R.id.color_strip)
        set = view.findViewById(R.id.set)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsRecyclerView.setBackgroundColor(Color.TRANSPARENT)

        colorsRecyclerView.adapter = AdapterPickedColors(ColorPickerPreferences.getColorHistory()) {
            colorPickerView.setColor(Color.parseColor(it), true)
            strip.backgroundTintList = ColorStateList.valueOf(Color.parseColor(it))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                strip.outlineSpotShadowColor = Color.parseColor(it)
                strip.outlineAmbientShadowColor = Color.parseColor(it)
            }
        }

        colorPickerView.setColorListener { i, s, _ ->
            hex.setText(s)
            strip.backgroundTintList = ColorStateList.valueOf(i)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                strip.outlineSpotShadowColor = i
                strip.outlineAmbientShadowColor = i
            }
        }

        colorPickerView.setColor(AppearancePreferences.getPickedAccentColor())
        hex.setText(AppearancePreferences.getPickedAccentColor().toHexColor())
        strip.backgroundTintList = ColorStateList.valueOf(AppearancePreferences.getPickedAccentColor())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            strip.outlineSpotShadowColor = AppearancePreferences.getPickedAccentColor()
            strip.outlineAmbientShadowColor = AppearancePreferences.getPickedAccentColor()
            strip.elevation = resources.getDimensionPixelOffset(R.dimen.app_views_elevation).toFloat()
        }

        hex.doOnTextChanged { text, _, _, _ ->
            kotlin.runCatching {
                if (text.isNullOrEmpty().invert()) {
                    if (text!!.startsWith("#")) {
                        colorPickerView.setColor(Color.parseColor(text.toString()))
                    } else {
                        colorPickerView.setColor(Color.parseColor("#$text"))
                    }
                }
            }.getOrElse {
                colorPickerView.setColor(Color.WHITE)
            }
        }

        set.setOnClickListener {
            if (AppearancePreferences.setCustomColor(true)) {
                kotlin.runCatching {
                    if (hex.text.isNullOrEmpty().invert()) {
                        if (hex.text!!.startsWith("#")) {
                            AppearancePreferences.setAccentColor(Color.parseColor(hex.text.toString()))
                            AppearancePreferences.setPickedAccentColor(Color.parseColor(hex.text.toString()))
                            ColorPickerPreferences.setColorHistory(hex.text.toString())
                        } else {
                            AppearancePreferences.setAccentColor(Color.parseColor("#${hex.text}"))
                            AppearancePreferences.setPickedAccentColor(Color.parseColor("#${hex.text}"))
                            ColorPickerPreferences.setColorHistory("#${hex.text}")
                        }
                    } else {
                        AppearancePreferences.setAccentColor(colorPickerView.currentColor)
                        AppearancePreferences.setPickedAccentColor(colorPickerView.currentColor)
                        ColorPickerPreferences.setColorHistory(colorPickerView.currentColor.toHexColor())
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AppearancePreferences.setMaterialYouAccent(false)
                    }
                }.onSuccess {
                    dismiss()
                }.getOrElse {
                    showWarning(hex.text.toString() + " - invalid color code")
                }
            } else {
                showWarning("Failed to set color")
            }
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(): ColorPicker {
            val args = Bundle()
            val fragment = ColorPicker()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showColorPicker(): ColorPicker {
            val dialog = newInstance()
            dialog.show(this, "color_picker")
            return dialog
        }
    }
}
