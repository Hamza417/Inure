package app.simple.inure.dialogs.miscellaneous

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.dialog.ErrorViewModelFactory
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.dialogs.ErrorViewModel

class Error : ScopedBottomSheetFragment() {

    private lateinit var error: TypeFaceTextView
    private lateinit var copy: DynamicRippleImageButton
    private var errorDialogCallbacks: ErrorDialogCallbacks? = null
    private lateinit var errorViewModel: ErrorViewModel

    private var spanned: Spanned? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_error, container, false)

        error = view.findViewById(R.id.print_error)
        copy = view.findViewById(R.id.copy_button)

        val errorViewModelFactory = ErrorViewModelFactory(requireApplication(),
                                                          requireArguments().getString("error")!!,
                                                          requireContext().resolveAttrColor(R.attr.colorAppAccent))

        errorViewModel = ViewModelProvider(this, errorViewModelFactory)[ErrorViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        errorViewModel.getSpanned().observe(viewLifecycleOwner, {
            error.text = it
            spanned = it
            copy.visible(true)
        })

        copy.setOnClickListener {
            val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Error", spanned)
            clipBoard.setPrimaryClip(clipData)

            if (clipBoard.hasPrimaryClip()) Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!requireActivity().isDestroyed) {
            errorDialogCallbacks?.onDismiss()
        }
    }

    fun setOnErrorDialogCallbackListener(errorDialogCallbacks: ErrorDialogCallbacks) {
        this.errorDialogCallbacks = errorDialogCallbacks
    }

    companion object {
        fun newInstance(error: String): Error {
            val args = Bundle()
            args.putString("error", error)
            val fragment = Error()
            fragment.arguments = args
            return fragment
        }

        interface ErrorDialogCallbacks {
            fun onDismiss()
        }
    }
}