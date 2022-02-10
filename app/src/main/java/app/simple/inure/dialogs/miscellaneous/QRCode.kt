package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedDialogFragment

class QRCode : ScopedDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_app_share, container, false)
    }

    companion object {
        fun newInstance(): QRCode {
            val args = Bundle()
            val fragment = QRCode()
            fragment.arguments = args
            return fragment
        }
    }
}