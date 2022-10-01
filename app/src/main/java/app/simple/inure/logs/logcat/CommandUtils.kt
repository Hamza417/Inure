package app.simple.inure.logs.logcat

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

object CommandUtils {
    fun runCmd(cmd: List<String>, stdoutList: MutableList<String>? = null, stderrList: MutableList<String>? = null, redirectStderr: Boolean = false): Int {
        val processBuilder = ProcessBuilder(cmd).redirectErrorStream(redirectStderr)
        var process: Process? = null
        try {
            process = processBuilder.start()
            val stderrStream = process.errorStream
            val stdoutStream = process.inputStream

            val stdoutThread = thread {
                try {
                    BufferedReader(InputStreamReader(stdoutStream)).use {
                        while (true) {
                            val line = it.readLine() ?: break
                            stdoutList?.add(line)
                        }
                    }
                } catch (_: Exception) {
                }
            }
            val stderrThread = thread {
                try {
                    BufferedReader(InputStreamReader(stderrStream)).use {
                        while (true) {
                            val line = it.readLine() ?: break
                            stderrList?.add(line)
                        }
                    }
                } catch (_: Exception) {
                }
            }

            val exitCode = process.waitFor()

            try {
                stderrThread.join(1000)
            } catch (_: InterruptedException) {
            }

            try {
                stdoutThread.join(1000)
            } catch (_: InterruptedException) {
            }

            return exitCode
        } catch (e: Exception) {
            return -1
        } finally {
            process?.destroy()
        }
    }
}