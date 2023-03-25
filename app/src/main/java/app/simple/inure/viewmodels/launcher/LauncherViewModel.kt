package app.simple.inure.viewmodels.launcher

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.APKCertificateUtils
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.AppUtils
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.NullSafety.isNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate

class LauncherViewModel(application: Application) : WrappedViewModel(application) {

    @Suppress("PrivatePropertyName")
    private val SHA1 = arrayListOf(
            "85D8B419713565CC7A4E14AAE269F2EC14F37013",
            "8592E01F4AC28B2CA1E423573773D71A1EC82849"
    )

    private val hasValidCertificate: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            if (packageManager.isPackageInstalled(AppUtils.unlockerPackageName)) {
                verifyCertificate()
            }
        }
    }

    fun getHasValidCertificate(): LiveData<Boolean> {
        return hasValidCertificate
    }

    private fun verifyCertificate() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val packageInfo = packageManager.getPackageInfo(AppUtils.unlockerPackageName)
                val file = packageInfo?.applicationInfo?.sourceDir?.toFile()

                val certificates: Array<X509Certificate> = if (packageInfo.isNotNull()) {
                    APKCertificateUtils(file, packageInfo!!.packageName, applicationContext()).x509Certificates
                } else {
                    ApkFile(file).use {
                        APKCertificateUtils(file, it.apkMeta.packageName, applicationContext()).x509Certificates
                    }
                }

                val fingerPrint = computeFingerPrint(certificates[0].encoded)

                Log.d("LauncherViewModel", "FingerPrint SHA1: $fingerPrint")

                /**
                 * This will verify the unlocker package integrity.
                 * If you're a developer and you're trying to
                 * debug the app, you can add your own SHA1
                 * fingerprint to the list above and it will
                 * work just fine.
                 *
                 * If you're someone trying to reverse engineer the app
                 * to get the full version without paying despite my thousands
                 * of hours of efforts, then you're a piece of shit and just
                 * set the parameter to true and app will work with any
                 * unlocker package.
                 */
                hasValidCertificate.postValue(SHA1.contains(fingerPrint))
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun computeFingerPrint(certRaw: ByteArray?): String {
        var strResult = ""
        val messageDigest: MessageDigest
        try {
            messageDigest = MessageDigest.getInstance("SHA1")
            certRaw?.let { messageDigest.update(it) }
            var strAppend: String

            for (b in messageDigest.digest()) {
                strAppend = (b.toInt() and 0xff).toString(16)
                if (strAppend.length == 1) strResult += "0"
                strResult += strAppend
            }

            strResult = strResult.uppercase()
        } catch (ex: NoSuchAlgorithmException) {
            ex.printStackTrace()
        }

        return strResult
    }
}