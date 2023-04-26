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
import app.simple.inure.models.Tracker
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ParcelUtils.parcelableArrayList
import app.simple.inure.util.ParcelUtils.serializable

class TrackerSelector : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var enable: DynamicRippleTextView
    private lateinit var disable: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    private var trackerSelectorCallbacks: TrackerSelectorCallbacks? = null

    private var paths: ArrayList<Tracker> = arrayListOf()
    private var selectedPaths: ArrayList<Tracker> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_selector_tracker, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        close = view.findViewById(R.id.close)
        enable = view.findViewById(R.id.enable)
        disable = view.findViewById(R.id.disable)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paths = requireArguments().parcelableArrayList<Tracker>(BundleConstants.trackers)!!

        if (savedInstanceState.isNotNull()) {
            selectedPaths = savedInstanceState?.serializable(BundleConstants.selectedTrackers)!!
        } else {
            selectedPaths.addAll(paths)
        }

        val adapterSplitApkSelector = AdapterTrackerSelector(paths, selectedPaths)

        adapterSplitApkSelector.setTrackerSelectorCallbacks(object : AdapterTrackerSelector.Companion.TrackerSelectorCallbacks {
            override fun onTrackerSelected(tracker: Tracker, isChecked: Boolean) {
                if (isChecked) {
                    selectedPaths.add(tracker)
                } else {
                    selectedPaths.remove(tracker)
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(BundleConstants.selectedTrackers, selectedPaths)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(trackers: ArrayList<Tracker>): TrackerSelector {
            val args = Bundle()
            args.putParcelableArrayList(BundleConstants.trackers, trackers)
            val fragment = TrackerSelector()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showTrackerSelector(trackers: ArrayList<Tracker>, trackerSelectorCallbacks: TrackerSelectorCallbacks) {
            val trackerSelector = newInstance(trackers)
            trackerSelector.setTrackerSelectorCallbacks(trackerSelectorCallbacks)
            trackerSelector.show(this, trackerSelector.tag)
        }

        interface TrackerSelectorCallbacks {
            fun onEnableSelected(paths: ArrayList<Tracker>)
            fun onDisableSelected(paths: ArrayList<Tracker>)
        }
    }
}