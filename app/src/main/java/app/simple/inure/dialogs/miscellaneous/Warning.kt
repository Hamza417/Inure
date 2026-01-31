package app.simple.inure.dialogs.miscellaneous

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.fragments.WarningCallbacks

class Warning : ScopedBottomSheetFragment() {

    private lateinit var warning: TypeFaceTextView
    private var warningCallbacks: WarningCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_simple_warning, container, false)

        warning = view.findViewById(R.id.warning)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kotlin.runCatching {
            warning.setText(requireArguments().getInt(BundleConstants.WARNING))
        }.getOrElse {
            warning.text = requireArguments().getString(BundleConstants.WARNING)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        try {
            if (!requireActivity().isDestroyed) {
                warningCallbacks?.onWarningDismissed()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun setOnWarningCallbackListener(warningCallbacks: WarningCallbacks) {
        this.warningCallbacks = warningCallbacks
    }

    companion object {
        fun newInstance(warning: String): Warning {
            val args = Bundle()
            args.putString(BundleConstants.WARNING, warning)
            val fragment = Warning()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(warning: Int): Warning {
            val args = Bundle()
            args.putInt(BundleConstants.WARNING, warning)
            val fragment = Warning()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showWarning(warning: String): Warning {
            val fragment = newInstance(warning)

            try {
                fragment.show(this, TAG)
            } catch (e: IllegalStateException) {
                val transaction = beginTransaction()
                transaction.setReorderingAllowed(true)
                transaction.add(fragment, TAG)
                transaction.commitAllowingStateLoss()
            }

            return fragment
        }

        fun FragmentManager.showWarning(@StringRes warning: Int): Warning {
            val fragment = newInstance(warning)
            try {
                fragment.show(this, TAG)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                val transaction = beginTransaction()
                transaction.setReorderingAllowed(true)
                transaction.add(fragment, TAG)
                transaction.commitAllowingStateLoss()
            }

            return fragment
        }

        const val TAG = "Warning"
    }
}
