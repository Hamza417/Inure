package app.simple.inure.ui.preferences.mainscreens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.activities.app.WebPageViewerActivity
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.extension.fragments.ScopedFragment

class AboutScreen : ScopedFragment() {

    private lateinit var changelogs: DynamicRippleRelativeLayout
    private lateinit var github: DynamicRippleRelativeLayout
    private lateinit var translation: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        changelogs = view.findViewById(R.id.changelogs)
        github = view.findViewById(R.id.about_github)
        translation = view.findViewById(R.id.about_translation)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Inure")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        translation.setOnClickListener {

        }

        changelogs.setOnClickListener {
            val intent = Intent(requireActivity(), WebPageViewerActivity::class.java)
            intent.putExtra(BundleConstants.webPage, getString(R.string.change_logs))
            startActivity(intent)
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