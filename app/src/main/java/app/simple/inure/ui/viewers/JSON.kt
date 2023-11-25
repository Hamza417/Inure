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
import app.simple.inure.dialogs.miscellaneous.LargeString.Companion.showLargeStringDialog
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.CodeViewModelFactory
import app.simple.inure.popups.viewers.PopupXmlViewer
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.JSONViewerViewModel
import java.io.IOException

class JSON : KeyboardScopedFragment() {

    private lateinit var json: TypeFaceEditText
    private lateinit var name: TypeFaceTextView
    private lateinit var progressBar: CustomProgressBar
    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var codeViewModelFactory: CodeViewModelFactory
    private lateinit var jsonViewerViewModel: JSONViewerViewModel

    private var path: String? = null

    private val exportManifest = registerForActivityResult(ActivityResultContracts.CreateDocument(MimeConstants.jsonType)) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(json.text.toString().toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_json_viewer, container, false)

        json = view.findViewById(R.id.json_viewer)
        name = view.findViewById(R.id.json_name)
        scrollView = view.findViewById(R.id.json_nested_scroll_view)
        progressBar = view.findViewById(R.id.json_loader)
        options = view.findViewById(R.id.json_viewer_options)

        path = requireArguments().getString(BundleConstants.pathToJSON)!!

        codeViewModelFactory = CodeViewModelFactory(packageInfo,
                                                    requireContext().resolveAttrColor(R.attr.colorAppAccent),
                                                    path!!,
                                                    requireArguments().getBoolean(BundleConstants.isRaw, false))

        jsonViewerViewModel = ViewModelProvider(this, codeViewModelFactory)[JSONViewerViewModel::class.java]

        startPostponedEnterTransition()

        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name.text = path

        jsonViewerViewModel.getSpanned().observe(viewLifecycleOwner) {
            if (it.length > FormattingPreferences.getLargeStringLimit()) {
                childFragmentManager.showLargeStringDialog(it.length) {
                    postDelayed {
                        json.setText(it)
                    }
                }
            } else {
                json.setText(it)
            }

            progressBar.gone()
            options.visible(true)
        }

        jsonViewerViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", json.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.export) -> {
                            val name = with(name.text.toString()) {
                                substring(lastIndexOf("/") + 1, length)
                            }

                            val fileName: String = packageInfo.packageName + "_" + name
                            exportManifest.launch(fileName)
                        }
                    }
                }
            })
        }
    }

    companion object {

        /**
         * @param packageInfo: PackageInfo of the app
         * @param path: Path to the JSON file
         * @param isRaw: true if the file is specified directly from the
         *               app and not needed to be fetched from the [PackageInfo],
         *               If true, [packageInfo] can be null however it's recommended
         *               to pass the empty [PackageInfo] object
         */
        fun newInstance(packageInfo: PackageInfo, path: String, isRaw: Boolean = false): JSON {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.pathToJSON, path)
            args.putBoolean(BundleConstants.isRaw, isRaw)
            val fragment = JSON()
            fragment.arguments = args
            return fragment
        }
    }
}