package app.simple.inure.ui.preferences.subscreens

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterAccentColor
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.appearance.ColorPicker.Companion.showColorPicker
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.themes.data.MaterialYou

class AccentColor : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterAccentColor: AdapterAccentColor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_accent_color, container, false)

        recyclerView = view.findViewById(R.id.accent_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.setHasFixedSize(true)

        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapterAccentColor.getItemViewType(position) == 0) 2 else 1
            }
        }

        val list = arrayListOf(
                Pair(ContextCompat.getColor(requireContext(), R.color.inure), "Inure"),
                Pair(ContextCompat.getColor(requireContext(), R.color.blue), "Blue"),
                Pair(ContextCompat.getColor(requireContext(), R.color.blueGrey), "Blue Grey"),
                Pair(ContextCompat.getColor(requireContext(), R.color.darkBlue), "Dark Blue"),
                Pair(ContextCompat.getColor(requireContext(), R.color.red), "Red"),
                Pair(ContextCompat.getColor(requireContext(), R.color.green), "Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.orange), "Orange"),
                Pair(ContextCompat.getColor(requireContext(), R.color.purple), "Purple"),
                Pair(ContextCompat.getColor(requireContext(), R.color.yellow), "Yellow"),
                Pair(ContextCompat.getColor(requireContext(), R.color.caribbeanGreen), "Caribbean Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.persianGreen), "Persian Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.amaranth), "Amaranth"),
                Pair(ContextCompat.getColor(requireContext(), R.color.indian_red), "Indian Red"),
                Pair(ContextCompat.getColor(requireContext(), R.color.light_coral), "Light Coral"),
                Pair(ContextCompat.getColor(requireContext(), R.color.pink_flare), "Pink Flare"),
                Pair(ContextCompat.getColor(requireContext(), R.color.makeup_tan), "Makeup Tan"),
                Pair(ContextCompat.getColor(requireContext(), R.color.egg_yellow), "Egg Yellow"),
                Pair(ContextCompat.getColor(requireContext(), R.color.medium_green), "Medium Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.olive), "Olive"),
                Pair(ContextCompat.getColor(requireContext(), R.color.copperfield), "Copperfield"),
                Pair(ContextCompat.getColor(requireContext(), R.color.mineral_green), "Mineral Green"),
                Pair(ContextCompat.getColor(requireContext(), R.color.lochinvar), "Lochinvar"),
                Pair(ContextCompat.getColor(requireContext(), R.color.beach_grey), "Beach Grey"),
                Pair(ContextCompat.getColor(requireContext(), R.color.cashmere), "Cashmere"),
                Pair(ContextCompat.getColor(requireContext(), R.color.grape), "Grape"),
                Pair(ContextCompat.getColor(requireContext(), R.color.roman_silver), "Roman Silver"),
                Pair(ContextCompat.getColor(requireContext(), R.color.horizon), "Horizon"),
                Pair(ContextCompat.getColor(requireContext(), R.color.limed_spruce), "Limed Spruce"),
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(MaterialYou.materialYouAdapterIndex,
                     Pair(ContextCompat.getColor(requireContext(), MaterialYou.materialYouAccentResID), "Material You (Dynamic)"))
        }

        if (DevelopmentPreferences.get(DevelopmentPreferences.ENABLE_CUSTOM_COLOR_PICKER_IN_ACCENT)) { // Add color picker
            list.add(1, Pair(AppearancePreferences.getPickedAccentColor(), getString(R.string.color_picker)))
        }

        adapterAccentColor = AdapterAccentColor(list)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()

        adapterAccentColor.setAccentColorCallbacks(object : AdapterAccentColor.Companion.AccentColorCallbacks {
            override fun onAccentColorPicker() {
                childFragmentManager.showColorPicker()
            }
        })

        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.adapter = adapterAccentColor
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AppearancePreferences.ACCENT_COLOR, AppearancePreferences.IS_CUSTOM_COLOR -> {
                adapterAccentColor.updateAccentColor()
            }
        }
    }

    companion object {
        fun newInstance(): AccentColor {
            val args = Bundle()
            val fragment = AccentColor()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "AccentColor"
    }
}
