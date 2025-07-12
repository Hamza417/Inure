package app.simple.inure.dialogs.terminal

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.typeface.CustomTypefaceSpan
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.TerminalPreferences
import app.simple.inure.themes.manager.ThemeManager

class TerminalSpecialKeys : ScopedBottomSheetFragment() {

    private lateinit var text: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_special_keys, container, false)
        text = view.findViewById(R.id.text)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controlKeyName = controlKeysShortNames[TerminalPreferences.getControlKey()]
        val fnKeyName = fnKeysShortNames[TerminalPreferences.getFnKey()]

        val builder = SpannableStringBuilder()
        builder.append(
                buildFormattedKeys(
                        controlKeyName,
                        controlKeyMappings,
                        AppearancePreferences.getAccentColor(),
                        "Ctrl Key Mappings"
                )
        )

        builder.append(
                buildFormattedKeys(
                        fnKeyName,
                        fnKeyMappings,
                        AppearancePreferences.getAccentColor(),
                        "Fn Key Mappings"
                )
        )

        text.setText(builder.trim(), TextView.BufferType.SPANNABLE)
    }

    private fun buildFormattedKeys(keyName: String, template: List<KeyMapping>, keyColor: Int, title: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.source_code_pro_regular)!!

        val titleStart = builder.length
        builder.append(title + "\n")
        builder.setSpan(StyleSpan(Typeface.BOLD), titleStart, builder.length, flag)
        builder.append("\n")

        val maxKeyLength = template.maxOfOrNull { mapping ->
            mapping.key.replace(Regex("(CTRLKEY|FNKEY)"), keyName).length
        } ?: 0

        for (mapping in template) {
            val key = mapping.key.replace(Regex("(CTRLKEY|FNKEY)"), keyName)
            val paddedKey = key.padEnd(maxKeyLength)
            val description = mapping.description.uppercase()
            val line = "$paddedKey : $description"
            val start = builder.length
            val keyStart = start
            val keyEnd = keyStart + paddedKey.length

            builder.append(line)

            // Use custom typeface, color, and bold for key
            builder.setSpan(CustomTypefaceSpan(typeface), keyStart, builder.length, flag)
            builder.setSpan(ForegroundColorSpan(keyColor), keyStart, keyEnd, flag)
            builder.setSpan(StyleSpan(Typeface.BOLD), keyStart, keyEnd, flag)

            // Color for description
            val descStart = line.indexOf(':') + start
            builder.setSpan(ForegroundColorSpan(ThemeManager.theme.textViewTheme.tertiaryTextColor), descStart, builder.length, flag)

            builder.append("\n")
        }

        builder.append("\n")
        return builder
    }

    companion object {
        fun newInstance(): TerminalSpecialKeys {
            val args = Bundle()
            val fragment = TerminalSpecialKeys()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showTerminalSpecialKeys(): TerminalSpecialKeys {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }

        private data class KeyMapping(val key: String, val description: String)

        private val controlKeyMappings = listOf(
                KeyMapping("CTRLKEY + Space", "Control-@ (NUL)"),
                KeyMapping("CTRLKEY + A..Z", "Control-A..Z"),
                KeyMapping("CTRLKEY + 5", "Control-]"),
                KeyMapping("CTRLKEY + 6", "Control-^"),
                KeyMapping("CTRLKEY + 7", "Control-_"),
                KeyMapping("CTRLKEY + 9", "F11"),
                KeyMapping("CTRLKEY + 0", "F12")
        )

        private val fnKeyMappings = listOf(
                KeyMapping("FNKEY + 1..9", "F1-F9"),
                KeyMapping("FNKEY + 0", "F10"),
                KeyMapping("FNKEY + W", "Up"),
                KeyMapping("FNKEY + A", "Left"),
                KeyMapping("FNKEY + S", "Down"),
                KeyMapping("FNKEY + D", "Right"),
                KeyMapping("FNKEY + P", "PageUp"),
                KeyMapping("FNKEY + N", "PageDown"),
                KeyMapping("FNKEY + T", "Tab"),
                KeyMapping("FNKEY + L", "| (pipe)"),
                KeyMapping("FNKEY + U", "_ (underscore)"),
                KeyMapping("FNKEY + E", "Control-[ (ESC)"),
                KeyMapping("FNKEY + X", "Delete"),
                KeyMapping("FNKEY + I", "Insert"),
                KeyMapping("FNKEY + H", "Home"),
                KeyMapping("FNKEY + F", "End"),
                KeyMapping("FNKEY + .", "Control-\\")
        )

        private val controlKeysShortNames = arrayOf(
                "Ball",
                "@",
                "Left Alt",
                "Right Alt",
                "Volume Up",
                "Volume Down",
                "Camera",
                "None"
        )

        private val fnKeysShortNames = arrayOf(
                "Ball",
                "@",
                "Left Alt",
                "Right Alt",
                "Volume Up",
                "Volume Down",
                "Camera",
                "None"
        )

        private const val TAG = "TerminalSpecialKeys"
    }
}