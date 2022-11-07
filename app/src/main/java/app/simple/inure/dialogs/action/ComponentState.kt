package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.ComponentStateFactory
import app.simple.inure.viewmodels.dialogs.ComponentStateViewModel

class ComponentState : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var status: TypeFaceTextView

    private lateinit var componentStateFactory: ComponentStateFactory
    private lateinit var componentStateViewModel: ComponentStateViewModel
    private var componentStatusCallbacks: ComponentStatusCallbacks? = null

    private var mode: Boolean? = null
    private var packageId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_component_state, container, false)

        loader = view.findViewById(R.id.loader)
        status = view.findViewById(R.id.component_state_result)

        packageId = requireArguments().getString(BundleConstants.packageId)!!
        mode = requireArguments().getBoolean(BundleConstants.componentMode)

        componentStateFactory = ComponentStateFactory(packageInfo, packageId!!, mode!!)
        componentStateViewModel = ViewModelProvider(this, componentStateFactory).get(ComponentStateViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    fun setOnComponentStateChangeListener(componentStatusCallbacks: ComponentStatusCallbacks) {
        this.componentStatusCallbacks = componentStatusCallbacks
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, packageId: String, isComponentEnabled: Boolean): ComponentState {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.packageId, packageId)
            args.putBoolean(BundleConstants.componentMode, isComponentEnabled)
            val fragment = ComponentState()
            fragment.arguments = args
            return fragment
        }

        interface ComponentStatusCallbacks {
            fun onSuccess()
        }
    }
}