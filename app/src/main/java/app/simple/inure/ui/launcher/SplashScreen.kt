package app.simple.inure.ui.launcher

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedFragment

class SplashScreen : ScopedFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_splash_screen, container, false)
        return view
    }

    override fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

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