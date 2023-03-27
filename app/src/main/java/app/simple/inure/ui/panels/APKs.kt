package app.simple.inure.ui.panels

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterApks
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.viewmodels.panels.ApkBrowserViewModel

class APKs : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var apkBrowserViewModel: ApkBrowserViewModel
    private lateinit var adapterApks: AdapterApks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_apk_browser, container, false)

        recyclerView = view.findViewById(R.id.apks_recycler_view)

        apkBrowserViewModel = ViewModelProvider(requireActivity())[ApkBrowserViewModel::class.java]

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        showLoader(manualOverride = true)

        adapterApks = AdapterApks()
        recyclerView.adapter = adapterApks

        apkBrowserViewModel.getApkPaths().observe(viewLifecycleOwner) {
            hideLoader()

            adapterApks.apps = it
            adapterApks.notifyDataSetChanged()
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