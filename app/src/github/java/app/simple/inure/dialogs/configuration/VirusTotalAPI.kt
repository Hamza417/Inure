package app.simple.inure.dialogs.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.VirusTotalPreferences
import app.simple.inure.preferences.VirusTotalPreferences.validateAPI
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser

class VirusTotalAPI : ScopedBottomSheetFragment() {

    private lateinit var box: DynamicCornerEditText
    private lateinit var getAPIKey: DynamicRippleTextView
    private lateinit var save: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var virusTotalAPIListener: onVirusTotalAPIListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_virustotal_api, container, false)

        box = view.findViewById(R.id.text_box)
        getAPIKey = view.findViewById(R.id.get_api_key)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        save.alpha = 0.5f
        save.isEnabled = false

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        box.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                if (text.toString().validateAPI()) {
                    save.alpha = 1f
                    save.isEnabled = true
                } else {
                    save.alpha = 0.5f
                    save.isEnabled = false
                }
            } else {
                save.alpha = 0.5f
                save.isEnabled = false
            }
        }

        save.setOnClickListener {
            if (box.text.toString().validateAPI()) {
                val apiKey = box.text.toString()
                VirusTotalPreferences.setVirusTotalApiKey(apiKey)
                virusTotalAPIListener?.onVirusTotalAPI()
                dismiss()
            }
        }

        cancel.setOnClickListener {
            dismiss()
        }

        getAPIKey.setOnClickListener {
            "https://docs.virustotal.com/docs/api-overview"
                .asUri()
                .openInBrowser(requireContext())
        }
    }

    fun setOnVirusTotalAPIListener(listener: onVirusTotalAPIListener) {
        this.virusTotalAPIListener = listener
    }

    companion object {
        const val TAG = "VirusTotalAPI"

        fun newInstance(): VirusTotalAPI {
            return VirusTotalAPI()
        }

        fun FragmentManager.showVirusTotalAPI(): VirusTotalAPI {
            val dialog = newInstance()
            if (findFragmentByTag(TAG) == null) {
                dialog.show(this, TAG)
            }
            return dialog
        }

        interface onVirusTotalAPIListener {
            fun onVirusTotalAPI()
        }
    }
}