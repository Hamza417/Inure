package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterBoot
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.preferences.BootPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.viewers.BootViewModel

class Boot : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterBoot: AdapterBoot? = null
    private var bootViewModel: BootViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_boot, container, false)

        recyclerView = view.findViewById(R.id.receivers_recycler_view)
        search = view.findViewById(R.id.receivers_search_btn)
        searchBox = view.findViewById(R.id.receivers_search)
        title = view.findViewById(R.id.boot_title)

        val packageInfoFactory = PackageInfoFactory(packageInfo)
        bootViewModel = ViewModelProvider(this, packageInfoFactory)[BootViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        fullVersionCheck()
        searchBoxState(false, BootPreferences.isSearchVisible())

        bootViewModel?.getBootData()?.observe(viewLifecycleOwner) {
            adapterBoot = AdapterBoot(it, keyword = searchBox.text.toString().trim())
            setCount(it.size)

            adapterBoot!!.setBootCallbacks(object : AdapterBoot.Companion.BootCallbacks {
                override fun onBootClicked(resolveInfo: ResolveInfo, checked: Boolean) {
                    bootViewModel?.setComponentEnabled(resolveInfo, checked)
                }

                override fun onBootLongPressed(packageId: String, icon: View, isComponentEnabled: Boolean, position: Int) {

                }

                override fun onBootSwitchChanged(resolveInfo: ResolveInfo, checked: Boolean) {
                    bootViewModel?.setComponentEnabled(resolveInfo, checked.invert())
                }
            })

            recyclerView.adapter = adapterBoot

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    bootViewModel?.filterKeywords(text.toString().trim())
                }
            }
        }

        bootViewModel?.getBootUpdate()?.observe(viewLifecycleOwner) {
            if (it.isNotNull()) {
                adapterBoot!!.updateBoot(it)
            }
        }

        bootViewModel?.getWarning()?.observe(viewLifecycleOwner) {
            if (it.isNotNull()) {
                showWarning(it)
            }
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                BootPreferences.setSearchVisible(!BootPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            BootPreferences.IS_SEARCH_VISIBLE -> {
                searchBoxState(false, BootPreferences.isSearchVisible())
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Boot {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Boot()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Boot"
    }
}
