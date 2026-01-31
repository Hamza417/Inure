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
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.subpanels.ClassSourceViewModelFactory
import app.simple.inure.popups.viewers.PopupXmlViewer
import app.simple.inure.viewmodels.subviewers.ClassSourceViewModel
import java.io.IOException

class ClassSource : ScopedFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var text: TypeFaceEditText

    private lateinit var classSourceViewModelFactory: ClassSourceViewModelFactory
    private lateinit var classSourceViewModel: ClassSourceViewModel

    private val exportText = registerForActivityResult(ActivityResultContracts.CreateDocument(MimeConstants.javaType)) { uri: Uri? ->
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
        val view = inflater.inflate(R.layout.fragment_tracker_source_viewer, container, false)

        name = view.findViewById(R.id.tracker_class_name)
        options = view.findViewById(R.id.tracker_viewer_options)
        text = view.findViewById(R.id.tracker_viewer)

        classSourceViewModelFactory = ClassSourceViewModelFactory(requireArguments().getString(BundleConstants.CLASS_NAME)!!, packageInfo)
        classSourceViewModel = ViewModelProvider(this, classSourceViewModelFactory)[ClassSourceViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        name.text = requireArguments().getString(BundleConstants.CLASS_NAME) ?: getString(R.string.not_available)

        classSourceViewModel.getSourceData().observe(viewLifecycleOwner) {
            text.setText(it)
        }

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("tracker_source", text.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }

                        getString(R.string.export) -> {
                            val fileName: String = packageInfo.packageName + "_" + name.text
                            exportText.launch(fileName)
                        }
                    }
                }
            })
        }
    }

    companion object {
        fun newInstance(className: String, packageInfo: PackageInfo): ClassSource {
            val args = Bundle()
            args.putString(BundleConstants.CLASS_NAME, className)
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            val fragment = ClassSource()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "ClassSource"
    }
}
