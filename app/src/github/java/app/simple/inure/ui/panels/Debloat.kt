package app.simple.inure.ui.panels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterDebloat
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.debloat.DebloatMenu.Companion.showDebloatMenu
import app.simple.inure.dialogs.debloat.DebloatSelect
import app.simple.inure.dialogs.debloat.DebloatSelect.Companion.showDebloatSelectionDialog
import app.simple.inure.dialogs.debloat.DebloatSort.Companion.showDebloatFilter
import app.simple.inure.dialogs.debloat.UninstallMethodChoice.Companion.showUninstallMethodChoice
import app.simple.inure.dialogs.menus.AppsMenu.Companion.showAppsMenu
import app.simple.inure.dialogs.miscellaneous.PackageStateResult.Companion.showPackageStateResult
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.models.Bloat
import app.simple.inure.preferences.DebloatPreferences
import app.simple.inure.ui.subpanels.DebloatChecklist
import app.simple.inure.ui.subpanels.DebloatSearch
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.panels.DebloatViewModel

class Debloat : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var debloatViewModel: DebloatViewModel? = null
    private var adapterDebloat: AdapterDebloat? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_debloat, container, false)

        recyclerView = view.findViewById(R.id.debloat_recycler_view)
        debloatViewModel = ViewModelProvider(requireActivity())[DebloatViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()
        postponeEnterTransition()

        if (debloatViewModel?.shouldShowLoader() == true) {
            showLoader(manualOverride = true)
        }

        debloatViewModel?.getBloatList()?.observe(viewLifecycleOwner) { bloats ->
            hideLoader()
            adapterDebloat = AdapterDebloat(bloats)

            adapterDebloat!!.setAdapterDebloatCallback(object : AdapterDebloat.Companion.AdapterDebloatCallback {
                override fun onBloatSelected(bloat: Bloat) {
                    bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.getDebloatMenu(adapterDebloat!!.isAnyItemSelected()))
                    debloatViewModel?.loadSelectedBloatList()
                }

                override fun onBloatLongPressed(bloat: Bloat) {
                    childFragmentManager.showAppsMenu(bloat.packageInfo)
                }
            })

            recyclerView.adapter = adapterDebloat

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(
                    BottomMenuConstants.getDebloatMenu(adapterDebloat!!.isAnyItemSelected()), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_select_all -> {
                        childFragmentManager.showDebloatSelectionDialog().setOnDebloatSelectCallback(object : DebloatSelect.Companion.DebloatSelectCallback {
                            override fun onModeSelected(mode: Int) {
                                adapterDebloat?.updateSelections(mode)
                            }
                        })
                    }
                    R.drawable.ic_checklist -> {
                        openFragmentSlide(DebloatChecklist.newInstance(), DebloatChecklist.TAG)
                    }
                    R.drawable.ic_recycling -> {
                        childFragmentManager.showUninstallMethodChoice().onUninstallMethodSelected = { uninstall ->
                            if (uninstall) {
                                onSure {
                                    showLoader(manualOverride = true)
                                    debloatViewModel?.initDebloaterEngine(DebloatViewModel.METHOD_UNINSTALL)
                                }
                            } else {
                                onSure {
                                    showLoader(manualOverride = true)
                                    debloatViewModel?.initDebloaterEngine(DebloatViewModel.METHOD_DISABLE)
                                }
                            }
                        }
                    }
                    R.drawable.ic_restore -> {
                        onSure {
                            showLoader(manualOverride = true)
                            debloatViewModel?.initDebloaterEngine(DebloatViewModel.METHOD_RESTORE)
                        }
                    }
                    R.drawable.ic_help -> {
                        getString(R.string.debloat_help).asUri().openInBrowser(requireContext())
                    }
                    R.drawable.ic_refresh -> {
                        debloatViewModel?.refreshBloatList()
                    }
                    R.drawable.ic_filter -> {
                        childFragmentManager.showDebloatFilter()
                    }
                    R.drawable.ic_settings -> {
                        childFragmentManager.showDebloatMenu()
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(DebloatSearch.newInstance(), DebloatSearch.TAG)
                    }
                }
            }
        }

        debloatViewModel?.getDebloatedPackages()?.observe(viewLifecycleOwner) {
            hideLoader()

            if (it.isNotNull() && it.isNotEmpty()) {
                childFragmentManager.showPackageStateResult(it)
                debloatViewModel?.clearDebloatedPackages()
                debloatViewModel?.refreshBloatList()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            DebloatPreferences.APPLICATION_TYPE,
            DebloatPreferences.LIST_TYPE,
            DebloatPreferences.REMOVAL_TYPE,
            DebloatPreferences.SORT,
            DebloatPreferences.STATE,
            DebloatPreferences.SORTING_STYLE -> {
                adapterDebloat?.setLoading(true)
                debloatViewModel?.refreshBloatList()
            }

            DebloatPreferences.RECOMMENDED_HIGHLIGHT,
            DebloatPreferences.ADVANCED_HIGHLIGHT,
            DebloatPreferences.EXPERT_HIGHLIGHT,
            DebloatPreferences.UNSAFE_HIGHLIGHT,
            DebloatPreferences.UNLISTED_HIGHLIGHT -> {
                adapterDebloat?.notifyDataSetChanged()
            }
        }
    }

    companion object {
        fun newInstance(): Debloat {
            val args = Bundle()
            val fragment = Debloat()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Debloat"
    }
}
