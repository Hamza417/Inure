package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.FontViewModelFactory
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.FontViewModel

class Font : KeyboardScopedFragment() {

    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var fontEditText: EditText
    private lateinit var fontName: TypeFaceTextView
    private lateinit var fontViewModel: FontViewModel
    private lateinit var fontViewModelFactory: FontViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_font_viewer, container, false)

        scrollView = view.findViewById(R.id.font_viewer_scroll_view)
        fontEditText = view.findViewById(R.id.ttf_viewer)
        fontName = view.findViewById(R.id.ttf_name)

        fontViewModelFactory = FontViewModelFactory(requireArguments().getString(BundleConstants.path)!!, packageInfo)
        fontViewModel = ViewModelProvider(this, fontViewModelFactory).get(FontViewModel::class.java)

        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        fontName.text = requireArguments().getString(BundleConstants.path)

        fontViewModel.getQuote().observe(viewLifecycleOwner) {
            fontEditText.setText(it)
            fontEditText.visible(true)
        }

        fontViewModel.getTypeFace().observe(viewLifecycleOwner) {
            /**
             * The header font style is set to app's default to
             * balance the design consistency
             */
            // fontName.typeface = it
            fontEditText.typeface = it
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo, path: String): Font {
            val args = Bundle()
            args.putString(BundleConstants.path, path)
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            val fragment = Font()
            fragment.arguments = args
            return fragment
        }
    }
}