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
import app.simple.inure.adapters.details.AdapterExtras
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
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
            if (recyclerView.adapter.isNull()) {
                adapterExtras = AdapterExtras(it, searchBox.text.toString().trim())
                recyclerView.adapter = adapterExtras

                adapterExtras?.setOnResourceClickListener(object : AdapterExtras.ExtrasCallbacks {
                    override fun onExtrasClicked(path: String) {
                        when {
                            path.endsWith(".ttf") -> {
                                openFragmentSlide(Font.newInstance(packageInfo, path), "ttf_viewer")
                            }
                            path.endsWith(".html") -> {
                                openFragmentSlide(XMLViewerTextView.newInstance(packageInfo, false, path), "html_viewer")
                            }
                            path.endsWith(".java") -> {
                                openFragmentSlide(Java.newInstance(packageInfo, path), "java_viewer")
                            }
                            path.endsWith(".md") -> {
                                openFragmentSlide(Markdown.newInstance(packageInfo, path), "md_viewer")
                            }
                            path.endsWith(".json") -> {
                                openFragmentSlide(JSON.newInstance(packageInfo, path), "json_viewer")
                            }
                            /**
                             * TODO - Add a delicious looking code viewers
                             *
                             * JSON done
                             * JAVA done
                             */
                            else -> {
                                openFragmentSlide(Text.newInstance(packageInfo, path), "text_viewer")
                            }
                        }
                    }

                    override fun onExtrasLongClicked(path: String) {
                        when {
                            path.endsWith(".ttf") -> {
                                openFragmentSlide(Font.newInstance(packageInfo, path), "ttf_viewer")
                            }
                            path.endsWith(".html") ||
                                    path.endsWith(".java") ||
                                    path.endsWith(".css") ||
                                    path.endsWith(".json") ||
                                    path.endsWith(".proto") ||
                                    path.endsWith(".js") ||
                                    path.endsWith(".md") -> {
                                openFragmentSlide(Text.newInstance(packageInfo, path), "text_viewer")
                            }
                            else -> {
                                openFragmentSlide(Text.newInstance(packageInfo, path), "text_viewer")
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

        options.setOnClickListener {
            PopupExtrasMenu(it)
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ExtrasPreferences.setSearchVisibility(!ExtrasPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ExtrasPreferences.extrasSearch -> {
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
    }
}