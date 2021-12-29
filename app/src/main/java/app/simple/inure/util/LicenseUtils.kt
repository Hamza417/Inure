package app.simple.inure.util

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

object LicenseUtils {
    fun generateNonce(): Int? {
        try {
            // Create a secure random number generator
            val sr: SecureRandom = SecureRandom.getInstance("SHA1PRNG")

            // Get 1024 random bits
            val bytes = ByteArray(1024 / 8)
            sr.nextBytes(bytes)

            //return NONCE;
            return sr.nextInt(32)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return null
    }
}