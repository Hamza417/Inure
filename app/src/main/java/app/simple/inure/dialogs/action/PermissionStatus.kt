package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils
import app.simple.inure.apk.utils.PermissionUtils.isException
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.theme.ThemeDivider
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.PermissionStatusFactory
import app.simple.inure.models.PermissionInfo
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.PermissionPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ColorUtils
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.StringUtils.optimizeToColoredString
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.dialogs.PermissionStatusViewModel

class PermissionStatus : ScopedBottomSheetFragment() {

    private lateinit var loader: CustomProgressBar
    private lateinit var name: TypeFaceTextView
    private lateinit var status: TypeFaceTextView
    private lateinit var description: TypeFaceTextView
    private lateinit var warning: TypeFaceTextView

    private lateinit var close: DynamicRippleTextView
    private lateinit var state: DynamicRippleTextView
    private lateinit var divider: ThemeDivider
    private lateinit var btnContainer: LinearLayout

    private lateinit var permissionInfo: PermissionInfo
    private lateinit var permissionStatusFactory: PermissionStatusFactory
    private lateinit var permissionStatusViewModel: PermissionStatusViewModel
    private var permissionStatusCallbacks: PermissionStatusCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_permission_status, container, false)

        loader = view.findViewById(R.id.loader)
        name = view.findViewById(R.id.permissions_name)
        status = view.findViewById(R.id.permissions_status)
        description = view.findViewById(R.id.permissions_desc)
        warning = view.findViewById(R.id.warning)
        close = view.findViewById(R.id.close)
        state = view.findViewById(R.id.permission_state)
        divider = view.findViewById(R.id.divider)
        btnContainer = view.findViewById(R.id.btn_container)

        name.setTextIsSelectable(true)
        description.setTextIsSelectable(true)
        status.setTextIsSelectable(true)

        with(ColorUtils.lightenColor(AppearancePreferences.getAccentColor(), 0.2F)) {
            name.highlightColor = this
            description.highlightColor = this
            status.highlightColor = this
        }

        permissionInfo = requireArguments().parcelable(BundleConstants.PERMISSION_INFO)!!
        packageInfo = requireArguments().parcelable(BundleConstants.PACKAGE_INFO)!!

        permissionStatusFactory = PermissionStatusFactory(packageInfo, permissionInfo)
        permissionStatusViewModel = ViewModelProvider(this, permissionStatusFactory)[PermissionStatusViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loader.invisible(animate = false)
        //        name.setPermissionName(permissionInfo)
        //        status.setStatusText(permissionInfo)
        //        description.setDescriptionText(permissionInfo)

        if (permissionInfo.permissionInfo.isNotNull()) {
            name.setPermissionName(permissionInfo)
            description.setDescriptionText(permissionInfo)
            status.setStatusText(permissionInfo)

            /* -------------------------------------------------------------------------------------------------------- */

            status.setTextColor(AppearancePreferences.getAccentColor())
            description.visible(false)
        } else {
            name.text = permissionInfo.name.optimizeToColoredString(".")
            status.text = getString(R.string.permission_info_not_available)
            status.setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
            description.gone()
        }

        setStateText(animate = false)

        state.setOnClickListener {
            loader.visible(animate = true)
            permissionStatusViewModel.setPermissionState(permissionInfo)
        }

        close.setOnClickListener {
            dismiss()
        }

        permissionStatusViewModel.getSuccessStatus().observe(viewLifecycleOwner) {
            when (it) {
                "Done" -> {
                    loader.invisible(animate = true)
                    with(permissionInfo.isGranted == 1) {
                        if (this) {
                            permissionInfo.isGranted = 0
                        } else {
                            permissionInfo.isGranted = 1
                        }

                        permissionStatusCallbacks?.onSuccess(!this)
                        status.setStatusText(permissionInfo)
                        setStateText()
                    }
                }
                "Failed" -> {
                    loader.invisible(animate = true)
                }
            }
        }

        permissionStatusViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }
    }

    private fun TypeFaceTextView.setStatusText(permissionInfo: PermissionInfo) {
        @Suppress("deprecation")
        var text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PermissionUtils.protectionToString(permissionInfo.permissionInfo!!.protection, permissionInfo.permissionInfo!!.protectionFlags, context)
        } else {
            PermissionUtils.protectionToString(permissionInfo.permissionInfo!!.protectionLevel, permissionInfo.permissionInfo!!.protectionLevel, context)
        }

        text = when (permissionInfo.isGranted) {
            0 -> {
                text + " | " + context.getString(R.string.rejected)
            }
            1 -> {
                text + " | " + context.getString(R.string.granted)
            }
            2 -> {
                text + " | " + context.getString(R.string.unknown)
            }
            else -> {
                text + " | " + context.getString(R.string.unknown)
            }
        }

        setTextWithAnimation(text)
    }

    private fun TypeFaceTextView.setDescriptionText(permissionInfo: PermissionInfo) {
        text = kotlin.runCatching {
            val string = permissionInfo.permissionInfo!!.loadDescription(requireContext().packageManager)

            if (string.isNullOrEmpty()) {
                throw NullPointerException("Description is either null or not available")
            } else {
                string
            }
        }.getOrElse {
            getString(R.string.desc_not_available)
        }
    }

    private fun TypeFaceTextView.setPermissionName(permissionInfo: PermissionInfo) {
        text = if (PermissionPreferences.getLabelType()) {
            permissionInfo.name
        } else {
            permissionInfo.label
        }.toString().optimizeToColoredString(".")
    }

    private fun setStateText(animate: Boolean = true) {
        try {
            if (PermissionUtils.isDangerous(permissionInfo.permissionInfo!!)
                    || permissionInfo.permissionInfo!!.isException()) {
                state.visible(animate = true)

                if (animate) {
                    state.setTextWithSlideAnimation(if (permissionInfo.isGranted == 1) {
                        getString(R.string.revoke)
                    } else {
                        getString(R.string.grant)
                    })
                } else {
                    state.text = if (permissionInfo.isGranted == 1) {
                        getString(R.string.revoke)
                    } else {
                        getString(R.string.grant)
                    }
                }

                warning.gone()
                divider.gone()
            } else {
                state.gone(animate = false)
                warning.visible(animate = false)
                divider.visible(animate = false)
            }
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
            state.gone(animate = false)
            warning.visible(animate = false)
            divider.visible(animate = false)
        }

        btnContainer.requestLayout()
    }

    fun setOnPermissionStatusCallbackListener(permissionStatusCallbacks: PermissionStatusCallbacks) {
        this.permissionStatusCallbacks = permissionStatusCallbacks
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, permissionInfo: PermissionInfo): PermissionStatus {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            args.putParcelable(BundleConstants.PERMISSION_INFO, permissionInfo)
            val fragment = PermissionStatus()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showPermissionStatus(packageInfo: PackageInfo, permissionInfo: PermissionInfo): PermissionStatus {
            val fragment = newInstance(packageInfo, permissionInfo)
            fragment.show(this, TAG)
            return fragment
        }

        interface PermissionStatusCallbacks {
            fun onSuccess(grantedStatus: Boolean)
        }

        const val TAG = "PermissionStatus"
    }
}
