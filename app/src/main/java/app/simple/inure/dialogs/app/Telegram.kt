package app.simple.inure.dialogs.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class Telegram : ScopedBottomSheetFragment() {

    private lateinit var group: DynamicRippleTextView
    private lateinit var channel: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_telegram, container, false)

        group = view.findViewById(R.id.group)
        channel = view.findViewById(R.id.channel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        group.setOnClickListener {
            val uri: Uri = Uri.parse("https://t.me/inure_app_grp")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        channel.setOnClickListener {
            val uri: Uri = Uri.parse("https://t.me/inure_app_manager")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    companion object {
        fun newInstance(): Telegram {
            val args = Bundle()
            val fragment = Telegram()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showTelegramDialog() {
            val dialog = newInstance()
            dialog.show(this, "Telegram")
        }
    }
}