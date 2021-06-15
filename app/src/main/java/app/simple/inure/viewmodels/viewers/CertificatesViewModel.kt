package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.parsers.APKParser.getCertificates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CertificatesViewModel(application: Application, val applicationInfo: ApplicationInfo) : AndroidViewModel(application) {

    private val certificate: MutableLiveData<ArrayList<Pair<String, String>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, String>>>().also {
            loadCertificatesData()
        }
    }

    fun getCertificateData() : LiveData<ArrayList<Pair<String, String>>> {
        return certificate
    }

    private fun loadCertificatesData() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val cert = applicationInfo.getCertificates()

            val algorithm = cert.signAlgorithm
            val oid = cert.signAlgorithmOID
            val base64md5 = cert.certBase64Md5
            val md5 = cert.certMd5
            val startDate = cert.startDate.toString()
            val endDate = cert.endDate.toString()

            val arrayList = arrayListOf(
                Pair(context.getString(R.string.sign_algorithm), algorithm),
                Pair(context.getString(R.string.sign_algorithm_oid), oid),
                Pair(context.getString(R.string.certificate_base64_md5), base64md5),
                Pair(context.getString(R.string.certificate_md5), md5),
                Pair(context.getString(R.string.valid_from), startDate),
                Pair(context.getString(R.string.valid_to), endDate)
            )

            certificate.postValue(arrayList)
        }
    }
}