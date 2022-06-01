package app.simple.inure.viewmodels.deviceinfo

import android.app.Application
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.text.Spannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.DeviceUtils
import app.simple.inure.util.NumberUtils
import app.simple.inure.util.SDKHelper
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SystemInfoViewModel(application: Application) : WrappedViewModel(application) {

    private val additionalInformation: MutableLiveData<ArrayList<Pair<String, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, Spannable>>>().also {
            loadAdditionalInformation()
        }
    }

    private val information: MutableLiveData<ArrayList<Pair<String, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, Spannable>>>().also {
            loadInformation()
        }
    }

    fun getInformation(): LiveData<ArrayList<Pair<String, Spannable>>> {
        return information
    }

    fun getAdditionalInformation(): LiveData<ArrayList<Pair<String, Spannable>>> {
        return additionalInformation
    }

    private fun loadInformation() {
        viewModelScope.launch(Dispatchers.Default) {
            information.postValue(arrayListOf(
                    getAndroidVersion(),
                    getHardwareName(),
                    getSecurityUpdate(),
                    getKernelVersion(),
                    getBasebandVersion(),
                    getUser(),
                    getBoard(),
                    getBootloader(),
                    getFingerprint(),
                    getUSBDebugState(),
                    getUpTime()
            ))
        }
    }

    private fun loadAdditionalInformation() {
        viewModelScope.launch(Dispatchers.Default) {
            additionalInformation.postValue(arrayListOf(
                    getRoot(),
                    getBusybox()
            ))
        }
    }

    private fun getAndroidVersion(): Pair<String, Spannable> {
        return Pair(getString(R.string.android_version),
                    SDKHelper.getSdkTitle(Build.VERSION.SDK_INT).applySecondaryTextColor())
    }

    private fun getHardwareName(): Pair<String, Spannable> {
        return Pair(getString(R.string.hardware),
                    Build.HARDWARE.applySecondaryTextColor())
    }

    private fun getSecurityUpdate(): Pair<String, Spannable> {
        return Pair(getString(R.string.security_update),
                    Build.VERSION.SECURITY_PATCH.applySecondaryTextColor())
    }

    private fun getKernelVersion(): Pair<String, Spannable> {
        return Pair(getString(R.string.kernel_version),
                    DeviceUtils.readKernelVersion()!!.applySecondaryTextColor())
    }

    private fun getBasebandVersion(): Pair<String, Spannable> {
        kotlin.runCatching {
            return Pair(getString(R.string.baseband_version),
                        Build.getRadioVersion().applySecondaryTextColor())
        }.getOrElse {
            return Pair(getString(R.string.baseband_version),
                        getString(R.string.not_available).applySecondaryTextColor())
        }
    }

    private fun getUser(): Pair<String, Spannable> {
        return Pair(getString(R.string.user),
                    Build.USER.applySecondaryTextColor())
    }

    private fun getBoard(): Pair<String, Spannable> {
        return Pair(getString(R.string.board),
                    Build.BOARD.applySecondaryTextColor())
    }

    private fun getBootloader(): Pair<String, Spannable> {
        return Pair("Bootloader",
                    Build.BOOTLOADER.applySecondaryTextColor())
    }

    private fun getFingerprint(): Pair<String, Spannable> {
        return Pair(getString(R.string.fingerprint),
                    Build.FINGERPRINT.applySecondaryTextColor())
    }

    private fun getUSBDebugState(): Pair<String, Spannable> {
        val s = if (Settings.Secure.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1) {
            getString(R.string.enabled)
        } else {
            getString(R.string.disabled)
        }

        return Pair("USB Debugging", s.applySecondaryTextColor())
    }

    private fun getUpTime(): Pair<String, Spannable> {
        return Pair(getString(R.string.up_time),
                    NumberUtils.getFormattedTime(SystemClock.elapsedRealtime()).applySecondaryTextColor())
    }

    private fun getRoot(): Pair<String, Spannable> {
        val s = with(RootBeer(context)) {
            if (isRooted) {
                getString(R.string.available)
            } else {
                getString(R.string.not_available)
            }
        }

        return Pair("Root", s.applySecondaryTextColor())
    }

    private fun getBusybox(): Pair<String, Spannable> {
        val s = with(RootBeer(context)) {
            if (checkForBusyBoxBinary()) {
                getString(R.string.available)
            } else {
                getString(R.string.not_available)
            }
        }

        return Pair("Busybox", s.applySecondaryTextColor())
    }
}