package app.simple.inure.dialogs.action

import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.ExtractViewModelFactory
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.TextViewUtils.makeLinks
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.dialogs.ExtractViewModel

class Extract : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var progress: TypeFaceTextView
    private lateinit var status: TypeFaceTextView
    private lateinit var share: DynamicRippleTextView

    private lateinit var extractViewModel: ExtractViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_extract, container, false)

        loader = view.findViewById(R.id.extract_loader_indicator)
        progress = view.findViewById(R.id.extracting_progress)
        status = view.findViewById(R.id.extracting_updates)
        share = view.findViewById(R.id.share)

        val extractViewModelFactory = ExtractViewModelFactory(packageInfo, requireArguments().getStringArray(BundleConstants.paths)!!.toSet())
        extractViewModel = ViewModelProvider(this, extractViewModelFactory)[ExtractViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extractViewModel.getProgress().observe(viewLifecycleOwner) {
            progress.text = getString(R.string.progress, it)
        }

        extractViewModel.getStatus().observe(viewLifecycleOwner) {
            status.text = it
        }

        extractViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        extractViewModel.getSuccess().observe(viewLifecycleOwner) {
            if (it) {
                progress.text = getString(R.string.done)
                loader.loaded()
                share.visible(true)
            }
        }

        extractViewModel.getFile().observe(viewLifecycleOwner) { file ->
            status.text = getString(R.string.saved_to, file?.absolutePath)
            status.makeLinks(Pair(file?.absolutePath!!, View.OnClickListener {
                val selectedUri: Uri = Uri.parse(extractViewModel.getFile().value?.absolutePath)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(selectedUri, "resource/folder")

                if (intent.resolveActivityInfo(requireContext().packageManager, 0) != null) {
                    startActivity(intent)
                } else {
                    showWarning(Warnings.getNoFileExplorerWarning(), dismiss = false)
                    Log.d("Extract", "No file explorer app installed on your device")
                }
            }))

            share.setOnClickListener {
                kotlin.runCatching {
                    if (it.isNotNull()) {
                        ShareCompat.IntentBuilder(requireActivity())
                            .setStream(FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", file))
                            .setType("*/*")
                            .startChooser()

                        dismiss()
                    }
                }.getOrElse {
                    it.printStackTrace()
                    showError(it.stackTraceToString())
                }
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, paths: Set<String>): Extract {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putStringArray(BundleConstants.paths, paths.toTypedArray())
            val fragment = Extract()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.launchExtract(packageInfo: PackageInfo, paths: Set<String>) {
            newInstance(packageInfo, paths).show(this, "extract")
        }
    }
}