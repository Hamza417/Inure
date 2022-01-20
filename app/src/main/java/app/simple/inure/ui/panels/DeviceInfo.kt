package app.simple.inure.ui.panels

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.menus.AdapterMenu
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.deviceinfo.SystemInfo
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.deviceinfo.PanelItemsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceInfo : ScopedFragment() {

    private lateinit var panels: RecyclerView

    private lateinit var availableRam: TypeFaceTextView
    private lateinit var usedRam: TypeFaceTextView

    private lateinit var ramIndicator: CustomProgressBar

    private lateinit var search: DynamicRippleImageButton
    private lateinit var popup: DynamicRippleImageButton

    private lateinit var adapterPanelItems: AdapterMenu
    private val panelItemsViewModel: PanelItemsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_device_info, container, false)

        availableRam = view.findViewById(R.id.analytics_total_ram)
        usedRam = view.findViewById(R.id.analytics_total_used)
        popup = view.findViewById(R.id.device_info_option_button)
        search = view.findViewById(R.id.device_info_search_button)

        ramIndicator = view.findViewById(R.id.analytics_ram_progress_bar)

        panels = view.findViewById(R.id.device_info_panel_rv)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        panelItemsViewModel.getPanelItems().observe(viewLifecycleOwner, {
            postponeEnterTransition()

            adapterPanelItems = AdapterMenu(it)

            adapterPanelItems.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.system) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        SystemInfo.newInstance(),
                                                        icon, "system_info")
                        }
                    }
                }
            })

            panels.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
            panels.adapter = adapterPanelItems
            panels.scheduleLayoutAnimation()

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        })

        popup.setOnClickListener {

        }

        search.setOnClickListener {
            clearEnterTransition()
            clearExitTransition()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Search.newInstance(true),
                                        "search")
        }
    }

    private fun setRamAnalytics() {
        viewLifecycleOwner.lifecycleScope.launch {
            val available: Long
            val used: Long

            withContext(Dispatchers.Default) {
                val mi = ActivityManager.MemoryInfo()
                val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.getMemoryInfo(mi)

                available = mi.totalMem
                used = mi.totalMem - mi.availMem
            }

            ramIndicator.max = (available / 1000).toInt()
            ramIndicator.setProgress((used / 1000).toInt(), animate = true, fromStart = false)

            availableRam.text = available.toSize()
            usedRam.text = used.toSize()
        }
    }

    private val ramRunnable = object : Runnable {
        override fun run() {
            setRamAnalytics()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(ramRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ramRunnable)
        ramIndicator.clearAnimation()
    }

    companion object {
        fun newInstance(): DeviceInfo {
            val args = Bundle()
            val fragment = DeviceInfo()
            fragment.arguments = args
            return fragment
        }
    }
}
