package app.simple.inure.ui.preferences.mainscreens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.ui.preferences.subscreens.Share

class AboutScreen : ScopedFragment() {

    private lateinit var changelogs: DynamicRippleRelativeLayout
    private lateinit var github: DynamicRippleRelativeLayout
    private lateinit var userAgreement: DynamicRippleRelativeLayout
    private lateinit var credits: DynamicRippleRelativeLayout
    private lateinit var translation: DynamicRippleRelativeLayout
    private lateinit var licenses: DynamicRippleRelativeLayout
    private lateinit var privacyPolicy: DynamicRippleLinearLayout
    private lateinit var telegram: DynamicRippleRelativeLayout
    private lateinit var share: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_about, container, false)

        changelogs = view.findViewById(R.id.changelogs)
        github = view.findViewById(R.id.about_github)
        userAgreement = view.findViewById(R.id.user_agreement)
        credits = view.findViewById(R.id.credits)
        translation = view.findViewById(R.id.about_translation)
        licenses = view.findViewById(R.id.licenses)
        privacyPolicy = view.findViewById(R.id.toc)
        telegram = view.findViewById(R.id.about_telegram)
        share = view.findViewById(R.id.about_share)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        credits.setOnClickListener {
            openWebPage(getString(R.string.credits))
        }

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Inure")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        translation.setOnClickListener {
            openWebPage(getString(R.string.translate))
        }

        changelogs.setOnClickListener {
            openWebPage(getString(R.string.change_logs))
        }

        licenses.setOnClickListener {
            openWebPage(getString(R.string.open_source_licenses))
        }

        userAgreement.setOnClickListener {
            openWebPage(getString(R.string.user_agreements))
        }

        privacyPolicy.setOnClickListener {
            openWebPage(getString(R.string.privacy_policy))
        }

        telegram.setOnClickListener {
            val uri: Uri = Uri.parse("https://t.me/inure_app_manager")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        share.setOnClickListener {
            openFragmentSlide(Share.newInstance(), "share")
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