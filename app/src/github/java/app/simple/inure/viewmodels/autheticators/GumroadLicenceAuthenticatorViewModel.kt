package app.simple.inure.viewmodels.autheticators

import android.app.Application
import android.net.TrafficStats
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
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
            TrafficStats.setThreadStatsTag(0xF00D)
            val httpClient = OkHttpClient()

            val request = okhttp3.Request.Builder()
                .url("https://api.gumroad.com/v2/licenses/verify")
                .post(okhttp3.FormBody.Builder()
                          .add("product_permalink", "nlf3AEUrATXrE9iBrZ2Mbw==")
                          .add("license_key", licence)
                          .build())
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBodyCopy = response.peekBody(Long.MAX_VALUE)
                val responseBody = responseBodyCopy.string()

                if (response.isSuccessful) {
                    // Check if the response body contains the key "success" and the value is true
                    // and refunded value is false

                    Log.d("GumroadLicenceAuthenticatorViewModel", responseBody)

                    val jsonObject = JSONObject(responseBody)
                    val success = jsonObject.getBoolean("success")
                    val refunded = jsonObject.getBoolean("refunded")

                    if (success && !refunded) {
                        // Licence is valid
                        licenseStatus.postValue(true)
                    } else {
                        // Licence is invalid
                        licenseStatus.postValue(false)
                    }
                } else {
                    // Licence is invalid
                    Log.e("GumroadLicenceAuthenticatorViewModel", responseBody)
                    licenseStatus.postValue(false)

                    val jsonObject = JSONObject(responseBody)
                    message.postValue(jsonObject.getString("message"))
                }
            }

            Log.d("GumroadLicenceAuthenticatorViewModel", TrafficStats.getThreadStatsTag().toString())
            TrafficStats.clearThreadStatsTag()
            httpClient.connectionPool().evictAll()
        }
    }
}