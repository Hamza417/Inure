package app.simple.inure.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.simple.inure.R
import app.simple.inure.adapters.batch.AdapterBatchInstaller
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.batch.BatchInstallerFactory
import app.simple.inure.models.BatchInstallerInfo
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.batch.BatchInstallerViewModel
import kotlinx.coroutines.launch

/**
 * Full-screen subpanel that shows a live list of APK files being installed in batch.
 *
 * Receives a list of APK file paths via [BundleConstants.PATHS], spins up
 * [BatchInstallerViewModel], and reflects real-time [BatchInstallerInfo.InstallState]
 * changes in [AdapterBatchInstaller].
 *
 * @author Hamza417
 */
class BatchInstaller : ScopedFragment() {

    private lateinit var state: TypeFaceTextView
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var loader: LoaderImageView
    private lateinit var progress: TypeFaceTextView
    private lateinit var close: DynamicRippleTextView

    private var batchInstallerViewModel: BatchInstallerViewModel? = null
    private var adapterBatchInstaller: AdapterBatchInstaller? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_batch_installer, container, false)

        state = view.findViewById(R.id.state)
        recyclerView = view.findViewById(R.id.recycler_view)
        loader = view.findViewById(R.id.loader)
        progress = view.findViewById(R.id.progress)
        close = view.findViewById(R.id.close)

        val paths = requireArguments().getStringArrayList(BundleConstants.PATHS)!!
        val factory = BatchInstallerFactory(paths)
        batchInstallerViewModel = ViewModelProvider(this, factory)[BatchInstallerViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        loader.visible(animate = false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                batchInstallerViewModel?.installList?.collect { list ->
                    if (list.isEmpty()) return@collect

                    // set installed out of total count text
                    val installedCount = list.count {
                        it.installState == BatchInstallerInfo.InstallState.INSTALLED
                    }

                    state.text = buildString {
                        append(getString(R.string.installed))
                        append(" $installedCount / ${list.size}")
                    }

                    if (adapterBatchInstaller == null) {
                        loader.gone(animate = true)
                        progress.gone(animate = true)
                        // Pass a fresh copy so the adapter has independent storage;
                        // subsequent snapshots from the flow diff against this copy correctly.
                        adapterBatchInstaller = AdapterBatchInstaller(ArrayList(list))
                        recyclerView.adapter = adapterBatchInstaller
                    } else {
                        adapterBatchInstaller?.updateResults(list)
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                batchInstallerViewModel?.progress?.collect { text ->
                    if (text.isNotEmpty()) progress.text = text
                }
            }
        }

        batchInstallerViewModel?.getWarning()?.observe(viewLifecycleOwner) {
            showWarning(it)
        }

        batchInstallerViewModel?.getError()?.observe(viewLifecycleOwner) {
            showError(it)
        }

        close.setOnClickListener {
            popBackStack()
        }
    }

    companion object {
        /**
         * Creates a new [BatchInstaller] fragment instance.
         *
         * @param paths List of absolute file paths for the APKs to install.
         * @return A configured [BatchInstaller] fragment.
         */
        fun newInstance(paths: ArrayList<String>): BatchInstaller {
            val args = Bundle()
            args.putStringArrayList(BundleConstants.PATHS, paths)
            val fragment = BatchInstaller()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "BatchInstaller"
    }
}
