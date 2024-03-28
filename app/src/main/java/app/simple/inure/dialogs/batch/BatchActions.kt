package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import app.simple.inure.R
import app.simple.inure.adapters.batch.AdapterBatchActions
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.menus.BatchActionsCallback
import app.simple.inure.preferences.ConfigurationPreferences

class BatchActions : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterBatchActions: AdapterBatchActions
    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var batchActionsCallback: BatchActionsCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_actions, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterBatchActions = AdapterBatchActions(getBatchMenu())
        gridLayoutManager = GridLayoutManager(requireContext(), 5)
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (getBatchMenu()[position].first == -1) {
                    5
                } else {
                    1
                }
            }
        }

        adapterBatchActions.setBatchActionsCallbackListener { id, icon ->
            batchActionsCallback.onBatchMenuItemClicked(id, icon).also {
                dismiss()
            }
        }

        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapterBatchActions
    }

    private fun getBatchMenu(): ArrayList<Pair<Int, Int>> {
        return when {
            ConfigurationPreferences.isUsingRoot() -> {
                batchMenuRoot
            }
            ConfigurationPreferences.isUsingShizuku() -> {
                batchMenuShizuku
            }
            else -> {
                batchMenuNonRoot
            }
        }
    }

    fun setBatchActionCallbackListener(batchActionsCallback: BatchActionsCallback) {
        this.batchActionsCallback = batchActionsCallback
    }

    companion object {
        private const val TAG = "BatchActions"

        private val divider = Pair(-1, -1)
        private val refresh = Pair(R.drawable.ic_refresh, R.string.refresh)

        fun newInstance(): BatchActions {
            val args = Bundle()
            val fragment = BatchActions()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchActions(): BatchActions {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }

        private val batchMenuNonRoot: ArrayList<Pair<Int, Int>> by lazy {
            arrayListOf(
                    Pair(R.drawable.ic_delete, R.string.uninstall),
                    divider,
                    Pair(R.drawable.ic_downloading, R.string.extract),
                    Pair(R.drawable.ic_text_snippet, R.string.generate_apps_list),
                    Pair(R.drawable.ic_tags, R.string.tags),
                    divider,
                    Pair(R.drawable.ic_checklist, R.string.checklist),
                    Pair(R.drawable.ic_select_all, R.string.select_all),
                    divider,
                    refresh,
            )
        }

        private val batchMenuRoot: ArrayList<Pair<Int, Int>> by lazy {
            arrayListOf(
                    Pair(R.drawable.ic_settings_power, R.string.battery),
                    Pair(R.drawable.ic_radiation_nuclear, R.string.trackers),
                    divider,
                    Pair(R.drawable.ic_hide_source, R.string.state),
                    Pair(R.drawable.ic_delete, R.string.uninstall),
                    Pair(R.drawable.ic_broom, R.string.clear_cache),
                    Pair(R.drawable.ic_close, R.string.force_stop),
                    divider,
                    Pair(R.drawable.ic_downloading, R.string.extract),
                    Pair(R.drawable.ic_text_snippet, R.string.generate_apps_list),
                    Pair(R.drawable.ic_tags, R.string.tags),
                    divider,
                    Pair(R.drawable.ic_checklist, R.string.checklist),
                    Pair(R.drawable.ic_select_all, R.string.select_all),
                    divider,
                    refresh,
            )
        }

        private val batchMenuShizuku: ArrayList<Pair<Int, Int>> by lazy {
            arrayListOf(
                    Pair(R.drawable.ic_settings_power, R.string.battery),
                    // Pair(R.drawable.ic_radiation_nuclear, R.string.trackers),
                    divider,
                    Pair(R.drawable.ic_hide_source, R.string.state),
                    Pair(R.drawable.ic_delete, R.string.uninstall),
                    Pair(R.drawable.ic_close, R.string.force_stop),
                    divider,
                    Pair(R.drawable.ic_downloading, R.string.extract),
                    Pair(R.drawable.ic_text_snippet, R.string.generate_apps_list),
                    Pair(R.drawable.ic_tags, R.string.tags),
                    divider,
                    Pair(R.drawable.ic_checklist, R.string.checklist),
                    Pair(R.drawable.ic_select_all, R.string.select_all),
                    divider,
                    refresh,
            )
        }
    }
}
