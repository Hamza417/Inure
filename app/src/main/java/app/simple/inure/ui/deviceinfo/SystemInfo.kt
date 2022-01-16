package app.simple.inure.ui.deviceinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedFragment

class SystemInfo : ScopedFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.device_info_system, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()
    }

    companion object {
        fun newInstance(): SystemInfo {
            val args = Bundle()
            val fragment = SystemInfo()
            fragment.arguments = args
            return fragment
        }
    }
}