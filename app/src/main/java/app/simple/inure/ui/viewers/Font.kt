package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.Quotes
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.TTFHelper
import app.simple.inure.util.TextViewUtils.toHtmlSpanned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Font : ScopedFragment() {

    private lateinit var fontEditText: EditText
    private lateinit var fontName: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_font_viewer, container, false)

        fontEditText = view.findViewById(R.id.ttf_viewer)
        fontName = view.findViewById(R.id.ttf_name)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        fontName.text = requireArguments().getString("path")

        val quotes = Quotes

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val typeFace = TTFHelper.getTTFFile(
                requireArguments().getString("path")!!,
                applicationInfo, requireContext())

            val spanned = quotes.quotes.random().replace("%%%", getAccentColor(requireContext().resolveAttrColor(R.attr.colorAppAccent))).toHtmlSpanned()

            withContext(Dispatchers.Main) {
                fontEditText.setTypeface(typeFace, Typeface.NORMAL)
                fontName.setTypeface(typeFace, Typeface.NORMAL)
                fontEditText.setText(spanned)
            }
        }
    }

    private fun getAccentColor(intColor: Int): String {
        return java.lang.String.format("#%06X", 0xFFFFFF and intColor)
    }

    companion object {
        fun newInstance(path: String, applicationInfo: ApplicationInfo): Font {
            val args = Bundle()
            args.putString("path", path)
            args.putParcelable("application_info", applicationInfo)
            val fragment = Font()
            fragment.arguments = args
            return fragment
        }
    }
}