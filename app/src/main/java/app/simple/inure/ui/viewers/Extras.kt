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
import app.simple.inure.adapters.viewers.AdapterExtras
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Extensions
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.Extra
import app.simple.inure.popups.viewers.PopupExtrasMenu
import app.simple.inure.preferences.ExtrasPreferences
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.viewmodels.viewers.ExtrasViewModel

class Extras : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var extrasViewModel: ExtrasViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private var adapterExtras: AdapterExtras? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_extras, container, false)

        recyclerView = view.findViewById(R.id.extras_recycler_view)
        options = view.findViewById(R.id.extras_options)
        search = view.findViewById(R.id.extras_search_btn)
        searchBox = view.findViewById(R.id.extras_search)
        title = view.findViewById(R.id.extras_title)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        extrasViewModel = ViewModelProvider(this, packageInfoFactory)[ExtrasViewModel::class.java]

        searchBoxState(false, ExtrasPreferences.isSearchVisible())
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extrasViewModel.getExtras().observe(viewLifecycleOwner) {
            setCount(it.size)

            if (recyclerView.adapter.isNull()) {
                adapterExtras = AdapterExtras(it, searchBox.text.toString().trim())
                recyclerView.adapter = adapterExtras

                adapterExtras?.setOnResourceClickListener(object : AdapterExtras.ExtrasCallbacks {
                    override fun onExtrasClicked(extra: Extra) {
                        when {
                            extra.path.endsWith(Extensions.TTF) -> {
                                openFragmentSlide(Font.newInstance(packageInfo, extra.path), Font.TAG)
                            }
                            extra.path.endsWith(Extensions.HTML) -> {
                                openFragmentSlide(XML.newInstance(packageInfo, false, extra.path), XML.TAG)
                            }
                            extra.path.endsWith(Extensions.JAVA) -> {
                                openFragmentSlide(Java.newInstance(packageInfo, extra.path), Java.TAG)
                            }
                            extra.path.endsWith(Extensions.MD) -> {
                                openFragmentSlide(Markdown.newInstance(packageInfo, extra.path), Markdown.TAG)
                            }
                            extra.path.endsWith(Extensions.JSON) -> {
                                openFragmentSlide(JSON.newInstance(packageInfo, extra.path), JSON.TAG)
                            }
                            else -> {
                                openFragmentSlide(Text.newInstance(packageInfo, extra.path), Text.TAG)
                            }
                        }
                    }

                    override fun onExtrasLongClicked(extra: Extra) {
                        when {
                            extra.path.endsWith(Extensions.TTF) -> {
                                openFragmentSlide(Font.newInstance(packageInfo, extra.path), Font.TAG)
                            }
                            extra.isTextFile() -> {
                                openFragmentSlide(Text.newInstance(packageInfo, extra.path), Text.TAG)
                            }
                            else -> {
                                openFragmentSlide(Text.newInstance(packageInfo, extra.path), Text.TAG)
                            }
                        }
                    }
                })
            } else {
                adapterExtras?.updateData(it, searchBox.text.toString())
            }

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    extrasViewModel.keyword = text.toString().trim()
                }
            }
        }

        extrasViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        extrasViewModel.notFound.observe(viewLifecycleOwner) {
            showWarning(R.string.no_extras_found)
        }

        options.setOnClickListener {
            PopupExtrasMenu(requireView())
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ExtrasPreferences.setSearchVisibility(!ExtrasPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun Extra.isTextFile(): Boolean {
        val textExtensions = listOf(
                Extensions.HTML,
                Extensions.JAVA,
                Extensions.CSS,
                Extensions.JSON,
                Extensions.PROTO,
                Extensions.JS,
                Extensions.MD)

        return textExtensions.any { path.endsWith(it) }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ExtrasPreferences.EXTRAS_SEARCH -> {
                searchBoxState(true, ExtrasPreferences.isSearchVisible())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapterExtras?.unregister()
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Extras {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            val fragment = Extras()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Extras"
    }
}
