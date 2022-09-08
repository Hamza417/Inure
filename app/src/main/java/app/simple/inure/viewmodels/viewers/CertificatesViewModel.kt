package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.text.Spannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.SignatureUtils.convertToHex
import app.simple.inure.apk.utils.SignatureUtils.getApplicationSignature
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest

class CertificatesViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val certificate: MutableLiveData<ArrayList<Pair<Int, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Spannable>>>().also {
            loadCertificatesData()
        }
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getCertificateData(): LiveData<ArrayList<Pair<Int, Spannable>>> {
        return certificate
    }

    private fun loadCertificatesData() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val pair = packageInfo.getApplicationSignature(applicationContext())

                val arrayList = arrayListOf(
                        Pair(R.string.sign_algorithm, pair.first.sigAlgName.applySecondaryTextColor()),
                        Pair(R.string.sign_algorithm_oid, pair.first.sigAlgOID.applySecondaryTextColor()),
                        Pair(R.string.certificate_md5, convertToHex(MessageDigest.getInstance("md5").digest(pair.second.toByteArray())).applySecondaryTextColor()),
                        Pair(R.string.certificate_sha1, convertToHex(MessageDigest.getInstance("sha1").digest(pair.second.toByteArray())).applySecondaryTextColor()),
                        Pair(R.string.certificate_sha256, convertToHex(MessageDigest.getInstance("sha256").digest(pair.second.toByteArray())).applySecondaryTextColor()),
                        Pair(R.string.public_key, pair.first.publicKey.toString().applySecondaryTextColor()),
                        Pair(R.string.valid_from, pair.first.notBefore.toString().applyAccentColor()),
                        Pair(R.string.valid_to, pair.first.notAfter.toString().applyAccentColor()),
                        Pair(R.string.issuer, pair.first.issuerX500Principal?.name!!.applyAccentColor()),
                        Pair(R.string.x_509, pair.first.toString().applySecondaryTextColor())
                )

                this@CertificatesViewModel.certificate.postValue(arrayList)
            }.getOrElse {
                delay(500L)
                error.postValue(it.message)
            }
        }
    }
}