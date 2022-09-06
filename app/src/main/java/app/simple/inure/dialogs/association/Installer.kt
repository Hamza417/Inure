package app.simple.inure.dialogs.association

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.association.InstallerViewModelFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.viewmodels.association.InstallerViewModel

class Installer : ScopedBottomSheetFragment() {

    private lateinit var installerViewModel: InstallerViewModel

    private lateinit var icon: AppIconImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView
    private lateinit var positiveButton: DynamicRippleTextView
    private lateinit var negativeButton: DynamicRippleTextView

    private var update = false
    private var installed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_installer, container, false)

        icon = view.findViewById(R.id.icon)
        name = view.findViewById(R.id.name)
        packageName = view.findViewById(R.id.package_id)
        positiveButton = view.findViewById(R.id.install)
        negativeButton = view.findViewById(R.id.cancel)

        val factory = InstallerViewModelFactory(requireArguments().getParcelable(BundleConstants.uri)!!)
        installerViewModel = ViewModelProvider(this, factory)[InstallerViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        installerViewModel.getPackageInfo().observe(viewLifecycleOwner) {
            packageInfo = it
            name.text = packageInfo.applicationInfo.loadLabel(requireContext().packageManager)
            packageName.text = buildString {
                append(packageInfo.packageName)
                append(" (${packageInfo.versionName})")
            }
        }

        installerViewModel.getFile().observe(viewLifecycleOwner) {
            icon.loadAppIcon(it)
        }

        negativeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().finish()
    }

    companion object {
        fun newInstance(uri: Uri): Installer {
            val args = Bundle()
            args.putParcelable(BundleConstants.uri, uri)
            val fragment = Installer()
            fragment.arguments = args
            return fragment
        }
    }
}