package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.SignatureUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class CertificatesViewModel(application: Application, val applicationInfo: ApplicationInfo) : AndroidViewModel(application) {

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val certificate: MutableLiveData<ArrayList<Pair<String, String>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, String>>>().also {
            loadCertificatesData()
        }
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getCertificateData(): LiveData<ArrayList<Pair<String, String>>> {
        return certificate
    }

    private fun loadCertificatesData() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val context = getApplication<Application>().applicationContext

                val pm = getApplication<Application>().packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_SIGNATURES)

                val sign = pm.signatures[0]

                val x509Certificate = CertificateFactory.getInstance("X.509")
                        .generateCertificate(ByteArrayInputStream(sign.toByteArray())) as X509Certificate

                val arrayList = arrayListOf(
                    Pair(context.getString(R.string.sign_algorithm), x509Certificate.sigAlgName),
                    Pair(context.getString(R.string.sign_algorithm_oid), x509Certificate.sigAlgOID),
                    Pair(context.getString(R.string.certificate_md5), SignatureUtils.convertToHex(MessageDigest.getInstance("md5").digest(sign.toByteArray()))),
                    Pair(context.getString(R.string.certificate_sha1), SignatureUtils.convertToHex(MessageDigest.getInstance("sha1").digest(sign.toByteArray()))),
                    Pair(context.getString(R.string.certificate_sha256), SignatureUtils.convertToHex(MessageDigest.getInstance("sha256").digest(sign.toByteArray()))),
                    Pair(context.getString(R.string.public_key), x509Certificate.publicKey.toString()),
                    Pair(context.getString(R.string.valid_from), x509Certificate.notBefore.toString()),
                    Pair(context.getString(R.string.valid_to), x509Certificate.notAfter.toString()),
                    Pair(context.getString(R.string.issuer), x509Certificate.issuerX500Principal.name),
                    Pair("X.509", x509Certificate.toString())
                )

                this@CertificatesViewModel.certificate.postValue(arrayList)
            }.getOrElse {
                delay(1000L)
                error.postValue(it.message)
            }
        }
    }
}