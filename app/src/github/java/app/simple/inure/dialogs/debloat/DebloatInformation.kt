package app.simple.inure.dialogs.debloat

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.DebloatInfoViewModelFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.util.FileUtils.toFileOrNull
import app.simple.inure.utils.DebloatUtils.setBloatFlags
import app.simple.inure.viewmodels.dialogs.DebloatInfoViewModel

class DebloatInformation : ScopedBottomSheetFragment() {

    private lateinit var icon: AppIconImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView
    private lateinit var flags: TypeFaceTextView
    private lateinit var description: TypeFaceTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var uninstall: DynamicRippleTextView
    private lateinit var disable: DynamicRippleTextView

    private lateinit var debloatInfoViewModel: DebloatInfoViewModel
    private lateinit var listener: DebloatInfoListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_debloat_info, container, false)

        icon = view.findViewById(R.id.app_icon)
        name = view.findViewById(R.id.name)
        packageName = view.findViewById(R.id.package_id)
        flags = view.findViewById(R.id.flags)
        description = view.findViewById(R.id.desc)
        close = view.findViewById(R.id.close)
        uninstall = view.findViewById(R.id.uninstall)
        disable = view.findViewById(R.id.disable)

        val factory = DebloatInfoViewModelFactory(packageInfo)
        debloatInfoViewModel = ViewModelProvider(this, factory)[DebloatInfoViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debloatInfoViewModel.getBloat().observe(viewLifecycleOwner) { bloat ->
            TransitionManager.beginDelayedTransition(requireView().parent as ViewGroup)
            icon.loadAppIcon(packageInfo.packageName, packageInfo.safeApplicationInfo.enabled, packageInfo.safeApplicationInfo.sourceDir.toFileOrNull())
            name.text = packageInfo.safeApplicationInfo.name
            packageName.text = packageInfo.packageName
            flags.setBloatFlags(bloat)
            description.text = bloat.description
        }

        close.setOnClickListener {
            dismiss()
        }

        uninstall.setOnClickListener {
            listener.onUninstallRequested()
            dismiss()
        }

        disable.setOnClickListener {
            listener.onDisableRequested()
            dismiss()
        }
    }

    fun setDebloatInfoListener(listener: DebloatInfoListener) {
        this.listener = listener
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): DebloatInformation {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = DebloatInformation()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showDebloatInfoDialog(packageInfo: PackageInfo): DebloatInformation {
            val dialog = newInstance(packageInfo)
            dialog.show(this, TAG)
            return dialog
        }

        interface DebloatInfoListener {
            fun onUninstallRequested()
            fun onDisableRequested()
        }

        private const val TAG = "DebloatInformation"
    }
}