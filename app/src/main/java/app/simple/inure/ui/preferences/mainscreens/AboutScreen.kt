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
import app.simple.inure.dialogs.miscellaneous.QRCode
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.panels.WebPage
import app.simple.inure.util.FragmentHelper

class AboutScreen : ScopedFragment() {

    private lateinit var changelogs: DynamicRippleRelativeLayout
    private lateinit var github: DynamicRippleRelativeLayout
    private lateinit var userAgreement: DynamicRippleRelativeLayout
    private lateinit var credits: DynamicRippleRelativeLayout
    private lateinit var translation: DynamicRippleRelativeLayout
    private lateinit var licenses: DynamicRippleRelativeLayout
    private lateinit var telegram: DynamicRippleRelativeLayout
    private lateinit var qrCode: DynamicRippleLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        changelogs = view.findViewById(R.id.changelogs)
        github = view.findViewById(R.id.about_github)
        userAgreement = view.findViewById(R.id.user_agreement)
        credits = view.findViewById(R.id.credits)
        translation = view.findViewById(R.id.about_translation)
        licenses = view.findViewById(R.id.licenses)
        telegram = view.findViewById(R.id.about_telegram)
        qrCode = view.findViewById(R.id.about_share)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        credits.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager,
                                        WebPage.newInstance(getString(R.string.credits)),
                                        "web_page")
        }

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/Hamza417/Inure")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        translation.setOnClickListener {
            val uri: Uri = Uri.parse("https://crowdin.com/project/inure")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        changelogs.setOnClickListener {
            clearTransitions()
            FragmentHelper.openFragment(parentFragmentManager,
                                        WebPage.newInstance(getString(R.string.change_logs)),
                                        "web_page")
        }

        licenses.setOnClickListener {
            clearTransitions()
            FragmentHelper.openFragment(parentFragmentManager,
                                        WebPage.newInstance(getString(R.string.open_source_licenses)),
                                        "web_page")
        }

        userAgreement.setOnClickListener {
            clearTransitions()
            FragmentHelper.openFragment(parentFragmentManager,
                                        WebPage.newInstance(getString(R.string.user_agreements)),
                                        "web_page")
        }

        telegram.setOnClickListener {
            val uri: Uri = Uri.parse("https://t.me/inure_app_manager")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        qrCode.setOnClickListener {
            QRCode.newInstance()
                .show(childFragmentManager, "qr_code")
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