package app.simple.inure.viewmodels.deviceinfo

import android.app.Application
import android.os.Build
import android.text.Spannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.util.ScreenMetrics
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceInfoViewModel(application: Application) : WrappedViewModel(application) {

    private val basics: MutableLiveData<ArrayList<Pair<String, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, Spannable>>>().also {
            loadBasics()
        }
    }

    private val display: MutableLiveData<ArrayList<Pair<String, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, Spannable>>>().also {
            loadDisplay()
        }
    }

    fun getBasics(): LiveData<ArrayList<Pair<String, Spannable>>> {
        return basics
    }

    fun getDisplay(): LiveData<ArrayList<Pair<String, Spannable>>> {
        return display
    }

    private fun loadBasics() {
        viewModelScope.launch(Dispatchers.Default) {
            basics.postValue(arrayListOf(
                    getModel(),
                    getManufacturer()
            ))
        }
    }

    private fun loadDisplay() {
        viewModelScope.launch(Dispatchers.Default) {
            display.postValue(arrayListOf(
                    getScreenForm(),
                    getResolution(),
                    getDensity(),
                    getRefreshRate(),
                    getOrientation()
            ))
        }
    }

    private fun getModel(): Pair<String, Spannable> {
        return Pair(getString(R.string.model),
                    Build.MODEL.applySecondaryTextColor())
    }

    private fun getManufacturer(): Pair<String, Spannable> {
        return Pair(getString(R.string.manufacturer),
                    Build.MANUFACTURER.applySecondaryTextColor())
    }

    private fun getScreenForm(): Pair<String, Spannable> {
        return Pair(getString(R.string.form_factor),
                    ScreenMetrics.getScreenClass(context).applySecondaryTextColor())
    }

    private fun getDensity(): Pair<String, Spannable> {
        return Pair(getString(R.string.density),
                    ScreenMetrics.getScreenDensity(context).applySecondaryTextColor())
    }

    private fun getResolution(): Pair<String, Spannable> {
        val res = ScreenMetrics.getScreenSize(context)
        return Pair(getString(R.string.resolution),
                    ("${res.height}x${res.width}").applySecondaryTextColor())
    }

    private fun getRefreshRate(): Pair<String, Spannable> {
        return Pair(getString(R.string.refresh_rate),
                    ScreenMetrics.getRefreshRate(context).toString().applySecondaryTextColor())
    }

    private fun getOrientation(): Pair<String, Spannable> {
        return Pair(getString(R.string.orientation),
                    ScreenMetrics.getOrientation(context).applySecondaryTextColor())
    }
}