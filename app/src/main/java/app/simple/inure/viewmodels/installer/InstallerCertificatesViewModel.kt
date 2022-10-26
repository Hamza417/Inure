package app.simple.inure.viewmodels.installer

import android.app.Application
import android.text.Spannable
import androidx.core.text.toSpannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.SignatureUtils.convertToHex
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkV2Signer
import net.dongliu.apk.parser.bean.CertificateMeta
import java.io.File
import java.security.MessageDigest

class InstallerCertificatesViewModel(application: Application, val file: File) : WrappedViewModel(application) {

    val list = arrayListOf<Pair<Int, Spannable>>()

    private val certificate: MutableLiveData<ArrayList<Pair<Int, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Spannable>>>().also {
            loadCertificatesData()
        }
    }

    fun getCertificateData(): LiveData<ArrayList<Pair<Int, Spannable>>> {
        return certificate
    }

    private fun loadCertificatesData() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val apkFile = ApkFile(file)

                println("Here")

                val v1 = apkFile.apkSingers
                val v2 = kotlin.runCatching {
                    apkFile.apkV2Singers
                }.getOrElse {
                    arrayListOf<ApkV2Signer>()
                }

                apkFile.close()

                for (certs in v1) {
                    for (cert in certs.certificateMetas) {
                        addCertificate(cert, "v1")
                    }
                }

                for (certs in v2) {
                    for (cert in certs.certificateMetas) {
                        addCertificate(cert, "v2")
                    }
                }

                this@InstallerCertificatesViewModel.certificate.postValue(list)
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun addCertificate(cert: CertificateMeta, type: String) {
        list.add(Pair(R.string.label_type, type.applyAccentColor()))
        list.add(Pair(R.string.sign_algorithm, cert.signAlgorithm.applySecondaryTextColor()))
        list.add(Pair(R.string.sign_algorithm_oid, cert.signAlgorithmOID.applySecondaryTextColor()))
        list.add(Pair(R.string.certificate_md5, convertToHex(MessageDigest.getInstance("md5").digest(cert.certMd5.toByteArray())).applySecondaryTextColor()))
        // list.add(Pair(R.string.certificate_sha1, convertToHex(MessageDigest.getInstance("base64md5").digest(cert.certBase64Md5.toByteArray())).applySecondaryTextColor()))
        list.add(Pair(R.string.valid_from, cert.startDate.toString().applyAccentColor()))
        list.add(Pair(R.string.valid_to, cert.endDate.toString().applyAccentColor()))
        list.add(Pair(0, "".toSpannable())) // Divider
    }
}