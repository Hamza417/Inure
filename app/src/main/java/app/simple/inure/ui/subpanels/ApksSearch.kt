package app.simple.inure.ui.subpanels

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.activities.association.ApkInstallerActivity
import app.simple.inure.activities.association.ManifestAssociationActivity
import app.simple.inure.adapters.ui.AdapterApksSearch
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.isInstalled
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.popups.apks.PopupApkBrowser
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.ui.panels.AppInfo
import app.simple.inure.ui.viewers.Information
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.ApkBrowserViewModel

class ApksSearch : KeyboardScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var searchBox: TypeFaceEditText
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var searchContainer: LinearLayout

    private lateinit var apkBrowserViewModel: ApkBrowserViewModel
    private lateinit var adapterApksSearch: AdapterApksSearch

    private var displayHeight: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_music_search, container, false)

        recyclerView = view.findViewById(R.id.search_recycler_view)
        searchBox = view.findViewById(R.id.search_box)
        clear = view.findViewById(R.id.clear)
        searchContainer = view.findViewById(R.id.search_container)
        apkBrowserViewModel = ViewModelProvider(requireActivity())[ApkBrowserViewModel::class.java]

        displayHeight = StatusBarHeight.getDisplayHeight(requireContext()) +
                StatusBarHeight.getStatusBarHeight(requireContext().resources)

        //        val params = searchContainer.layoutParams as ViewGroup.MarginLayoutParams
        //        params.setMargins(params.leftMargin,
        //                          StatusBarHeight.getStatusBarHeight(resources) + params.topMargin,
        //                          params.rightMargin,
        //                          params.bottomMargin)
        //
        //        recyclerView.setPadding(recyclerView.paddingLeft,
        //                                recyclerView.paddingTop + params.topMargin + params.height + params.bottomMargin,
        //                                recyclerView.paddingRight,
        //                                recyclerView.paddingBottom)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        searchBox.setText(ApkBrowserPreferences.getSearchKeyword())
        searchBox.setWindowInsetsAnimationCallback()
        clearButtonState()

        if (requireArguments().getBoolean(BundleConstants.isKeyboardOpened, false).invert()) {
            searchBox.showInput()
            requireArguments().putBoolean(BundleConstants.isKeyboardOpened, true)
        }

        apkBrowserViewModel.getSearchResults().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            adapterApksSearch = AdapterApksSearch(
                    it, searchBox.text.toString(),
                    requireArguments().getString(BundleConstants.transitionName, ""),
                    requireArguments().getInt(BundleConstants.position, 0))

            adapterApksSearch.setOnItemClickListener(object : AdapterCallbacks {
                override fun onApkClicked(view: View, position: Int, icon: ImageView) {
                    val uri = FileProvider.getUriForFile(
                            /* context = */ requireActivity().applicationContext,
                            /* authority = */ "${requireContext().packageName}.provider",
                            /* file = */ adapterApksSearch.paths[position].file)

                    val intent = Intent(requireContext(), ApkInstallerActivity::class.java)
                    intent.setDataAndType(uri, "application/vnd.android.package-archive")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    icon.transitionName = uri.toString()

                    if (BehaviourPreferences.isArcAnimationOn()) {
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), icon, icon.transitionName)
                        startActivity(intent, options.toBundle())
                    } else {
                        startActivity(intent)
                    }
                }

                override fun onApkLongClicked(view: View, position: Int, icon: ImageView) {
                    PopupApkBrowser(requireView()).setPopupApkBrowserCallbacks(object : PopupApkBrowser.Companion.PopupApkBrowserCallbacks {
                        override fun onInstallClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireActivity().applicationContext,
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ adapterApksSearch.paths[position].file)

                            val intent = Intent(requireContext(), ApkInstallerActivity::class.java)
                            intent.setDataAndType(uri, "application/vnd.android.package-archive")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            icon.transitionName = uri.toString()

                            if (BehaviourPreferences.isArcAnimationOn()) {
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), icon, icon.transitionName)
                                startActivity(intent, options.toBundle())
                            } else {
                                startActivity(intent)
                            }
                        }

                        override fun onDeleteClicked() {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    if (adapterApksSearch.paths[position].file.delete()) {
                                        apkBrowserViewModel.delete(adapterApksSearch.paths[position])
                                        adapterApksSearch.paths.removeAt(position)
                                        adapterApksSearch.notifyItemRemoved(position)
                                    }
                                }
                            })
                        }

                        override fun onSendClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireContext(),
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ adapterApksSearch.paths[position].file)
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "application/vnd.android.package-archive"
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(Intent.createChooser(intent, it[position].file.absolutePath.substringAfterLast("/")))
                        }

                        override fun onManifestClicked() {
                            val uri = FileProvider.getUriForFile(
                                    /* context = */ requireContext(),
                                    /* authority = */ "${requireContext().packageName}.provider",
                                    /* file = */ adapterApksSearch.paths[position].file)
                            val intent = Intent(requireContext(), ManifestAssociationActivity::class.java)
                            intent.setDataAndType(uri, "application/vnd.android.package-archive")
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(Intent.createChooser(intent, it[position].file.absolutePath.substringAfterLast("/")))
                        }

                        override fun onInfoClicked() {
                            kotlin.runCatching {
                                if (adapterApksSearch.paths[position].file.absolutePath.endsWith(".apk")) {
                                    packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        requirePackageManager().getPackageArchiveInfo(
                                                adapterApksSearch.paths[position]
                                                    .file.absolutePath, PackageManager.PackageInfoFlags.of(PackageUtils.flags))!!
                                    } else {
                                        requirePackageManager().getPackageArchiveInfo(
                                                adapterApksSearch.paths[position].file.absolutePath, PackageUtils.flags.toInt())!!
                                    }

                                    packageInfo.applicationInfo.sourceDir = adapterApksSearch.paths[position].file.absolutePath
                                } else {
                                    packageInfo = PackageInfo() // empty package info
                                    packageInfo.applicationInfo = ApplicationInfo() // empty application info
                                    packageInfo.applicationInfo.sourceDir = adapterApksSearch.paths[position].file.absolutePath
                                }

                                if (packageInfo.isInstalled()) {
                                    packageInfo = requirePackageManager().getPackageInfo(packageInfo.packageName)!!
                                    icon.transitionName = packageInfo.packageName
                                    requireArguments().putString(BundleConstants.transitionName, icon.transitionName)
                                    requireArguments().putInt(BundleConstants.position, position)
                                    packageInfo.applicationInfo.name = it[position].file.absolutePath.substringAfterLast("/")
                                    openFragmentArc(AppInfo.newInstance(packageInfo), icon, AppInfo.TAG)
                                } else {
                                    openFragmentSlide(Information.newInstance(packageInfo), AppInfo.TAG)
                                }
                            }.onFailure {
                                showWarning("Failed to open apk : ${
                                    adapterApksSearch
                                        .paths[position].file.absolutePath.substringAfterLast("/")
                                }", false)
                            }
                        }

                        override fun onSelectClicked() {
                            /* no-op */
                        }
                    })
                }
            })

            recyclerView.adapter = adapterApksSearch

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                if (text.isNullOrEmpty()) {
                    apkBrowserViewModel.search("")
                } else {
                    apkBrowserViewModel.search(text.toString())
                }
            }

            ApkBrowserPreferences.setSearchKeyword(text.toString())

            clearButtonState()
        }

        clear.setOnClickListener {
            searchBox.setText("")
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
        fun newInstance(): ApksSearch {
            val args = Bundle()
            val fragment = ApksSearch()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "ApksSearch"
    }
}
