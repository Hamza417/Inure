package app.simple.inure.ui.viewers

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.MimeConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.menus.CodeViewerMenu
import app.simple.inure.dialogs.miscellaneous.LargeString.Companion.showLargeStringDialog
import app.simple.inure.extensions.fragments.FinderScopedFragment
import app.simple.inure.factories.panels.XMLViewerViewModelFactory
import app.simple.inure.popups.viewers.PopupXmlViewer
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.XMLViewerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class XML : FinderScopedFragment() {

    private lateinit var text: TypeFaceEditText
    private lateinit var icon: ImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var progress: CustomProgressBar
    private lateinit var options: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var search: DynamicRippleImageButton

    private lateinit var componentsViewModel: XMLViewerViewModel
    private lateinit var applicationInfoFactory: XMLViewerViewModelFactory

    private val exportManifest = registerForActivityResult(CreateDocument(MimeConstants.xmlType)) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(text.text.toString().toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_xml_viewer, container, false)

        text = view.findViewById(R.id.text_viewer)
        name = view.findViewById(R.id.xml_name)
        icon = view.findViewById(R.id.xml_viewer_header_icon)
        progress = view.findViewById(R.id.xml_loader)
        options = view.findViewById(R.id.xml_viewer_options)
        settings = view.findViewById(R.id.xml_viewer_settings)
        scrollView = view.findViewById(R.id.xml_nested_scroll_view)
        search = view.findViewById(R.id.search)

        name.text = requireArguments().getString(BundleConstants.PATH_TO_XML)!!

        applicationInfoFactory = XMLViewerViewModelFactory(packageInfo,
                                                           requireArguments().getString(BundleConstants.PATH_TO_XML)!!,
                                                           requireArguments().getBoolean(BundleConstants.IS_RAW, false))

        componentsViewModel = ViewModelProvider(this, applicationInfoFactory)[XMLViewerViewModel::class.java]

        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun getScrollView(): PaddingAwareNestedScrollView {
        return scrollView
    }

    override fun getEditText(): TypeFaceEditText {
        return text
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getBoolean(BundleConstants.IS_MANIFEST)) {
            icon.setImageResource(R.drawable.ic_android)
        } else {
            icon.setImageResource(R.drawable.ic_file_xml)
        }

        startPostponedEnterTransition()

        componentsViewModel.getSpanned().observe(viewLifecycleOwner) {
            if (it.length > FormattingPreferences.getLargeStringLimit()) {
                childFragmentManager.showLargeStringDialog(it.length) {
                    postDelayed {
                        val params = TextViewCompat.getTextMetricsParams(text)

                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                            val precomputedText = PrecomputedTextCompat.create(it, params)
                            launch(Dispatchers.Main) {
                                TextViewCompat.setPrecomputedText(text, precomputedText)
                                progress.gone()
                                options.visible(true)
                                settings.visible(true)
                                search.visible(animate = true)
                            }
                        }
                    }
                }
            } else {
                postDelayed {
                    val params = TextViewCompat.getTextMetricsParams(text)

                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                        val precomputedText = PrecomputedTextCompat.create(it, params)
                        launch(Dispatchers.Main) {
                            TextViewCompat.setPrecomputedText(text, precomputedText)
                            progress.gone()
                            options.visible(true)
                            settings.visible(true)
                            search.visible(animate = true)
                        }
                    }
                }
            }
        }

        componentsViewModel.getError().observe(viewLifecycleOwner) {
            progress.gone()
            showError(it)
        }

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", text.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.export) -> {
                            try {
                                val name = with(name.text.toString()) {
                                    substring(lastIndexOf("/") + 1, length)
                                }

                                val fileName: String = packageInfo.packageName + "_" + name
                                exportManifest.launch(fileName)
                            } catch (e: ActivityNotFoundException) {
                                showWarning(Warnings.getActivityNotFoundWarning())
                            }
                        }
                    }
                }
            })
        }

        settings.setOnClickListener {
            CodeViewerMenu.newInstance()
                .show(childFragmentManager, "code_viewer_menu")
        }

        search.setOnClickListener {
            changeSearchState()
        }
    }

    companion object {
        /**
         * @param packageInfo: PackageInfo of the app
         * @param isManifest: true if the xml is manifest
         * @param pathToXml: path to the xml file
         * @param isRaw: true if the file is specified directly from the
         *               app and not needed to be fetched from the [PackageInfo],
         *               If true, [packageInfo] can be null however it's recommended
         *               to pass the empty [PackageInfo] object
         */
        fun newInstance(packageInfo: PackageInfo?, isManifest: Boolean, pathToXml: String?, isRaw: Boolean = false): XML {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            args.putBoolean(BundleConstants.IS_MANIFEST, isManifest)
            args.putBoolean(BundleConstants.IS_RAW, isRaw)
            args.putString(BundleConstants.PATH_TO_XML, pathToXml)
            val fragment = XML()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "xml_viewer"
    }
}
