package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.os.BundleCompat
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.getPermissionDescription
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.Button
import app.simple.inure.enums.AppOpMode
import app.simple.inure.enums.AppOpScope
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.AppOp
import com.google.android.material.button.MaterialButtonToggleGroup

class AppOpState : ScopedBottomSheetFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var description: TypeFaceTextView
    private lateinit var stateGroup: MaterialButtonToggleGroup
    private lateinit var scopeGroup: MaterialButtonToggleGroup
    private lateinit var allow: Button
    private lateinit var ignore: Button
    private lateinit var deny: Button
    private lateinit var apply: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    private var appOp: AppOp? = null
    private var updatedAppOp = AppOp()

    private var appOpStateCallbacks: AppOpStateCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_state_appop, container, false)

        name = view.findViewById(R.id.name)
        description = view.findViewById(R.id.description)
        allow = view.findViewById(R.id.allow)
        ignore = view.findViewById(R.id.ignore)
        deny = view.findViewById(R.id.deny)
        close = view.findViewById(R.id.close)
        apply = view.findViewById(R.id.apply)
        stateGroup = view.findViewById(R.id.state_group)
        scopeGroup = view.findViewById(R.id.scope_group)

        appOp = BundleCompat.getParcelable(requireArguments(), BundleConstants.appOp, AppOp::class.java) ?: AppOp()
        appOp?.copyTo(updatedAppOp)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allow.setButtonCheckedColor(allowColor)
        deny.setButtonCheckedColor(denyColor)

        name.text = appOp?.permission ?: getString(R.string.unknown)
        description.text = requireContext().getPermissionDescription(appOp?.id ?: "")

        when (appOp?.mode) {
            AppOpMode.ALLOW -> {
                allow.isChecked = true
            }
            AppOpMode.IGNORE -> {
                ignore.isChecked = true
            }
            AppOpMode.DENY -> {
                deny.isChecked = true
            }
            else -> {
                // Do nothing
            }
        }

        when (appOp?.scope) {
            AppOpScope.UID -> {
                scopeGroup.check(R.id.uid)
            }
            AppOpScope.PACKAGE -> {
                scopeGroup.check(R.id.application)
            }
            else -> {
                // Do nothing
            }
        }

        stateGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.allow -> {
                        updatedAppOp.mode = AppOpMode.ALLOW
                    }
                    R.id.ignore -> {
                        updatedAppOp.mode = AppOpMode.IGNORE
                    }
                    R.id.deny -> {
                        updatedAppOp.mode = AppOpMode.DENY
                    }
                    else -> {
                        updatedAppOp.mode = AppOpMode.DEFAULT
                    }
                }
            }
        }

        scopeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.uid -> {
                        updatedAppOp.scope = AppOpScope.UID
                    }
                    R.id.application -> {
                        updatedAppOp.scope = AppOpScope.PACKAGE
                    }
                }
            }
        }

        apply.setOnClickListener {
            appOpStateCallbacks?.onApplyAppOpState(updatedAppOp)
            dismiss()
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    fun setAppOpStateCallbacks(callbacks: AppOpStateCallbacks) {
        this.appOpStateCallbacks = callbacks
    }

    companion object {
        const val TAG = "AppOpState"

        fun newInstance(appOp: AppOp, packageInfo: PackageInfo): AppOpState {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putParcelable(BundleConstants.appOp, appOp)
            val fragment = AppOpState()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAppOpStateDialog(appOp: AppOp, packageInfo: PackageInfo): AppOpState {
            val dialog = newInstance(appOp, packageInfo)
            dialog.show(this, TAG)
            return dialog
        }

        private val allowColor = "#1F7A55".toColorInt()
        private val denyColor = "#C11007".toColorInt()

        interface AppOpStateCallbacks {
            fun onApplyAppOpState(appOp: AppOp)
        }
    }
}