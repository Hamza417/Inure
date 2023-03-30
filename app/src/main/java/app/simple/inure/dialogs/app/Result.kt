package app.simple.inure.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class Result : ScopedBottomSheetFragment() {

    private lateinit var result: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(app.simple.inure.R.layout.dialog_result, container, false)

        result = view.findViewById(app.simple.inure.R.id.result)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        result.text = arguments?.getString(BundleConstants.result)
    }

    companion object {
        fun newInstance(result: String): Result {
            val args = Bundle()
            args.putString(BundleConstants.result, result)
            val fragment = Result()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showResult(result: String): Result {
            val dialog = newInstance(result)
            dialog.show(this, "result")
            return dialog
        }
    }
}