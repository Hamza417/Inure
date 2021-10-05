package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.SignatureUtils
import app.simple.inure.apk.utils.SignatureUtils.getApplicationSignature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest

class CertificatesViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

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

                val pair = packageInfo.getApplicationSignature(context)!!

                val arrayList = arrayListOf(
                    Pair(context.getString(R.string.sign_algorithm), pair.first.sigAlgName),
                    Pair(context.getString(R.string.sign_algorithm_oid), pair.first.sigAlgOID),
                    Pair(context.getString(R.string.certificate_md5), SignatureUtils.convertToHex(MessageDigest.getInstance("md5").digest(pair.second.toByteArray()))),
                    Pair(context.getString(R.string.certificate_sha1), SignatureUtils.convertToHex(MessageDigest.getInstance("sha1").digest(pair.second.toByteArray()))),
                    Pair(context.getString(R.string.certificate_sha256), SignatureUtils.convertToHex(MessageDigest.getInstance("sha256").digest(pair.second.toByteArray()))),
                    Pair(context.getString(R.string.public_key), pair.first.publicKey.toString()),
                    Pair(context.getString(R.string.valid_from), pair.first.notBefore.toString()),
                    Pair(context.getString(R.string.valid_to), pair.first.notAfter.toString()),
                    Pair(context.getString(R.string.issuer), pair.first.issuerX500Principal?.name),
                    Pair("X.509", pair.first.toString())
                )

                this@CertificatesViewModel.certificate.postValue(arrayList)
            }.getOrElse {
                delay(1000L)
                error.postValue(it.message)
            }
        }
    }
}