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
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.activities.association.ManifestAssociationActivity
import app.simple.inure.adapters.ui.AdapterApks
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getPackageArchiveInfo
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.apks.ApkScanner
import app.simple.inure.dialogs.apks.ApkScanner.Companion.showApkScanner
import app.simple.inure.dialogs.apks.ApksMenu.Companion.showApksMenu
import app.simple.inure.dialogs.apks.ApksSort.Companion.showApksSort
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.miscellaneous.StoragePermission
import app.simple.inure.dialogs.miscellaneous.StoragePermission.Companion.showStoragePermissionDialog
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.models.ApkFile
import app.simple.inure.popups.apks.PopupApkBrowser
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.ui.subpanels.ApksSearch
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.PermissionUtils.checkStoragePermission
import app.simple.inure.viewmodels.panels.ApkBrowserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile

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
            if (requireContext().checkStoragePermission()) {
                if (apkBrowserViewModel.shouldShowLoader()) {
                    apkScanner = childFragmentManager.showApkScanner()
                }
            } else {
                childFragmentManager.showStoragePermissionDialog()
                    .setStoragePermissionCallbacks(object : StoragePermission.Companion.StoragePermissionCallbacks {
                        override fun onStoragePermissionGranted() {
                            if (apkBrowserViewModel.shouldShowLoader()) {
                                apkBrowserViewModel.refresh()
                                apkScanner = childFragmentManager.showApkScanner()
                            }
                        }
                    })
            }
        }

        apkBrowserViewModel.getApkFiles().observe(viewLifecycleOwner) { apkFiles ->
            postponeEnterTransition()
            apkScanner?.dismiss()

            adapterApks = AdapterApks(
                    paths = apkFiles,
                    transitionName = requireArguments().getString(BundleConstants.transitionName, ""),
                    transitionPosition = requireArguments().getInt(BundleConstants.position, 0))

            adapterApks.setOnItemClickListener(object : AdapterCallbacks {
                override fun onApkClicked(view: View, position: Int, icon: ImageView) {
                    val uri = FileProvider.getUriForFile(
                            /* context = */ requireActivity().applicationContext,
                            /* authority = */ "${requireContext().packageName}.provider",
                            /* file = */ adapterApks.paths[position].file)

                    icon.transitionName = uri.toString()
                    requireArguments().putString(BundleConstants.transitionName, icon.transitionName)
                    requireArguments().putInt(BundleConstants.position, position)
                    // icon.transitionName = adapterApks.paths[position].absolutePath
                    openFragmentArc(Installer.newInstance(uri), icon, Installer.TAG)
                }

                override fun onApkLongClicked(view: View, position: Int, icon: ImageView) {
                    PopupApkBrowser(requireView()).setPopupApkBrowserCallbacks(object : PopupApkBrowser.Companion.PopupApkBrowserCallbacks {
                        override fun onInstallClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireActivity().applicationContext,
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ adapterApks.paths[position].file)

                            icon.transitionName = uri.toString()
                            requireArguments().putString(BundleConstants.transitionName, icon.transitionName)
                            requireArguments().putInt(BundleConstants.position, position)
                            // icon.transitionName = adapterApks.paths[position].absolutePath
                            openFragmentArc(Installer.newInstance(uri), icon, Installer.TAG)
                        }

                        override fun onDeleteClicked() {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    try {
                                        if (adapterApks.paths[position].file.delete()) {
                                            apkBrowserViewModel.delete(adapterApks.paths[position])
                                            adapterApks.paths.removeAt(position)
                                            adapterApks.notifyItemRemoved(position.plus(1))
                                            adapterApks.notifyItemChanged(0) // Update the header
                                        }
                                    } catch (e: IndexOutOfBoundsException) {
                                        showWarning("Failed to delete ${adapterApks.paths[position].file.name}", false)
                                    }
                                }
                            })
                        }

                        override fun onSendClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireContext(),
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ adapterApks.paths[position].file)
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "application/vnd.android.package-archive"
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(Intent.createChooser(intent, apkFiles[position].file.absolutePath.substringAfterLast("/")))
                        }

                        override fun onManifestClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireContext(),
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ adapterApks.paths[position].file)
                            val intent = Intent(requireContext(), ManifestAssociationActivity::class.java)
                            intent.setDataAndType(uri, "application/vnd.android.package-archive")
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(Intent.createChooser(intent, apkFiles[position].file.absolutePath.substringAfterLast("/")))
                        }

                        override fun onInfoClicked() {
                            showLoader(manualOverride = true)
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                kotlin.runCatching {
                                    if (adapterApks.paths[position].file.absolutePath.endsWith(".apk")) {
                                        packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            requirePackageManager().getPackageArchiveInfo(adapterApks.paths[position].file.absolutePath, PackageManager.PackageInfoFlags.of(PackageUtils.flags))!!
                                        } else {
                                            requirePackageManager().getPackageArchiveInfo(adapterApks.paths[position].file.absolutePath, PackageUtils.flags.toInt())!!
                                        }

                                        packageInfo.safeApplicationInfo.sourceDir = adapterApks.paths[position].file.absolutePath
                                    } else if (adapterApks.paths[position].file.absolutePath.endsWith(".apks") ||
                                            adapterApks.paths[position].file.absolutePath.endsWith(".xapk") ||
                                            adapterApks.paths[position].file.absolutePath.endsWith(".zip") ||
                                            adapterApks.paths[position].file.absolutePath.endsWith(".apkm")) {

                                        val copiedFile = requireContext().getInstallerDir(adapterApks.paths[position].file.name + ".zip") // .zip is useless here

                                        ZipFile(adapterApks.paths[position].file.path).extractAll(copiedFile.path.substringBeforeLast("."))

                                        for (file in copiedFile.path.substringBeforeLast(".").toFile().listFiles()!!) {
                                            packageInfo = requirePackageManager().getPackageArchiveInfo(file.absolutePath.toFile()) ?: continue
                                            packageInfo.safeApplicationInfo.sourceDir = file.absolutePath
                                            packageInfo.safeApplicationInfo.publicSourceDir = file.absolutePath
                                            break
                                        }
                                    } else {
                                        packageInfo = PackageInfo() // empty package info
                                        packageInfo.safeApplicationInfo = ApplicationInfo() // empty application info
                                        packageInfo.safeApplicationInfo.sourceDir = adapterApks.paths[position].file.absolutePath
                                    }

                                    withContext(Dispatchers.Main) {
                                        if (requirePackageManager().isPackageInstalled(packageInfo.packageName)) {
                                            packageInfo = requirePackageManager().getPackageInfo(packageInfo.packageName)!!
                                            icon.transitionName = packageInfo.packageName
                                            requireArguments().putString(BundleConstants.transitionName, icon.transitionName)
                                            requireArguments().putInt(BundleConstants.position, position)
                                            packageInfo.safeApplicationInfo.name = apkFiles[position].file.absolutePath.substringAfterLast("/")
                                            hideLoader()
                                            openFragmentArc(AppInfo.newInstance(packageInfo), icon, "apk_info")
                                        } else {
                                            icon.transitionName = packageInfo.packageName
                                            requireArguments().putString(BundleConstants.transitionName, icon.transitionName)
                                            requireArguments().putInt(BundleConstants.position, position)
                                            packageInfo.safeApplicationInfo.name = apkFiles[position].file.absolutePath.substringAfterLast("/")
                                            hideLoader()
                                            openFragmentArc(AppInfo.newInstance(packageInfo), icon, "apk_info")
                                        }
                                    }
                                }.onFailure {
                                    withContext(Dispatchers.Main) {
                                        hideLoader()
                                        showWarning("Failed to open package file" +
                                                            " : ${adapterApks.paths[position].file.absolutePath.substringAfterLast("/")}", false)
                                    }
                                }
                            }
                        }

                        override fun onSelectClicked() {
                            adapterApks.paths[position].isSelected = !adapterApks.paths[position].isSelected
                            adapterApks.notifyItemChanged(position.plus(1))
                            // adapterApks.notifyItemChanged(0) // Update the header

                            adapterApks.isSelectionMode = adapterApks.paths.any { it.isSelected }
                            updateBottomMenu()
                        }
                    })
                }

                override fun onSelectionChanged() {
                    updateBottomMenu()
                }
            })

            recyclerView.adapter = adapterApks

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getApkBrowserMenu(), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_refresh -> {
                        apkScanner = childFragmentManager.showApkScanner()
                        apkBrowserViewModel.refresh()
                    }

                    R.drawable.ic_settings -> {
                        childFragmentManager.showApksMenu()
                    }

                    R.drawable.ic_search -> {
                        openFragmentSlide(ApksSearch.newInstance(), ApksSearch.TAG)
                    }

                    R.drawable.ic_filter -> {
                        childFragmentManager.showApksSort()
                    }

                    R.drawable.ic_send -> {
                        if (adapterApks.paths.any { it.isSelected }) {
                            @Suppress("UNCHECKED_CAST")
                            val selectedApks = (adapterApks.paths.clone() as ArrayList<ApkFile>).filter { it.isSelected }
                            val selectedApksFiles = selectedApks.map { it.file }
                            val selectedApksFilesUri = selectedApksFiles.map {
                                FileProvider.getUriForFile(requireContext(),
                                                           "${requireContext().packageName}.provider", it)
                            }
                            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
                            intent.type = "application/vnd.android.package-archive"
                            intent.putExtra(Intent.EXTRA_STREAM, ArrayList(selectedApksFilesUri))
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(Intent.createChooser(intent, "Share ${selectedApksFiles.size} APKs"))
                        } else {
                            showWarning("No APKs selected", false)
                        }
                    }

                    R.drawable.ic_delete -> { // Delete the selected files
                        childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                            override fun onSure() {
                                if (adapterApks.paths.any { it.isSelected }) {
                                    @Suppress("UNCHECKED_CAST") val selectedApks =
                                        (adapterApks.paths.clone() as ArrayList<ApkFile>).filter {
                                            it.isSelected
                                        }
                                    for (apk in selectedApks) {
                                        if (apk.file.exists()) {
                                            if (apk.file.delete()) {
                                                val position = adapterApks.paths.indexOf(apk)
                                                adapterApks.paths.remove(apk)
                                                adapterApks.notifyItemRemoved(position.plus(1))
                                                adapterApks.notifyItemChanged(0) // Update the header
                                            } else {
                                                showWarning("Failed to delete ${apk.file.name}", false)
                                            }
                                        }
                                    }

                                    adapterApks.isSelectionMode = adapterApks.paths.any { it.isSelected }
                                    updateBottomMenu()
                                } else {
                                    showWarning("No APKs selected", false)
                                }
                            }
                        })
                    }
                }
            }

            updateBottomMenu()
        }
    }

    private fun updateBottomMenu() {
        if (adapterApks.isSelectionMode) {
            bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.getApkBrowserMenuSelection())
        } else {
            bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.getApkBrowserMenu())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ApkBrowserPreferences.LOAD_SPLIT_ICON -> {
                adapterApks.loadSplitIcon()
            }

            ApkBrowserPreferences.APK_FILTER -> {
                apkBrowserViewModel.filter()
            }

            ApkBrowserPreferences.REVERSED,
            ApkBrowserPreferences.SORT_STYLE,
                -> {
                apkBrowserViewModel.sort()
            }

            ApkBrowserPreferences.EXTERNAL_STORAGE -> {
                apkBrowserViewModel.refresh()
                apkScanner = childFragmentManager.showApkScanner()
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

        const val TAG = "APKs"
    }
}
