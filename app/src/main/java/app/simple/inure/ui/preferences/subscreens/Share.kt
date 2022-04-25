package app.simple.inure.ui.preferences.subscreens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.AboutPreferences

class Share : ScopedFragment() {

    private lateinit var share: DynamicRippleTextView
    private lateinit var reset: DynamicRippleTextView
    private lateinit var message: TypeFaceEditTextDynamicCorner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_share, container, false)

        share = view.findViewById(R.id.share)
        reset = view.findViewById(R.id.reset)
        message = view.findViewById(R.id.share_edit_text)

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        message.setText(AboutPreferences.getShareMessage())

        message.doOnTextChanged { text, _, _, _ ->
            AboutPreferences.setShareMessage(text.toString())
        }

        share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage = "\n${message.text ?: "Let me recommend you this really good looking applications manager app."}\n\n"
            shareMessage = "${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        }

        reset.setOnClickListener {
            message.setText("Let me recommend you this really good looking applications manager app.")
        }
    }

    companion object {
        fun newInstance(): Share {
            val args = Bundle()
            val fragment = Share()
            fragment.arguments = args
            return fragment
        }
    }
}