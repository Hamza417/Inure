package app.simple.inure.ui.preferences.mainscreens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.extension.fragments.ScopedFragment

class AboutScreen : ScopedFragment() {

    private lateinit var github: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        github = view.findViewById(R.id.about_github)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Inure")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    companion object {
        fun newInstance(): AboutScreen {
            val args = Bundle()
            val fragment = AboutScreen()
            fragment.arguments = args
            return fragment
        }
    }
}