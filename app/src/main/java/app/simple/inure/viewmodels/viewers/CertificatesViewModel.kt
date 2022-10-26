package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.text.Spannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.APKCertificateUtils
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.security.cert.X509Certificate

class CertificatesViewModel(application: Application, val packageInfo: PackageInfo? = null, private val file: File? = null) : WrappedViewModel(application) {

    private val list = arrayListOf<Pair<Int, Spannable>>()

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
                val certificates: Array<X509Certificate> = if (packageInfo.isNotNull()) {
                    APKCertificateUtils(file, packageInfo!!.packageName, applicationContext()).x509Certificates
                } else {
                    ApkFile(file).use {
                        APKCertificateUtils(file, it.apkMeta.packageName, applicationContext()).x509Certificates
                    }
                }

                for (cert in certificates) {
                    if (list.isNotEmpty()) {
                        list.add(Pair(0, "".applySecondaryTextColor())) // Divider, trust me
                    }

                    addCertificates(cert)
                }

                this@CertificatesViewModel.certificate.postValue(list)
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun addCertificates(cert: X509Certificate) {
        list.add(Pair(R.string.sign_algorithm, cert.sigAlgName.applySecondaryTextColor()))
        list.add(Pair(R.string.sign_algorithm_oid, cert.sigAlgOID.applySecondaryTextColor()))
        list.add(Pair(R.string.serial_number, cert.serialNumber.toString(16).applySecondaryTextColor()))
        list.add(Pair(R.string.certificate_sha1, APKCertificateUtils.getCertificateFingerprint(cert, "SHA1").lowercase().applySecondaryTextColor()))
        list.add(Pair(R.string.certificate_sha256, APKCertificateUtils.getCertificateFingerprint(cert, "SHA-256").lowercase().applySecondaryTextColor()))
        list.add(Pair(R.string.certificate_md5, APKCertificateUtils.getCertificateFingerprint(cert, "MD5").applySecondaryTextColor()))
        list.add(Pair(R.string.public_key, cert.publicKey.toString().split("=").toTypedArray()[1].split(",").toTypedArray()[0].applySecondaryTextColor()))
        list.add(Pair(R.string.valid_from, cert.notBefore.toString().applyAccentColor()))
        list.add(Pair(R.string.valid_to, cert.notAfter.toString().applyAccentColor()))
        list.add(Pair(R.string.issuer, cert.issuerX500Principal?.name!!.applyAccentColor()))
        list.add(Pair(R.string.subject, cert.subjectDN.name.applyAccentColor()))
        list.add(Pair(R.string.x_509, cert.toString().applySecondaryTextColor()))
    }
}