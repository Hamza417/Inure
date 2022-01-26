package app.simple.inure.util

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object DeviceUtils {

    fun readKernelVersion(): String? {
        return try {
            val p = Runtime.getRuntime().exec("uname -a")
            val inputStream: InputStream? = if (p.waitFor() == 0) {
                p.inputStream
            } else {
                p.errorStream
            }
            val br = BufferedReader(InputStreamReader(inputStream),
                                    1000)
            val line: String = br.readLine()
            br.close()
            line
        } catch (ex: Exception) {
            "ERROR: " + ex.message
        }
    }
}