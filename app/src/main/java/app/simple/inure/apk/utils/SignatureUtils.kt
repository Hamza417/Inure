package app.simple.inure.apk.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.experimental.and

object SignatureUtils {

    fun PackageInfo.getApplicationSignature(context: Context): Pair<X509Certificate, Signature> {
        val arrayOfSignatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager
                    .getPackageInfo(packageName,
                                    PackageManager.PackageInfoFlags.of(
                                            PackageManager.GET_SIGNING_CERTIFICATES.toLong()))
                    .signingInfo?.apkContentsSigners!!
            } else {
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo?.apkContentsSigners!!
            }
        } else {
            @Suppress("deprecation", "PackageManagerGetSignatures")
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures!!
        }

        for (signature in arrayOfSignatures) {
            /**
             * Get the X.509 certificate.
             */
            val rawCert = signature.toByteArray()
            val certStream: InputStream = ByteArrayInputStream(rawCert)

            val certFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
            return Pair(certFactory.generateCertificate(certStream) as X509Certificate, signature)
        }

        throw CertificateException("Certificate not found.")
    }

    /**
     *  //https://stackoverflow.com/questions/5980658/how-to-sha1-hash-a-string-in-android
     */
    fun convertToHex(data: ByteArray): String {
        val buf = StringBuilder()
        for (b in data) {
            var halfByte: Int = (b.toInt() ushr 4) and 0x0F
            var twoHalves = 0

            do {
                buf.append(if (halfByte in 0..9) ('0'.code + halfByte).toChar() else ('a'.code + (halfByte - 10)).toChar())
                halfByte = (b and 0x0F).toInt()
            } while (twoHalves++ < 1)
        }
        return buf.toString()
    }
}
