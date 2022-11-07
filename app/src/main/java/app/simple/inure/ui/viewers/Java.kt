package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.MimeConstants
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.CodeViewModelFactory
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.JavaViewModel
import java.io.IOException

class Java : KeyboardScopedFragment() {

    private lateinit var java: TypeFaceEditText
    private lateinit var name: TypeFaceTextView
    private lateinit var progressBar: CustomProgressBar
    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var codeViewModelFactory: CodeViewModelFactory
    private lateinit var javaViewModel: JavaViewModel

    private var path: String? = null

    private val exportManifest = registerForActivityResult(ActivityResultContracts.CreateDocument(MimeConstants.javaType)) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(java.text.toString().toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_java, container, false)

        java = view.findViewById(R.id.java_viewer)
        name = view.findViewById(R.id.java_name)
        scrollView = view.findViewById(R.id.java_nested_scroll_view)
        progressBar = view.findViewById(R.id.java_loader)
        options = view.findViewById(R.id.java_viewer_options)

        path = requireArguments().getString(BundleConstants.pathToJava)!!

        codeViewModelFactory = CodeViewModelFactory(requireActivity().application,
                                                    packageInfo,
                                                    requireContext().resolveAttrColor(R.attr.colorAppAccent),
                                                    path!!)

        javaViewModel = ViewModelProvider(this, codeViewModelFactory)[JavaViewModel::class.java]

        startPostponedEnterTransition()
        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name.text = path

        javaViewModel.getSpanned().observe(viewLifecycleOwner) {
            java.setText(it)
            progressBar.gone()
            options.visible(true)
        }

        javaViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", java.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.save) -> {
                            val fileName: String = packageInfo.packageName + "_" + name.text
                            exportManifest.launch(fileName)
                        }
                    }
                }
            })
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, path: String): Java {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.pathToJava, path)
            val fragment = Java()
            fragment.arguments = args
            return fragment
        }
    }
}