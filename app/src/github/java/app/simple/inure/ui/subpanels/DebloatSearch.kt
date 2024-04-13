package app.simple.inure.ui.subpanels

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
import app.simple.inure.dialogs.app.AppMenu.Companion.showAppMenu
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.models.Bloat
import app.simple.inure.preferences.DebloatPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.DebloatViewModel

class DebloatSearch : KeyboardScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var searchBox: TypeFaceEditText
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var searchContainer: LinearLayout

    private lateinit var debloatViewModel: DebloatViewModel
    private var adapterDebloat: AdapterDebloat? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_debloat_search, container, false)

        recyclerView = view.findViewById(R.id.search_recycler_view)
        searchBox = view.findViewById(R.id.search_box)
        clear = view.findViewById(R.id.clear)
        searchContainer = view.findViewById(R.id.search_container)
        debloatViewModel = ViewModelProvider(requireActivity())[DebloatViewModel::class.java]

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
                if (DebloatPreferences.setSearchKeyword(text.toString())) {
                    debloatViewModel.keyword = text.toString()
                }
            }

            clearButtonState()
        }

        debloatViewModel.getSearchedBloatList().observe(viewLifecycleOwner) {
            adapterDebloat = AdapterDebloat(it, false, DebloatPreferences.getSearchKeyword())
            adapterDebloat!!.setAdapterDebloatCallback(object : AdapterDebloat.Companion.AdapterDebloatCallback {
                override fun onBloatSelected(bloat: Bloat) {
                    debloatViewModel.loadSelectedBloatList()
                }

                override fun onBloatLongPressed(bloat: Bloat) {
                    childFragmentManager.showAppMenu(bloat.packageInfo)
                }
            })
            recyclerView.adapter = adapterDebloat
        }

        clear.setOnClickListener {
            searchBox.text?.clear()
            DebloatPreferences.setSearchKeyword("")
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
