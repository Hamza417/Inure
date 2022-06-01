package app.simple.inure.dialogs.terminal

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.TerminalPreferences
import app.simple.inure.terminal.util.TermSettings

class DialogSpecialKeys : ScopedBottomSheetFragment() {

    private lateinit var text: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_special_keys, container, false)

        text = view.findViewById(R.id.text)

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text.text = (formatMessage(TerminalPreferences.getControlKey(), TermSettings.CONTROL_KEY_ID_NONE,
                                   resources, R.array.control_keys_short_names,
                                   R.string.control_key_dialog_control_text,
                                   R.string.control_key_dialog_control_disabled_text, "CTRLKEY")
                + "\n\n" +
                formatMessage(TerminalPreferences.getFnKey(), TermSettings.FN_KEY_ID_NONE,
                              resources, R.array.fn_keys_short_names,
                              R.string.control_key_dialog_fn_text,
                              R.string.control_key_dialog_fn_disabled_text, "FNKEY"))
    }

    private fun formatMessage(keyId: Int, disabledKeyId: Int, r: Resources, arrayId: Int, enabledId: Int, disabledId: Int, regex: String): String {
        if (keyId == disabledKeyId) {
            return r.getString(disabledId)
        }
        val keyNames = r.getStringArray(arrayId)
        val keyName = keyNames[keyId]
        val template = r.getString(enabledId)
        return template.replace(regex.toRegex(), keyName)
    }

    companion object {
        fun newInstance(): DialogSpecialKeys {
            val args = Bundle()
            val fragment = DialogSpecialKeys()
            fragment.arguments = args
            return fragment
        }
    }
}