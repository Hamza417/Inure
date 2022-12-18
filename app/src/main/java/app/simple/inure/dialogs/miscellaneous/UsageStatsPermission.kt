package app.simple.inure.dialogs.miscellaneous

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class UsageStatsPermission : ScopedBottomSheetFragment() {

    private lateinit var container: DynamicRippleLinearLayoutWithFactor
    private lateinit var grant: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var state: TypeFaceTextView

    private var usageStatsPermissionCallbacks: UsageStatsPermissionCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_permission_usage_stats, container, false)

        this.container = view.findViewById(R.id.grant_usage_access)
        grant = view.findViewById(R.id.grant)
        close = view.findViewById(R.id.close)
        state = view.findViewById(R.id.status_usage_access)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            kotlin.runCatching {
                startActivity(intent)
            }.onFailure {
                intent.data = null
                startActivity(intent)
            }
        }

        grant.setOnClickListener {
            container.callOnClick()
        }

        close.setOnClickListener {
            if (requireContext().checkForUsageAccessPermission()) {
                usageStatsPermissionCallbacks?.onClosedAfterGrant()
                dismiss()
            } else {
                dismiss()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setState()
    }

    private fun setState() {
        if (requireContext().checkForUsageAccessPermission()) {
            state.text = getString(R.string.granted)
            container.isClickable = false
            grant.gone()
        } else {
            container.isClickable = true
            grant.visible(true)
            state.text = getString(R.string.not_granted)
        }
    }

    fun setOnUsageStatsPermissionCallbackListener(usageStatsPermissionCallbacks: UsageStatsPermissionCallbacks) {
        this.usageStatsPermissionCallbacks = usageStatsPermissionCallbacks
    }

    companion object {
        fun newInstance(): UsageStatsPermission {
            val args = Bundle()
            val fragment = UsageStatsPermission()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showUsageStatsPermissionDialog(): UsageStatsPermission {
            val dialog = newInstance()
            dialog.show(this, "usage_stats_permission")
            return dialog
        }

        interface UsageStatsPermissionCallbacks {
            fun onClosedAfterGrant()
        }
    }
}