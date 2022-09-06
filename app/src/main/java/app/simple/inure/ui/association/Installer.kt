package app.simple.inure.ui.association

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.association.InstallerViewModelFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.association.InstallerViewModel

class Installer : ScopedFragment() {

    private lateinit var installerViewModel: InstallerViewModel

    private lateinit var icon: AppIconImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView
    private lateinit var install: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView
    private lateinit var update: DynamicRippleTextView
    private lateinit var uninstall: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_installer, container, false)

        icon = view.findViewById(R.id.icon)
        name = view.findViewById(R.id.name)
        packageName = view.findViewById(R.id.package_id)
        install = view.findViewById(R.id.install)
        cancel = view.findViewById(R.id.cancel)
        update = view.findViewById(R.id.update)
        uninstall = view.findViewById(R.id.uninstall)

        val factory = InstallerViewModelFactory(requireArguments().getParcelable(BundleConstants.uri)!!)
        installerViewModel = ViewModelProvider(this, factory)[InstallerViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        installerViewModel.getPackageInfo().observe(viewLifecycleOwner) {
            packageInfo = it

            name.text = packageInfo.applicationInfo.name
            packageName.text = buildString {
                append(packageInfo.packageName)
                append(" (${packageInfo.versionName})")
            }

            if (PackageUtils.isPackageInstalled(packageInfo.packageName, requireContext().packageManager)) {
                install.gone()
                update.visible(true)
                uninstall.visible(true)
            } else {
                install.visible(true)
                update.gone()
                uninstall.gone()
            }

            install.setOnClickListener {
                installerViewModel.install()
            }

            update.setOnClickListener {
                installerViewModel.install()
            }

            uninstall.setOnClickListener {
                Toast.makeText(requireContext(), "Not yet implemented", Toast.LENGTH_SHORT).show()
            }
        }

        installerViewModel.getFile().observe(viewLifecycleOwner) {
            icon.loadAppIcon(it)
        }

        installerViewModel.installing.observe(viewLifecycleOwner) {
            if (it.isZero()) {
                uninstall.visible(animate = false)
                cancel.setText(R.string.close)
                cancel.visible(animate = false)
            } else {
                install.gone()
                cancel.gone()
            }
        }

        cancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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