package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.viewmodels.factory.FontViewModelFactory
import app.simple.inure.viewmodels.panels.FontData

class Font : ScopedFragment() {

    private lateinit var fontEditText: EditText
    private lateinit var fontName: TextView
    private lateinit var viewModel: FontData
    private lateinit var fontViewModelFactory: FontViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_font_viewer, container, false)

        fontEditText = view.findViewById(R.id.ttf_viewer)
        fontName = view.findViewById(R.id.ttf_name)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        fontViewModelFactory = FontViewModelFactory(requireActivity().application,
                                                    requireArguments().getString("path")!!,
                                                    applicationInfo,
                                                    requireContext().resolveAttrColor(R.attr.colorAppAccent))

        viewModel = ViewModelProvider(this, fontViewModelFactory).get(FontData::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        fontName.text = requireArguments().getString("path")

        viewModel.getQuote().observe(viewLifecycleOwner, {
            fontEditText.setText(it)
        })

        viewModel.getTypeFace().observe(viewLifecycleOwner, {
            fontEditText.setTypeface(it, Typeface.NORMAL)
        })
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