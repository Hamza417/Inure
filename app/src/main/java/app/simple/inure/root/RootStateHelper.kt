package app.simple.inure.root

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.simple.inure.BuildConfig
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RootStateHelper {
    private const val TAG = "RootStateHelper"

    fun Switch.setRootState(viewLifecycleOwner: LifecycleOwner) {
        setOnSwitchClickListener {
            if (ConfigurationPreferences.isUsingRoot()) {
                setCheckedSafely(false)
            } else {
                var isGranted: Boolean

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "onSwitchChecked: Requesting root access")
                    kotlin.runCatching {
                        Shell.enableVerboseLogging = BuildConfig.DEBUG
                        Shell.setDefaultBuilder(
                                Shell.Builder
                                    .create()
                                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                                    .setTimeout(10))
                    }.getOrElse {
                        Log.e(TAG, "onSwitchChecked: $it")
                        it.printStackTrace()
                    }

                    Shell.getShell() // Request root access
                    isGranted = Shell.isAppGrantedRoot() == true
                    setCheckedSafely(isGranted)

                    if (isGranted.invert()) {
                        blinkThumbTwoTimes()
                    }
                }
            }
        }

        setOnSwitchCheckedChangeListener {
            Log.d(TAG, "onSwitchChecked: Root switch checked changed to $it")
            ConfigurationPreferences.setUsingRoot(it)
        }
    }
}
