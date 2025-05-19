package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.utils.JsonParserUtil
import app.simple.inure.virustotal.VirusTotalClient
import app.simple.inure.virustotal.VirusTotalResponse
import app.simple.inure.virustotal.VirusTotalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import org.json.JSONObject

class VirusTotalViewModel(application: Application, private val packageInfo: PackageInfo) : WrappedViewModel(application) {

    init {
        loadVirusTotalData()
    }

    private val response: MutableLiveData<VirusTotalResponse> by lazy {
        MutableLiveData<VirusTotalResponse>()
    }

    private val progress: MutableLiveData<VirusTotalResult.Progress> by lazy {
        MutableLiveData<VirusTotalResult.Progress>()
    }

    // Error message
    private val failed: MutableLiveData<VirusTotalResult.Error> by lazy {
        MutableLiveData<VirusTotalResult.Error>()
    }

    fun getResponse(): LiveData<VirusTotalResponse> {
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
                VirusTotalClient.getInstance().scanFile(packageInfo.applicationInfo?.sourceDir!!).collect { response ->
                    ensureActive() // Check if UI is still active before posting any updates

                    when (response) {
                        is VirusTotalResult.Error -> {
                            failed.postValue(response)
                        }
                        is VirusTotalResult.Progress -> {
                            progress.postValue(response)
                        }
                        is VirusTotalResult.Success -> {
                            this@VirusTotalViewModel.response.postValue(handleResponse(response.result))
                        }
                        is VirusTotalResult.Uploaded -> {
                            // We don't need to show analysis ID in the UI
                            Log.i(TAG, "Uploaded: ${response.result}")
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                postWarning(e.message)
            }
        }
    }

    fun handleResponse(jsonObject: JSONObject?): VirusTotalResponse? {
        return JsonParserUtil.parseSingleAttributes(jsonObject, VirusTotalResponse::class.java)
    }

    companion object {
        private const val TAG = "VirusTotalViewModel"
    }
}
