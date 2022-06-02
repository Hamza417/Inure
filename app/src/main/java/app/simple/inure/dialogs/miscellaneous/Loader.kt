package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class Loader : ScopedBottomSheetFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_loader, container, false)
    }

    companion object {
        fun newInstance(): Loader {
            val args = Bundle()
            val fragment = Loader()
            fragment.arguments = args
            return fragment
        }
    }
}