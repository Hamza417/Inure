package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterExtras
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextSearch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.viewers.PopupExtrasMenu
import app.simple.inure.preferences.ExtrasPreferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.factory.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.ExtrasViewModel

class Extras : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var filter: DynamicRippleImageButton
    private lateinit var options: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextSearch
    private lateinit var extrasViewModel: ExtrasViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private var adapterExtras: AdapterExtras? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_extras, container, false)

        recyclerView = view.findViewById(R.id.extras_recycler_view)
        filter = view.findViewById(R.id.extras_filter)
        options = view.findViewById(R.id.extras_options)
        search = view.findViewById(R.id.extras_search_btn)
        searchBox = view.findViewById(R.id.extras_search)
        title = view.findViewById(R.id.extras_title)
        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        extrasViewModel = ViewModelProvider(this, packageInfoFactory).get(ExtrasViewModel::class.java)

        searchBoxState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        extrasViewModel.getExtras().observe(viewLifecycleOwner, {
            adapterExtras = AdapterExtras(it, searchBox.text.toString())
            recyclerView.adapter = adapterExtras

            adapterExtras?.setOnResourceClickListener(object : AdapterExtras.ExtrasCallbacks {
                override fun onExtrasClicked(path: String) {
                    clearEnterTransition()
                    clearExitTransition()
                    when {
                        path.endsWith(".ttf") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Font.newInstance(packageInfo, path),
                                                        "ttf_viewer")
                        }
                        path.endsWith(".html") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        HtmlViewer.newInstance(packageInfo, path),
                                                        "html_viewer")
                        }
                        /**
                         * TODO - Add a delicious looking code viewer
                         *
                         * JSON done
                         */
                        path.endsWith(".java") ||
                                path.endsWith(".css") ||
                                path.endsWith(".proto") ||
                                path.endsWith(".js") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(packageInfo, path),
                                                        "text_viewer")
                        }
                        path.endsWith(".md") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Markdown.newInstance(packageInfo, path),
                                                        "md_viewer")
                        }
                        path.endsWith(".json") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        JSONViewer.newInstance(packageInfo, path),
                                                        "json_viewer")
                        }
                        else -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(packageInfo, path),
                                                        "text_viewer")
                        }
                    }
                }

                override fun onExtrasLongClicked(path: String) {
                    clearEnterTransition()
                    clearExitTransition()
                    when {
                        path.endsWith(".ttf") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Font.newInstance(packageInfo, path),
                                                        "ttf_viewer")
                        }
                        path.endsWith(".html") ||
                                path.endsWith(".java") ||
                                path.endsWith(".css") ||
                                path.endsWith(".json") ||
                                path.endsWith(".proto") ||
                                path.endsWith(".js") ||
                                path.endsWith(".md") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(packageInfo, path),
                                                        "text_viewer")
                        }
                        else -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(packageInfo, path),
                                                        "text_viewer")
                        }
                    }
                }
            })
        })

        extrasViewModel.getError().observe(viewLifecycleOwner, {
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })

        options.setOnClickListener {
            PopupExtrasMenu(it)
        }

        filter.setOnClickListener {
            Toast.makeText(requireContext(), "Currently thinking which approach would be better, " +
                    "bit flags or boolean for filtering the list. " +
                    "Will be implemented later.", Toast.LENGTH_SHORT).show()
        }

        search.setOnClickListener {
            ExtrasPreferences.setSearchVisibility(!ExtrasPreferences.isSearchVisible())
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                extrasViewModel.getExtrasData(text.toString())
            }
        }
    }

    private fun searchBoxState() {
        if (ExtrasPreferences.isSearchVisible()) {
            search.setImageResource(R.drawable.ic_close)
            title.gone()
            searchBox.visible(true)
        } else {
            search.setImageResource(R.drawable.ic_search)
            title.visible(true)
            searchBox.gone()
        }

        searchBox.toggleInput()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ExtrasPreferences.extrasSearch -> {
                searchBoxState()
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