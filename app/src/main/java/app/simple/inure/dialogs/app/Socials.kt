package app.simple.inure.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.glide.util.ImageLoader.loadDrawable
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser

class Socials : ScopedBottomSheetFragment() {

    private lateinit var avatar: ImageView
    private lateinit var twitter: DynamicRippleTextView
    private lateinit var instagram: DynamicRippleTextView
    private lateinit var github: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_socials, container, false)

        avatar = view.findViewById(R.id.avatar)
        twitter = view.findViewById(R.id.twitter)
        instagram = view.findViewById(R.id.instagram)
        github = view.findViewById(R.id.github)
        close = view.findViewById(R.id.close)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatar.loadDrawable(R.drawable.avatar)

        twitter.setOnClickListener {
            getString(R.string.twitter_profile_link).asUri().openInBrowser(requireContext())
        }

        instagram.setOnClickListener {
            getString(R.string.instagram_profile_link).asUri().openInBrowser(requireContext())
        }

        github.setOnClickListener {
            getString(R.string.github_profile_link).asUri().openInBrowser(requireContext())
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(): Socials {
            val args = Bundle()
            val fragment = Socials()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showSocialsDialog(): Socials {
            val dialog = newInstance()
            dialog.show(this, "socials")
            return dialog
        }
    }
}