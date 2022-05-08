package app.simple.inure.ui.subviewers

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
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.subpanels.TrackerSourceViewModelFactory
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.viewmodels.subviewers.TrackerSourceViewModel
import java.io.IOException

class TrackerSourceViewer() : ScopedFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var text: TypeFaceEditText

    private lateinit var trackerSourceViewModelFactory: TrackerSourceViewModelFactory
    private lateinit var trackerSourceViewModel: TrackerSourceViewModel

    private val exportText = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
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

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        trackerSourceViewModelFactory = TrackerSourceViewModelFactory(requireApplication(), requireArguments().getString(BundleConstants.className)!!, packageInfo)
        trackerSourceViewModel = ViewModelProvider(this, trackerSourceViewModelFactory)[TrackerSourceViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        name.text = requireArguments().getString(BundleConstants.className) ?: getString(R.string.not_available)

        trackerSourceViewModel.getSourceData().observe(viewLifecycleOwner) {
            text.setText(it)
        }

        options.setOnClickListener {
            val popupXmlViewer = PopupXmlViewer(it)

            popupXmlViewer.setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("tracker_source", text.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.save) -> {
                            val fileName: String = packageInfo.packageName + "_" + name.text
                            exportText.launch(fileName)
                        }
                    }
                }
            })
        }
    }

    companion object {
        fun newInstance(className: String, packageInfo: PackageInfo): TrackerSourceViewer {
            val args = Bundle()
            args.putString(BundleConstants.className, className)
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = TrackerSourceViewer()
            fragment.arguments = args
            return fragment
        }
    }
}