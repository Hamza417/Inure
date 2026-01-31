package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterDexData
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.preferences.DexClassesPreferences
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.DexDataViewModel

class DexClasses : SearchBarScopedFragment() {

    private lateinit var dexDataViewModel: DexDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var loader: CustomProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dex_data, container, false)

        search = view.findViewById(R.id.search)
        searchBox = view.findViewById(R.id.search_edit_text)
        title = view.findViewById(R.id.dex_title)
        recyclerView = view.findViewById(R.id.dexs_recycler_view)
        loader = view.findViewById(R.id.loader)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        dexDataViewModel = ViewModelProvider(this, packageInfoFactory)[DexDataViewModel::class.java]

        searchBoxState(animate = false, DexClassesPreferences.isSearchVisible())

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (packageInfo.packageName == "android") {
            showWarning(Warnings.ANDROID_SYSTEM_DEX_CLASSES)
        }

        dexDataViewModel.getDexClasses().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            setCount(it.size)

            val adapter = AdapterDexData(it, searchBox.text.toString().trim())

            adapter.onDetailsClicked = { dexClass ->
                openFragmentSlide(ClassSource.newInstance(dexClass, packageInfo), ClassSource.TAG)
            }

            recyclerView.setExclusiveAdapter(adapter)
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                dexDataViewModel.filterClasses(text.toString().trim())
            }
        }

        dexDataViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                DexClassesPreferences.setSearchVisible(!DexClassesPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DexClassesPreferences.DEX_SEARCH -> {
                searchBoxState(animate = true, DexClassesPreferences.isSearchVisible())
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): DexClasses {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            val fragment = DexClasses()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Dexs"
    }
}
