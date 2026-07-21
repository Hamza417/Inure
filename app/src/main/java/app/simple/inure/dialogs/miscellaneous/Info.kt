package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class Info : ScopedBottomSheetFragment() {

    private lateinit var info: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_simple_info, container, false)

        info = view.findViewById(R.id.info)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        info.text = requireArguments().getString(BundleConstants.WARNING)
    }

    companion object {
        fun newInstance(info: String): Info {
            val args = Bundle()
            args.putString(BundleConstants.WARNING, info)
            val fragment = Info()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showInfo(warning: String): Info {
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

        const val TAG = "Info"
    }
}
