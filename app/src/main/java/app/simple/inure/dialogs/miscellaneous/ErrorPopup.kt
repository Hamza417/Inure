package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceEditText
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment

class ErrorPopup : ScopedBottomSheetFragment() {

    private lateinit var error: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_error, container, false)

        error = view.findViewById(R.id.print_error)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        error.setText(requireArguments().getString("error")!!)
    }

    companion object {
        fun newInstance(error: String): ErrorPopup {
            val args = Bundle()
            args.putString("error", error)
            val fragment = ErrorPopup()
            fragment.arguments = args
            return fragment
        }
    }
}
