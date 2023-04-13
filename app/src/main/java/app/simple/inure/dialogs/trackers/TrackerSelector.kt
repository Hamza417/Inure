package app.simple.inure.dialogs.trackers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.dialogs.AdapterTrackerSelector
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class TrackerSelector : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var enable: DynamicRippleTextView
    private lateinit var disable: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    private var trackerSelectorCallbacks: TrackerSelectorCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_tracker_selector, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        close = view.findViewById(R.id.close)
        enable = view.findViewById(R.id.enable)
        disable = view.findViewById(R.id.disable)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedPaths = mutableSetOf<String>()
        val paths = requireArguments().getStringArrayList(BundleConstants.trackers)!!.let {
            val mutableList = mutableListOf<Pair<String, Boolean>>()
            it.forEach { path ->
                mutableList.add(Pair(path, true))
            }
            mutableList
        }

        selectedPaths.addAll(paths.map { it.first })

        val adapterSplitApkSelector = AdapterTrackerSelector(paths)

        adapterSplitApkSelector.setTrackerSelectorCallbacks(object : AdapterTrackerSelector.Companion.TrackerSelectorCallbacks {
            override fun onTrackerSelected(path: String, isChecked: Boolean) {
                if (isChecked) {
                    selectedPaths.add(path)
                } else {
                    selectedPaths.remove(path)
                }

                if (selectedPaths.isEmpty()) {
                    enable.isEnabled = false
                    disable.isEnabled = false
                    enable.animate().alpha(0.5f).setDuration(250).start()
                    disable.animate().alpha(0.5f).setDuration(250).start()
                } else {
                    enable.isEnabled = true
                    disable.isEnabled = true
                    enable.animate().alpha(1.0f).setDuration(250).start()
                    disable.animate().alpha(1.0f).setDuration(250).start()
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapterSplitApkSelector

        enable.setOnClickListener {
            trackerSelectorCallbacks?.onEnableSelected(selectedPaths)
            dismiss()
        }

        disable.setOnClickListener {
            trackerSelectorCallbacks?.onDisableSelected(selectedPaths)
            dismiss()
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    fun setTrackerSelectorCallbacks(trackerSelectorCallbacks: TrackerSelectorCallbacks) {
        this.trackerSelectorCallbacks = trackerSelectorCallbacks
    }

    companion object {
        fun newInstance(set: Set<String>): TrackerSelector {
            val args = Bundle()
            args.putStringArrayList(BundleConstants.trackers, ArrayList(set))
            val fragment = TrackerSelector()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showTrackerSelector(set: Set<String>, trackerSelectorCallbacks: TrackerSelectorCallbacks) {
            val trackerSelector = newInstance(set)
            trackerSelector.setTrackerSelectorCallbacks(trackerSelectorCallbacks)
            trackerSelector.show(this, trackerSelector.tag)
        }

        interface TrackerSelectorCallbacks {
            fun onEnableSelected(paths: Set<String>)
            fun onDisableSelected(paths: Set<String>)
        }
    }
}