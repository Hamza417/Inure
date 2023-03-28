package app.simple.inure.ui.panels

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.*
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterApks
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.apks.ApkScanner
import app.simple.inure.dialogs.apks.ApkScanner.Companion.showApkScanner
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.popups.apks.PopupApkBrowser
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
                    })
                }

                override fun onApkLongClicked(view: View, position: Int) {

                }
            })

            recyclerView.adapter = adapterApks

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.apkBrowserMenu, recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_refresh -> {
                        apkScanner = childFragmentManager.showApkScanner()
                        apkBrowserViewModel.refresh()
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(firstLaunch = true), "search")
                    }
                }
            }

            val tracker = SelectionTracker
                .Builder(
                        "Path Selection",
                        recyclerView,
                        ApkPathKeyProvider(),
                        ApkPathDetailsLookup(recyclerView),
                        StorageStrategy.createStringStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build()

            tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    tracker.hasSelection().let {
                        if (it) {
                            bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.apkBrowserMenuSelection)
                        } else {
                            bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.apkBrowserMenu)
                        }
                    }
                }
            })

            adapterApks.tracker = tracker
        }
    }

    private fun updateBottomMenu(isSelected: Boolean) {
        if (isSelected) {
            bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.apkBrowserMenuSelection)
        } else {
            bottomRightCornerMenu?.updateBottomMenu(BottomMenuConstants.apkBrowserMenu)
        }
    }

    inner class ApkPathDetailsLookup(private val recyclerView: CustomVerticalRecyclerView) : ItemDetailsLookup<String>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
            try {
                val view = recyclerView.findChildViewUnder(event.x, event.y)
                if (view != null) {
                    return (recyclerView.getChildViewHolder(view) as AdapterApks.Holder).getItemDetails()
                }
            } catch (e: java.lang.ClassCastException) {
                e.printStackTrace()
            }
            return null
        }
    }

    inner class ApkPathKeyProvider : ItemKeyProvider<String>(SCOPE_MAPPED) {
        override fun getKey(position: Int): String {
            Log.d("AdapterApks", "getKey: $position")
            return try {
                adapterApks.paths[position]
            } catch (e: IndexOutOfBoundsException) {
                "" // Return empty string if position is out of bounds
            }
        }

        override fun getPosition(key: String): Int {
            return adapterApks.paths.indexOf(key).plus(1)
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