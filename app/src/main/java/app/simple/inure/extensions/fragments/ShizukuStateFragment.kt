package app.simple.inure.extensions.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.ui.preferences.mainscreens.ConfigurationScreen
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.StringUtils.appendFlag
import app.simple.inure.util.ViewUtils.gone
import rikka.shizuku.Shizuku

abstract class ShizukuStateFragment : ScopedFragment() {
    private lateinit var shizukuSwitchView: Switch
    private lateinit var shizukuPermissionState: TypeFaceTextView

    private val requestCode = 100
    private var isBinderReceived = false

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        onRequestPermissionsResult(requestCode, grantResult)
    }

    private val onBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(ConfigurationScreen.TAG, "Shizuku binder received")
        isBinderReceived = true
        setShizukuPermissionState()
    }

    private val onBinderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(ConfigurationScreen.TAG, "Shizuku binder dead")
        isBinderReceived = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener)
            Shizuku.addBinderDeadListener(onBinderDeadListener)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shizukuSwitchView = view.findViewById(R.id.shizuku_switch_view)
        shizukuPermissionState = view.findViewById(R.id.shizuku_permission_state)

        shizukuSwitchView.isChecked = ConfigurationPreferences.isUsingShizuku()

        shizukuSwitchView.setOnSwitchCheckedChangeListener {
            if (it) {
                if (isBinderReceived) {
                    if (checkPermission()) {
                        ConfigurationPreferences.setUsingShizuku(true)
                    }

                    setShizukuPermissionState()
                } else {
                    shizukuSwitchView.uncheck(true)
                    showWarning(Warnings.SHIZUKU_BINDER_NOT_READY, false)
                    shizukuSwitchView.blinkThumbTwoTimes()
                }
            } else {
                ConfigurationPreferences.setUsingShizuku(false)
                setShizukuPermissionState()
                if (isBinderReceived.invert()) {
                    shizukuSwitchView.blinkThumbTwoTimes()
                }
            }
        }
    }

    private fun isShizukuPermissionGranted(): Boolean {
        return if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            false
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkPermission(): Boolean {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            shizukuPermissionState.text = "Pre-v11 is unsupported"
            return false
        }

        return when {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                // Granted
                shizukuSwitchView.check(true)
                Log.d(ConfigurationScreen.TAG, "checkPermission: granted")
                true
            }
            Shizuku.shouldShowRequestPermissionRationale() -> {
                // Users choose "Deny and don't ask again"
                shizukuSwitchView.uncheck(false)
                shizukuSwitchView.gone()
                Log.d(ConfigurationScreen.TAG, "checkPermission: shouldShowRequestPermissionRationale")
                shizukuPermissionState.text = Warnings.SHIZUKU_PERMISSION_DENIED
                false
            }
            else -> {
                // Request the permission
                Shizuku.requestPermission(requestCode)
                Log.d("ConfigurationScreen", "checkPermission: requestPermission")
                false
            }
        }
    }

    private fun setShizukuPermissionState() {
        handler.post {
            try {
                shizukuPermissionState.text = buildString {
                    if (Shizuku.isPreV11().invert()) {
                        if (Shizuku.shouldShowRequestPermissionRationale()) {
                            appendFlag(Warnings.SHIZUKU_PERMISSION_DENIED)
                            shizukuSwitchView.gone(animate = false)
                        } else {
                            if (isShizukuPermissionGranted()) {
                                appendFlag(getString(R.string.granted))
                            } else {
                                appendFlag(getString(R.string.rejected))
                            }

                            if (ConfigurationPreferences.isUsingShizuku()) {
                                appendFlag(getString(R.string.enabled))
                            } else {
                                appendFlag(getString(R.string.disabled))
                            }
                        }
                    } else {
                        appendFlag("Pre-v11 is unsupported")
                    }
                }
            } catch (e: IllegalStateException) {
                // Since the fragment is destroyed, the view is not available
                // So, we catch the exception and ignore it
                Log.e(ConfigurationScreen.TAG, "setShizukuPermissionState: $e")
            } catch (e: UninitializedPropertyAccessException) {
                Log.e(ConfigurationScreen.TAG, "setShizukuPermissionState: $e")
            }
        }
    }

    private fun onRequestPermissionsResult(requestCode: Int, grantResult: Int) {
        val granted: Boolean = grantResult == PackageManager.PERMISSION_GRANTED
        Log.d(ConfigurationScreen.TAG, "onRequestPermissionsResult: $granted with requestCode: $requestCode")
        ConfigurationPreferences.setUsingShizuku(granted)
        setShizukuPermissionState()

        if (granted) {
            shizukuSwitchView.check(true)
        } else {
            shizukuSwitchView.uncheck(true)
            if (Shizuku.shouldShowRequestPermissionRationale()) {
                shizukuPermissionState.text = Warnings.SHIZUKU_PERMISSION_DENIED
                shizukuSwitchView.gone(animate = false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        }
    }
}
