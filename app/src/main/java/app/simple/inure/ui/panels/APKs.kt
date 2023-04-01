package app.simple.inure.ui.panels

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.*
import app.simple.inure.R
import app.simple.inure.activities.association.ManifestAssociationActivity
import app.simple.inure.adapters.ui.AdapterApks
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.apks.ApkScanner
import app.simple.inure.dialogs.apks.ApkScanner.Companion.showApkScanner
import app.simple.inure.dialogs.apks.ApksMenu.Companion.showApksMenu
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.popups.apks.PopupApkBrowser
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.ui.viewers.Information
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.viewmodels.panels.ApkBrowserViewModel
import java.io.File

class APKs : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var apkBrowserViewModel: ApkBrowserViewModel
    private lateinit var adapterApks: AdapterApks
    private var apkScanner: ApkScanner? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_apk_browser, container, false)

        recyclerView = view.findViewById(R.id.apks_recycler_view)

        apkBrowserViewModel = ViewModelProvider(requireActivity())[ApkBrowserViewModel::class.java]

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        if (fullVersionCheck()) {
            if (apkBrowserViewModel.getApkPaths().isInitialized.invert()) {
                apkScanner = childFragmentManager.showApkScanner()
                startPostponedEnterTransition()
            }
        }

        apkBrowserViewModel.getApkPaths().observe(viewLifecycleOwner) { it ->
            apkScanner?.dismiss()

            adapterApks = AdapterApks()
            adapterApks.paths = it
            adapterApks.notifyDataSetChanged()

            adapterApks.setOnItemClickListener(object : AdapterCallbacks {
                override fun onApkClicked(view: View, position: Int) {
                    kotlin.runCatching {
                        if (adapterApks.paths[position].endsWith(".apk")) {
                            packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requirePackageManager().getPackageArchiveInfo(adapterApks.paths[position], PackageManager.PackageInfoFlags.of(PackageUtils.flags))!!
                            } else {
                                @Suppress("DEPRECATION")
                                requirePackageManager().getPackageArchiveInfo(adapterApks.paths[position], PackageUtils.flags.toInt())!!
                            }

                            packageInfo.applicationInfo.sourceDir = adapterApks.paths[position]
                        } else {
                            packageInfo = PackageInfo() // empty package info
                            packageInfo.applicationInfo = ApplicationInfo() // empty application info
                            packageInfo.applicationInfo.sourceDir = adapterApks.paths[position]
                        }

                        openFragmentSlide(Information.newInstance(packageInfo), "apk_info")
                    }.onFailure {
                        showWarning("Failed to open apk : ${adapterApks.paths[position].substringAfterLast("/")}}", false)
                    }
                }

                override fun onApkLongClicked(view: View, position: Int) {
                    PopupApkBrowser(view).setPopupApkBrowserCallbacks(object : PopupApkBrowser.Companion.PopupApkBrowserCallbacks {
                        override fun onInstallClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireContext(),
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ File(adapterApks.paths[position]))
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(uri, "application/vnd.android.package-archive")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(intent)
                        }

                        override fun onDeleteClicked() {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    File(adapterApks.paths[position]).delete()
                                    adapterApks.paths.removeAt(position)
                                    adapterApks.notifyItemRemoved(position.plus(1))
                                    adapterApks.notifyItemChanged(0) // Update the header
                                }
                            })
                        }

                        override fun onSendClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireContext(),
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ File(adapterApks.paths[position]))
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "application/vnd.android.package-archive"
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(Intent.createChooser(intent, it[position].substringAfterLast("/")))
                        }

                        override fun onManifestClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireContext(),
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ File(adapterApks.paths[position]))
                            val intent = Intent(requireContext(), ManifestAssociationActivity::class.java)
                            intent.setDataAndType(uri, "application/vnd.android.package-archive")
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(Intent.createChooser(intent, it[position].substringAfterLast("/")))
                        }

                        override fun onInfoClicked() {
                            onApkClicked(view, position)
                        }
                    })
                }
            })

            recyclerView.adapter = adapterApks

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.apkBrowserMenu, recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_refresh -> {
                        apkScanner = childFragmentManager.showApkScanner()
                        apkBrowserViewModel.refresh()
                    }
                    R.drawable.ic_settings -> {
                        childFragmentManager.showApksMenu()
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(firstLaunch = true), "search")
                    }
                }
            }
        }
    }

    @Suppress("unused")
    private fun updateBottomMenu(isSelected: Boolean) {
        if (isSelected) {
            bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.apkBrowserMenuSelection)
        } else {
            bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.apkBrowserMenu)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ApkBrowserPreferences.loadSplitIcon -> {
                adapterApks.loadSplitIcon()
            }
        }
    }

    companion object {
        fun newInstance(): APKs {
            val args = Bundle()
            val fragment = APKs()
            fragment.arguments = args
            return fragment
        }
    }
}