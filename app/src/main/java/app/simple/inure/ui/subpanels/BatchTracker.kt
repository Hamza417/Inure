package app.simple.inure.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.batch.AdapterBatchTracker
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.batch.BatchTrackersFactory
import app.simple.inure.models.Tracker
import app.simple.inure.ui.subviewers.TrackerInfo
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.batch.BatchTrackersViewModel

class BatchTracker : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var selectAll: DynamicRippleImageButton
    private lateinit var loader: LoaderImageView
    private lateinit var progress: TypeFaceTextView
    private lateinit var block: DynamicRippleTextView
    private lateinit var unblock: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    private var batchTrackersViewModel: BatchTrackersViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_batch_tracker, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        selectAll = view.findViewById(R.id.select_all)
        loader = view.findViewById(R.id.loader)
        progress = view.findViewById(R.id.progress)
        block = view.findViewById(R.id.block)
        unblock = view.findViewById(R.id.unblock)
        close = view.findViewById(R.id.close)

        val batchTrackersFactory = BatchTrackersFactory(requireArguments().getStringArrayList(BundleConstants.packageId)!!)
        batchTrackersViewModel = ViewModelProvider(this, batchTrackersFactory)[BatchTrackersViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        loader.visible(animate = false)

        batchTrackersViewModel?.getTrackers()?.observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            progress.gone(animate = true)

            if (it.isNotEmpty()) {
                selectAll.visible(animate = true)
                block.visible(animate = true)
                unblock.visible(animate = true)
            }

            recyclerView.adapter = AdapterBatchTracker(it, object : AdapterBatchTracker.Companion.BatchTrackerCallbacks {
                override fun onBatchLongPressed(tracker: Tracker) {
                    openFragmentSlide(TrackerInfo.newInstance(tracker), TrackerInfo.TAG)
                }
            })
        }

        batchTrackersViewModel?.getProgress()?.observe(viewLifecycleOwner) {
            progress.text = it
        }

        batchTrackersViewModel?.getWarning()?.observe(viewLifecycleOwner) {
            showWarning(it)
        }

        batchTrackersViewModel?.getError()?.observe(viewLifecycleOwner) {
            showError(it)
        }

        block.setOnClickListener {
            loader.visible(animate = true)
            batchTrackersViewModel
                ?.changeTrackerState((recyclerView.adapter as AdapterBatchTracker).getSelectedPackages(), true) {
                    postDelayed {
                        loader.gone(animate = true)
                        showWarning(getString(R.string.done), false)
                    }
                }
        }

        unblock.setOnClickListener {
            loader.visible(animate = true)
            batchTrackersViewModel
                ?.changeTrackerState((recyclerView.adapter as AdapterBatchTracker).getSelectedPackages(), false) {
                    postDelayed {
                        loader.gone(animate = true)
                        showWarning(getString(R.string.done), false)
                    }
                }
        }

        selectAll.setOnClickListener {
            if ((recyclerView.adapter as AdapterBatchTracker).isAllSelected()) {
                (recyclerView.adapter as AdapterBatchTracker).unselectAll()
            } else {
                (recyclerView.adapter as AdapterBatchTracker).selectAll()
            }
        }

        close.setOnClickListener {
            popBackStack()
        }
    }

    companion object {
        fun newInstance(packages: ArrayList<String>): BatchTracker {
            val args = Bundle()
            args.putStringArrayList(BundleConstants.packageId, packages)
            val fragment = BatchTracker()
            fragment.arguments = args
            return fragment
        }
    }
}