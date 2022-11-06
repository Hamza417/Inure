package app.simple.inure.dialogs.miscellaneous

import android.content.DialogInterface
import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.dialog.ErrorViewModelFactory
import app.simple.inure.interfaces.fragments.ErrorCallbacks
import app.simple.inure.popups.viewers.PopupInformation
import app.simple.inure.viewmodels.dialogs.ErrorViewModel

class Error : ScopedBottomSheetFragment() {

    private lateinit var error: DynamicRippleTextView
    private var errorCallbacks: ErrorCallbacks? = null
    private lateinit var errorViewModel: ErrorViewModel

    private var spanned: Spanned? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_error, container, false)

        error = view.findViewById(R.id.print_error)

        val errorViewModelFactory = ErrorViewModelFactory(requireArguments().getString(BundleConstants.error)!!)
        errorViewModel = ViewModelProvider(this, errorViewModelFactory)[ErrorViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        errorViewModel.getSpanned().observe(viewLifecycleOwner) { spanned ->
            error.text = spanned
            this.spanned = spanned

            error.setOnClickListener {
                PopupInformation(it, spanned.toString())
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!requireActivity().isDestroyed) {
            errorCallbacks?.onDismiss()
        }
    }

    fun setOnErrorCallbackListener(errorCallbacks: ErrorCallbacks) {
        this.errorCallbacks = errorCallbacks
    }

    companion object {
        fun newInstance(error: String): Error {
            val args = Bundle()
            args.putString(BundleConstants.error, error)
            val fragment = Error()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showError(error: String): Error {
            val errorDialog = newInstance(error)
            errorDialog.show(this, "error_dialog")
            return errorDialog
        }
    }
}