package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.ComponentStateFactory
import app.simple.inure.viewmodels.dialogs.ComponentStateViewModel

class ComponentState : ScopedActionDialogBottomFragment() {

    private lateinit var componentStateFactory: ComponentStateFactory
    private lateinit var componentStateViewModel: ComponentStateViewModel
    private var componentStatusCallbacks: ComponentStatusCallbacks? = null

    private var mode: Boolean? = null
    private var packageId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        packageId = requireArguments().getString(BundleConstants.PACKAGE_ID)!!
        mode = requireArguments().getBoolean(BundleConstants.COMPONENT_MODE)

        componentStateFactory = ComponentStateFactory(packageInfo, packageId!!, mode!!)
        componentStateViewModel = ViewModelProvider(this, componentStateFactory)[ComponentStateViewModel::class.java]

        componentStateViewModel.getSuccessStatus().observe(viewLifecycleOwner) {
            when (it) {
                "Done" -> {
                    loader.loaded()

                    if (mode!!) {
                        status.setText(R.string.disabled)
                    } else {
                        status.setText(R.string.enabled)
                    }

                    componentStatusCallbacks?.onSuccess()
                }
                "Failed" -> {
                    loader.error()
                    status.setText(R.string.failed)
                }
            }
        }

        componentStateViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }

        componentStateViewModel.error.observe(viewLifecycleOwner) {
            showError(it)
        }
    }

    fun setOnComponentStateChangeListener(componentStatusCallbacks: ComponentStatusCallbacks) {
        this.componentStatusCallbacks = componentStatusCallbacks
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, packageId: String, isComponentEnabled: Boolean): ComponentState {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            args.putString(BundleConstants.PACKAGE_ID, packageId)
            args.putBoolean(BundleConstants.COMPONENT_MODE, isComponentEnabled)
            val fragment = ComponentState()
            fragment.arguments = args
            return fragment
        }

        fun Fragment.showComponentStateDialog(packageInfo: PackageInfo, packageId: String, isComponentEnabled: Boolean, componentStatusCallbacks: ComponentStatusCallbacks) {
            newInstance(packageInfo, packageId, isComponentEnabled).apply {
                setOnComponentStateChangeListener(componentStatusCallbacks)
            }.show(childFragmentManager, TAG)
        }

        interface ComponentStatusCallbacks {
            fun onSuccess()
        }

        const val TAG = "ComponentState"
    }
}
