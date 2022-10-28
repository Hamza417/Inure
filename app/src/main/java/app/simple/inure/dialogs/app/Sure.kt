package app.simple.inure.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.fragments.SureCallbacks

class Sure : ScopedBottomSheetFragment() {

    private lateinit var sure: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView
    private var sureCallbacks: SureCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sure, container, false)

        sure = view.findViewById(R.id.sure)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sure.setOnClickListener {
            sureCallbacks?.onSure().also {
                dismiss()
            }
        }

        cancel.setOnClickListener {
            sureCallbacks?.onCancel().also {
                dismiss()
            }
        }
    }

    fun setOnSureCallbackListener(sureCallbacks: SureCallbacks) {
        this.sureCallbacks = sureCallbacks
    }

    companion object {
        fun newInstance(): Sure {
            val args = Bundle()
            val fragment = Sure()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.newSureInstance(): Sure {
            val args = Bundle()
            val fragment = Sure()
            fragment.arguments = args
            fragment.show(this, "sure")
            return fragment
        }
    }
}