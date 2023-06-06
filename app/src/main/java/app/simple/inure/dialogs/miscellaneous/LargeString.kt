package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class LargeString : ScopedBottomSheetFragment() {

    private lateinit var warning: TypeFaceTextView
    private lateinit var yes: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    var onYes: () -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_large_string, container, false)

        warning = view.findViewById(R.id.warning)
        yes = view.findViewById(R.id.yes)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        warning.text = getString(R.string.large_string_warning, requireArguments().getInt(BundleConstants.size, -1))

        yes.setOnClickListener {
            onYes.invoke()
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        fun newInstance(size: Int): LargeString {
            val args = Bundle()
            args.putInt(BundleConstants.size, size)
            val fragment = LargeString()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showLargeStringDialog(size: Int, onYes: () -> Unit) {
            val dialog = newInstance(size)
            dialog.onYes = onYes
            dialog.show(this, "large_string_dialog")
        }
    }
}