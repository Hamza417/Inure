package app.simple.inure.dialogs.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.preferences.VirusTotalPreferences

class VirusTotalAPI : ScopedDialogFragment() {

    private lateinit var textBox: DynamicCornerEditText
    private lateinit var save: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_virustotal_api, container, false)

        textBox = view.findViewById(R.id.text_box)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        save.setOnClickListener {
            val apiKey = textBox.text.toString()
            VirusTotalPreferences.setVirusTotalApiKey(apiKey)
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "VirusTotalAPI"

        fun newInstance(): VirusTotalAPI {
            return VirusTotalAPI()
        }

        fun FragmentManager.showVirusTotalAPI() {
            val dialog = newInstance()
            if (findFragmentByTag(TAG) == null) {
                dialog.show(this, TAG)
            }
        }
    }
}