package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.viewmodels.factory.FontViewModelFactory
import app.simple.inure.viewmodels.viewers.FontData

class Font : ScopedFragment() {

    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var fontEditText: EditText
    private lateinit var fontName: TypeFaceTextView
    private lateinit var viewModel: FontData
    private lateinit var fontViewModelFactory: FontViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_font_viewer, container, false)

        scrollView = view.findViewById(R.id.font_viewer_scroll_view)
        fontEditText = view.findViewById(R.id.ttf_viewer)
        fontName = view.findViewById(R.id.ttf_name)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        fontViewModelFactory = FontViewModelFactory(requireActivity().application,
                                                    requireArguments().getString("path")!!,
                                                    applicationInfo,
                                                    requireContext().resolveAttrColor(R.attr.colorAppAccent))

        viewModel = ViewModelProvider(this, fontViewModelFactory).get(FontData::class.java)

        FastScrollerBuilder(scrollView).useMd2Style().build()

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
            /**
             * The header font style is set to app's default to
             * balance the design consistency
             */
            // fontName.typeface = it
            fontEditText.typeface = it
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo, path: String): Font {
            val args = Bundle()
            args.putString("path", path)
            args.putParcelable("application_info", applicationInfo)
            val fragment = Font()
            fragment.arguments = args
            return fragment
        }
    }
}