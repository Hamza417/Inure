package app.simple.inure.dialogs.action

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.StateViewModelFactory
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.viewmodels.dialogs.StateViewModel

class State : ScopedBottomSheetFragment() {

    private lateinit var mode: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView
    private lateinit var loader: LoaderImageView
    private lateinit var status: TypeFaceTextView

    var onSuccess: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_enable_disable, container, false)

        mode = view.findViewById(R.id.mode)
        packageName = view.findViewById(R.id.package_name)
        loader = view.findViewById(R.id.loader)
        status = view.findViewById(R.id.state_result)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ConfigurationPreferences.isUsingRoot()) {
            mode.setText(R.string.root)
        } else if (ConfigurationPreferences.isUsingShizuku()) {
            mode.setText(R.string.shizuku)
        }

        packageName.text = packageInfo.packageName

        with(ViewModelProvider(this, StateViewModelFactory(packageInfo))[StateViewModel::class.java]) {
            getResults().observe(viewLifecycleOwner) {

            }

            getSuccessStatus().observe(viewLifecycleOwner) {
                when (it) {
                    "Done" -> {
                        loader.loaded()
                        if (getApplication<Application>().packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                            packageInfo.applicationInfo.enabled = true
                            status.setText(R.string.enabled)
                        } else {
                            packageInfo.applicationInfo.enabled = false
                            status.setText(R.string.disabled)
                        }
                        onSuccess?.invoke()
                    }
                    "Failed" -> {
                        loader.error()
                        status.setText(R.string.failed)
                    }
                }
            }

            getWarning().observe(viewLifecycleOwner) {
                showWarning(it)
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): State {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = State()
            fragment.arguments = args
            return fragment
        }
    }
}