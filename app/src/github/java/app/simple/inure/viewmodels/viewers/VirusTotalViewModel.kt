package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.virustotal.VirusTotalClient
import app.simple.inure.virustotal.VirusTotalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VirusTotalViewModel(application: Application, private val packageInfo: PackageInfo) : WrappedViewModel(application) {

    init {
        loadVirusTotalData()
    }

    private val response: MutableLiveData<VirusTotalResult.Success> by lazy {
        MutableLiveData<VirusTotalResult.Success>()
    }

    private val progress: MutableLiveData<VirusTotalResult.Progress> by lazy {
        MutableLiveData<VirusTotalResult.Progress>()
    }

    // Error message
    private val failed: MutableLiveData<VirusTotalResult.Error> by lazy {
        MutableLiveData<VirusTotalResult.Error>()
    }

    fun getResponse(): LiveData<VirusTotalResult.Success> {
        return response
    }

    fun getProgress(): LiveData<VirusTotalResult.Progress> {
        return progress
    }

    fun getFailed(): LiveData<VirusTotalResult.Error> {
        return failed
    }

    private fun loadVirusTotalData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                VirusTotalClient.getInstance().scanFile(packageInfo.applicationInfo?.sourceDir!!).collect { result ->
                    when (result) {
                        is VirusTotalResult.Error -> {
                            failed.postValue(result)
                        }
                        is VirusTotalResult.Progress -> {
                            progress.postValue(result)
                        }
                        is VirusTotalResult.Success -> {
                            response.postValue(result)
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                postWarning(e.message)
            }
        }
    }
}
