package app.simple.inure.viewmodels.autheticators

import android.app.Application
import android.net.TrafficStats
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.TrialPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject

class GumroadLicenceAuthenticatorViewModel(application: Application) : WrappedViewModel(application) {

    private val licenseStatus: MutableLiveData<Boolean> = MutableLiveData()
    private val message: MutableLiveData<String> = MutableLiveData()

    fun getLicenseStatus(): LiveData<Boolean> {
        return licenseStatus
    }

    fun getMessage(): LiveData<String> {
        return message
    }

    /**
     * Authenticates the licence key using the Gumroad API
     * @param licence The licence key to be verified
     * @return True if the licence is valid, false otherwise
     *
     * @see <a href="https://gumroad.com/api#license-verification">Gumroad API Documentation</a>
     * This is the cURL
     * curl https://api.gumroad.com/v2/licenses/verify \
     * -d "product_permalink=your_product_permalink" \
     * -d "license_key=your_license_key" \
     * -u "your_api_token:"
     */
    fun verifyLicence(licence: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                TrafficStats.setThreadStatsTag(0xF00D)
                val httpClient = createHttpClient()
                val request = createRequest(licence)
                val response = executeRequest(httpClient, request)
                handleResponse(response)
                cleanupResources(httpClient, response)
            }.getOrElse {
                handleException(it)
            }
        }
    }

    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient()
    }

    private fun createRequest(licence: String): okhttp3.Request {
        return okhttp3.Request.Builder()
            .url("https://api.gumroad.com/v2/licenses/verify")
            .post(okhttp3.FormBody.Builder()
                      .add("product_id", "nlf3AEUrATXrE9iBrZ2Mbw==")
                      .add("license_key", licence)
                      .build())
            .build()
    }

    private fun executeRequest(httpClient: OkHttpClient, request: okhttp3.Request): okhttp3.Response {
        return httpClient.newCall(request).execute()
    }

    private fun handleResponse(response: okhttp3.Response) {
        val responseBodyCopy = response.peekBody(Long.MAX_VALUE)
        val responseBody = responseBodyCopy.string()

        if (response.isSuccessful) {
            processSuccessfulResponse(responseBody)
        } else {
            processErrorResponse(responseBody)
        }
    }

    private fun processSuccessfulResponse(responseBody: String) {
        Log.d("GumroadLicenceAuthenticatorViewModel", responseBody)
        val jsonObject = JSONObject(responseBody)
        val success = jsonObject.getBoolean("success")
        val refunded = jsonObject.getJSONObject("purchase").getBoolean("refunded")

        if (success && !refunded) {
            updateTrialPreferences(true)
            licenseStatus.postValue(true)
        } else {
            updateTrialPreferences(false)
            licenseStatus.postValue(false)
            postRefundMessage(refunded)
        }
    }

    private fun processErrorResponse(responseBody: String) {
        Log.e("GumroadLicenceAuthenticatorViewModel", responseBody)
        updateTrialPreferences(false)
        licenseStatus.postValue(false)
        val jsonObject = JSONObject(responseBody)
        message.postValue(jsonObject.getString("message"))
    }

    private fun updateTrialPreferences(isValid: Boolean) {
        if (isValid) {
            setPreferencesForValidLicense()
        } else {
            setPreferencesForInvalidLicense()
        }
    }

    private fun setPreferencesForValidLicense() {
        setUnlockerVerificationRequired(false)
        setFullVersion(true)
        setHasLicenceKey(true)
    }

    private fun setPreferencesForInvalidLicense() {
        setFullVersion(false)
        setHasLicenceKey(false)
        setUnlockerVerificationRequired(true)
    }

    private fun setUnlockerVerificationRequired(required: Boolean) {
        TrialPreferences.setUnlockerVerificationRequired(required)
    }

    private fun setFullVersion(fullVersion: Boolean) {
        TrialPreferences.setFullVersion(fullVersion)
    }

    private fun setHasLicenceKey(hasKey: Boolean) {
        TrialPreferences.setHasLicenceKey(hasKey)
    }

    private fun postRefundMessage(refunded: Boolean) {
        if (refunded) {
            message.postValue("Your purchase has been refunded and the licence key is no longer valid.")
        } else {
            message.postValue("Licence is not valid. Please check the licence key and try again.")
        }
    }

    private fun cleanupResources(httpClient: OkHttpClient, response: okhttp3.Response) {
        Log.d("GumroadLicenceAuthenticatorViewModel", TrafficStats.getThreadStatsTag().toString())
        TrafficStats.clearThreadStatsTag()
        response.close()
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }

    private fun handleException(exception: Throwable) {
        postWarning(exception.message.toString())
        exception.printStackTrace()
    }
}
