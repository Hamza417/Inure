package app.simple.inure.dialogs.configuration

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedDialogFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.DateUtils
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import java.text.ParseException

class DateFormat : ScopedDialogFragment() {

    private lateinit var format: TypeFaceEditTextDynamicCorner
    private lateinit var update: TypeFaceTextView
    private lateinit var save: DynamicRippleTextView
    private lateinit var reset: DynamicRippleTextView
    private lateinit var cheatsheet: DynamicRippleTextView

    private val date = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_date_format, container, false)

        format = view.findViewById(R.id.date_format_edit_text)
        update = view.findViewById(R.id.date_format_update)
        save = view.findViewById(R.id.date_format_save)
        reset = view.findViewById(R.id.date_format_default)
        cheatsheet = view.findViewById(R.id.date_format_cheatsheet)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        format.doOnTextChanged { text, _, _, count ->
            kotlin.runCatching {
                if (count.isZero()) throw ParseException(getString(R.string.unknown), 0)
                update.text = DateUtils.formatDate(date, text.toString())
                if (save.visibility == View.GONE) save.visible(true)
            }.getOrElse {
                update.text = it.message
                save.gone()
            }
        }

        format.setText(ConfigurationPreferences.getDateFormat())

        save.setOnClickListener {
            ConfigurationPreferences.setDateFormat(format.text.toString())
            dismiss()
        }

        cheatsheet.setOnClickListener {
            val uri: Uri = Uri.parse("https://developer.android.com/reference/kotlin/java/text/SimpleDateFormat")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        reset.setOnClickListener {
            with("EEE, yyyy MMM dd, hh:mm a") {
                ConfigurationPreferences.setDateFormat(this)
                format.setText(this)
            }
        }
    }

    companion object {
        fun newInstance(): DateFormat {
            val args = Bundle()
            val fragment = DateFormat()
            fragment.arguments = args
            return fragment
        }
    }
}