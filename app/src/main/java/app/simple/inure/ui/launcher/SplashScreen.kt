package app.simple.inure.ui.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.app.Apps
import app.simple.inure.util.FragmentHelper.openFragment
import app.simple.inure.viewmodels.panels.AppData

class SplashScreen : ScopedFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appData: AppData by viewModels()

        appData.getAppData().observe(viewLifecycleOwner, {
            openFragment(
                requireActivity().supportFragmentManager,
                Apps.newInstance(), view.findViewById(R.id.imageView))
        })
    }

    companion object {
        fun newInstance(): SplashScreen {
            val args = Bundle()
            val fragment = SplashScreen()
            fragment.arguments = args
            return fragment
        }
    }
}