package app.simple.inure.ui.subpanels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterDebloat
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.preferences.DebloatPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.DebloatViewModel

class DebloatSearch : KeyboardScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var searchBox: TypeFaceEditText
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var searchContainer: LinearLayout

    private lateinit var debloatViewModel: DebloatViewModel
    private var adapterMusic: AdapterDebloat? = null

    private var deletedId = -1L
    private var displayHeight: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_debloat_search, container, false)

        recyclerView = view.findViewById(R.id.search_recycler_view)
        searchBox = view.findViewById(R.id.search_box)
        clear = view.findViewById(R.id.clear)
        searchContainer = view.findViewById(R.id.search_container)
        debloatViewModel = ViewModelProvider(requireActivity())[DebloatViewModel::class.java]

        displayHeight = StatusBarHeight.getDisplayHeight(requireContext()) +
                StatusBarHeight.getStatusBarHeight(requireContext().resources)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        searchBox.setText(DebloatPreferences.getSearchKeyword())
        searchBox.setWindowInsetsAnimationCallback()
        clearButtonState()

        if (requireArguments().getBoolean(BundleConstants.isKeyboardOpened, false).invert()) {
            searchBox.showInput()
            requireArguments().putBoolean(BundleConstants.isKeyboardOpened, true)
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                DebloatPreferences.setSearchKeyword(text.toString())
                debloatViewModel.keyword = text.toString()
            }

            clearButtonState()
        }

        debloatViewModel.getSearchedBloatList().observe(viewLifecycleOwner) {
            adapterMusic = AdapterDebloat(it, false)
            recyclerView.adapter = adapterMusic
        }

        clear.setOnClickListener {
            searchBox.text?.clear()
            DebloatPreferences.setSearchKeyword("")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {

        }
    }

    private fun clearButtonState() {
        if (searchBox.text.isNullOrEmpty()) {
            clear.gone(animate = true)
        } else {
            clear.visible(animate = true)
        }
    }

    companion object {
        fun newInstance(): DebloatSearch {
            val args = Bundle()
            val fragment = DebloatSearch()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "DebloatSearch"
    }
}
