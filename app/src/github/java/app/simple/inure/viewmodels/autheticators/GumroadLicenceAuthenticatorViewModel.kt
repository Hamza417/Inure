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
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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
                val httpClient = OkHttpClient().newBuilder()
                    .connectionPool(ConnectionPool(0, 5, TimeUnit.SECONDS))
                    .build()

                val request = okhttp3.Request.Builder()
                    .url("https://api.gumroad.com/v2/licenses/verify")
                    .post(okhttp3.FormBody.Builder()
                              .add("product_id", "nlf3AEUrATXrE9iBrZ2Mbw==")
                              .add("license_key", licence)
                              .build())
                    .build()

                /**
                 * {
                 *     "success": true,
                 *     "uses": 1,
                 *     "purchase": {
                 *         "seller_id": "O3SuCGgxAbgVoWuZyfGJWg==",
                 *         "product_id": "nlf3AEUrATXrE9iBrZ2Mbw==",
                 *         "product_name": "Inure Full Version Unlocker",
                 *         "permalink": "inure_unlocker",
                 *         "product_permalink": "https://hamza417.gumroad.com/l/inure_unlocker",
                 *         "short_product_id": "rwkyxh",
                 *         "email": "hamzarizwan243@gmail.com",
                 *         "price": 799,
                 *         "gumroad_fee": 80,
                 *         "currency": "usd",
                 *         "quantity": 1,
                 *         "discover_fee_charged": false,
                 *         "can_contact": true,
                 *         "referrer": "https://github.com/",
                 *         "card": {
                 *             "visual": null,
                 *             "type": null,
                 *             "bin": null,
                 *             "expiry_month": null,
                 *             "expiry_year": null
                 *         },
                 *         "order_number": 73577183,
                 *         "sale_id": "3pCnEidfGt923m6Oz41oUQ==",
                 *         "sale_timestamp": "2024-02-08T05:06:34Z",
                 *         "purchaser_id": "9140266370222",
                 *         "variants": "(Full Version Unlocker)",
                 *         "test": true,
                 *         "license_key": "00000000-00000000-00000000-00000000"
                 *         "ip_country": "Country",
                 *         "is_gift_receiver_purchase": false,
                 *         "refunded": false,
                 *         "disputed": false,
                 *         "dispute_won": false,
                 *         "id": "3pCnEidfGt923m6Oz41oUQ==",
                 *         "created_at": "2024-02-08T05:06:34Z",
                 *         "custom_fields": [],
                 *         "chargebacked": false
                 *     }
                 * }
                 */
                val response = httpClient.newCall(request).execute()
                val responseBodyCopy = response.peekBody(Long.MAX_VALUE)
                val responseBody = responseBodyCopy.string()

                if (response.isSuccessful) {
                    // Check if the response body contains the key "success" and the value is true
                    // and refunded value is false

                    Log.d("GumroadLicenceAuthenticatorViewModel", responseBody)

                    val jsonObject = JSONObject(responseBody)
                    val success = jsonObject.getBoolean("success")
                    val refunded = jsonObject.getJSONObject("purchase").getBoolean("refunded")

                    if (success && !refunded) {
                        // Licence is valid
                        if (TrialPreferences.setUnlockerVerificationRequired(false)) {
                            if (TrialPreferences.setFullVersion(true)) {
                                TrialPreferences.setHasLicenceKey(true)
                                licenseStatus.postValue(true)
                            }
                        }
                    } else {
                        // Licence is invalid
                        licenseStatus.postValue(false)
                        TrialPreferences.setFullVersion(false)
                        TrialPreferences.setHasLicenceKey(false)
                        TrialPreferences.setUnlockerVerificationRequired(true)
                    }
                } else {
                    // Licence is invalid
                    Log.e("GumroadLicenceAuthenticatorViewModel", responseBody)
                    licenseStatus.postValue(false)
                    TrialPreferences.setFullVersion(false)
                    TrialPreferences.setHasLicenceKey(false)
                    TrialPreferences.setUnlockerVerificationRequired(true)

                    val jsonObject = JSONObject(responseBody)
                    message.postValue(jsonObject.getString("message"))
                }

                Log.d("GumroadLicenceAuthenticatorViewModel", TrafficStats.getThreadStatsTag().toString())
                TrafficStats.clearThreadStatsTag()
                response.close()
                httpClient.dispatcher().executorService().shutdown()
            }.getOrElse {
                postWarning(it.message.toString())
                it.printStackTrace()
            }
        }
    }
}
