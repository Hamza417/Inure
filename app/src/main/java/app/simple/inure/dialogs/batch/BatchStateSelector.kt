package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class BatchStateSelector : ScopedBottomSheetFragment() {

    private lateinit var enableAll: DynamicRippleTextView
    private lateinit var disableAll: DynamicRippleTextView

    private var batchStateSelectorCallback: BatchStateSelectorCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_batch_state_selector, container, false)

        enableAll = view.findViewById(R.id.enable_all)
        disableAll = view.findViewById(R.id.disable_all)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableAll.setOnClickListener {
            batchStateSelectorCallback?.onEnableAll().also {
                dismiss()
            }
        }

        disableAll.setOnClickListener {
            batchStateSelectorCallback?.onDisableAll().also {
                dismiss()
            }
        }
    }

    fun onBatchStateSelectorCallback(callback: BatchStateSelectorCallback) {
        this.batchStateSelectorCallback = callback
    }

    companion object {
        const val TAG = "BatchStateSelector"

        fun newInstance(): BatchStateSelector {
            return BatchStateSelector()
        }

        interface BatchStateSelectorCallback {
            fun onEnableAll()
            fun onDisableAll()
        }

        fun FragmentManager.showBatchStateSelector(): BatchStateSelector {
            val batchStateSelector = newInstance()
            batchStateSelector.show(this, TAG)
            return batchStateSelector
        }
    }
}