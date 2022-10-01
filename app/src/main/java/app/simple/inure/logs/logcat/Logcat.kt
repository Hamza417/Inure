package app.simple.inure.logs.logcat

import android.content.Context
import android.net.Uri
import android.os.ConditionVariable
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import app.simple.inure.logs.collections.FixedCircularArray
import app.simple.inure.logs.logger.Logger
import java.io.*
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class Logcat(initialCapacity: Int = INITIAL_LOG_CAPACITY) : Closeable {
    var logcatBuffers = DEFAULT_BUFFERS
    private val logcatCmd = arrayOf("logcat", "-v", "long")
    private var pollInterval: Long = 250L // in ms
    private var threadLogcat: Thread? = null
    private var logcatProcess: Process? = null
    private val handler: Handler = Handler(Looper.getMainLooper())

    private var recordStartIndex = -1

    private val listeners = Collections.newSetFromMap(WeakHashMap<LogsReceivedListener, Boolean>())

    private var pollCondition = ConditionVariable()

    private var exitCode: Int = -1

    private var _pausedLock = Any()
    private var paused: Boolean = false
        get() = synchronized(_pausedLock) {
            field
        }
        set(value) = synchronized(_pausedLock) {
            field = value
        }
    private val pausePostLogsCondition = ConditionVariable()

    // must be synchronized
    private val logsLock = ReentrantLock()
    private val pendingLogsFullCondition = logsLock.newCondition()
    private var logs = FixedCircularArray<Log>(initialCapacity, INITIAL_LOG_SIZE)
    private var pendingLogs = FixedCircularArray<Log>(initialCapacity, INITIAL_LOG_SIZE)
    private val filters = mutableMapOf<String, Filter>()
    private val exclusions = mutableMapOf<String, Filter>()

    private var _activityInBackgroundLock = Any()
    private var activityInBackground: Boolean = true
        get() = synchronized(_activityInBackgroundLock) {
            field
        }
        set(value) = synchronized(_activityInBackgroundLock) {
            field = value
        }

    private val activityInBackgroundCondition = ConditionVariable()

    private val lifeCycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            Logger.debug(Logcat::class, "onActivityInForeground")

            if (!paused) {
                Logger.debug(Logcat::class, "Posting pending logs")
                postPendingLogs()
            }

            logsLock.withLock {
                activityInBackground = false
            }
            activityInBackgroundCondition.open()
        }

        override fun onPause(owner: LifecycleOwner) {
            Logger.debug(Logcat::class, "onActivityInBackground")
            logsLock.withLock {
                activityInBackground = true
            }
        }
    }

    @Volatile
    private var isProcessAlive = false

    private fun postPendingLogs() {
        logsLock.withLock {
            if (pendingLogs.isNotEmpty()) {
                logs.add(pendingLogs)

                val filteredLogs = pendingLogs.filter { e ->
                    !exclusions.values.any { it.apply(e) }
                }.filter { e ->
                    filters.values.all { it.apply(e) }
                }

                if (filteredLogs.isNotEmpty()) {
                    handler.post { listeners.forEach { it.onReceivedLogs(filteredLogs) } }
                }

                pendingLogs.clear()
                pendingLogsFullCondition.signal()
            }
        }
    }

    fun start() {
        if (logcatProcess == null) {
            paused = false
            exitCode = -1
            threadLogcat = thread(block = { runLogcat() }, name = "logcat")
        } else {
            Logger.info(Logcat::class, "Logcat is already running!")
        }
    }

    fun stop() {
        logcatProcess?.destroy()

        try {
            threadLogcat?.join(5000)
        } catch (e: InterruptedException) {
        }

        threadLogcat = null
        logcatProcess = null

        logsLock.withLock {
            logs.clear()
            pendingLogs.clear()
        }
    }

    fun clearLogs(onClear: (() -> Unit)? = null) = withPaused {
        logsLock.withLock {
            logs.clear()
            pendingLogs.clear()
        }

        onClear?.invoke()
    }

    fun restart() {
        stop()
        if (!exitSuccess()) {
            Logger.error(Logcat::class, "Exit failure: $exitCode")
        }
        start()
    }

    fun exitSuccess() = exitCode == 0

    fun isRunning() = isProcessAlive

    private inline fun <T> withPaused(block: () -> T): T {
        val wasPaused = paused
        pause()
        val result = block()
        if (!wasPaused) {
            resume()
        }
        return result
    }

    fun addEventListener(listener: LogsReceivedListener) = withPaused {
        logsLock.withLock {
            listeners += listener
        }
    }

    fun removeEventListener(listener: LogsReceivedListener) = withPaused {
        logsLock.withLock {
            listeners -= listener
        }
    }

    fun clearEventListeners() = withPaused {
        logsLock.withLock {
            listeners.clear()
        }
    }

    fun getLogsFiltered(): List<Log> {
        logsLock.withLock {
            return if (exclusions.isEmpty() && filters.isEmpty()) {
                logs.toList()
            } else {
                logs.filter { log ->
                    !exclusions.values.any { it.apply(log) } && filters.values.all { it.apply(log) }
                }
            }
        }
    }

    fun addExclusion(
            name: String,
            filter: Filter
    ) {
        logsLock.withLock {
            exclusions[name] = filter
        }
    }

    fun removeExclusion(name: String) {
        logsLock.withLock {
            exclusions.remove(name)
        }
    }

    fun clearExclusions() {
        logsLock.withLock {
            exclusions.clear()
        }
    }

    fun addFilter(
            name: String,
            filter: Filter
    ) {
        logsLock.withLock {
            filters[name] = filter
        }
    }

    fun removeFilter(name: String) {
        logsLock.withLock {
            filters.remove(name)
        }
    }

    fun clearFilters(exclude: String? = null) {
        logsLock.withLock {
            val it = filters.iterator()
            while (it.hasNext()) {
                if (it.next().key != exclude) {
                    it.remove()
                }
            }
        }
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        if (paused) {
            paused = false
            pausePostLogsCondition.open()
            pollCondition.open()
        }
    }

    fun startRecording() {
        logsLock.withLock {
            recordStartIndex = logs.size - 1
        }
    }

    fun stopRecording(): List<Log> {
        logsLock.withLock {
            val result = mutableListOf<Log>()
            if (recordStartIndex >= 0) {
                for (i in recordStartIndex until logs.size) {
                    result += logs[i]
                }
            }
            recordStartIndex = -1
            return result.filter { log -> filters.values.all { it.apply(log) } }
        }
    }

    fun bind(activity: AppCompatActivity?) {
        activity?.lifecycle?.addObserver(lifeCycleObserver)
    }

    fun unbind(activity: AppCompatActivity?) {
        activity?.lifecycle?.removeObserver(lifeCycleObserver)
    }

    override fun close() {
        stop()
        logsLock.withLock {
            listeners.clear()
        }
    }

    fun setPollInterval(interval: Long) {
        this.pollInterval = interval
        pollCondition.open()
    }

    private fun runLogcat() {
        val buffers = mutableListOf<String>()
        if (logcatBuffers.isNotEmpty() && AVAILABLE_BUFFERS.isNotEmpty()) {
            for (buffer in logcatBuffers) {
                buffers += "-b"
                buffers += buffer
            }
        }
        val processBuilder = ProcessBuilder(*logcatCmd, *buffers.toTypedArray())

        try {
            logcatProcess = processBuilder.start()
            isProcessAlive = true
        } catch (e: IOException) {
            return
        }

        val errorStream = logcatProcess?.errorStream
        val inputStream = logcatProcess?.inputStream

        val postThread = thread(block = { postLogsPeriodically() }, name = "logcat-post")
        val stderrThread = thread(block = { processStderr(errorStream) }, name = "logcat-stderr")
        val stdoutThread = thread(block = { processStdout(inputStream) }, name = "logcat-stdout")

        exitCode = try {
            logcatProcess?.waitFor() ?: -1
        } catch (e: InterruptedException) {
            -1
        }

        isProcessAlive = false

        pollCondition.open()
        activityInBackgroundCondition.open()

        logcatProcess = null

        val waitTime = 1000L
        try {
            stderrThread.join(waitTime)
        } catch (e: InterruptedException) {
        }
        try {
            stdoutThread.join(waitTime)
        } catch (e: InterruptedException) {
        }
        try {
            postThread.join(waitTime)
        } catch (e: InterruptedException) {
        }
    }

    fun setMaxLogsCount(maxLogsCount: Int) {
        logsLock.withLock {
            logs = FixedCircularArray(maxLogsCount, INITIAL_LOG_SIZE)
            pendingLogs = FixedCircularArray(maxLogsCount, INITIAL_LOG_SIZE)
        }
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        logsLock.withLock {
            logs.forEach { log -> stringBuilder.append(log) }
        }
        return stringBuilder.toString()
    }

    private fun processStderr(errStream: InputStream?) {
        try {
            BufferedReader(InputStreamReader(errStream)).use {
                while (isProcessAlive) {
                    it.readLine() ?: break
                }
            }
        } catch (e: Exception) {
            // do nothing
        }
    }

    private fun postLogsPeriodically() {
        while (isProcessAlive) {
            if (paused) {
                pausePostLogsCondition.block()
                pausePostLogsCondition.close()
                if (!isProcessAlive) {
                    break
                }
            }

            if (activityInBackground) {
                activityInBackgroundCondition.block()
                activityInBackgroundCondition.close()
                if (!isProcessAlive) {
                    break
                }
            }

            val t0 = System.currentTimeMillis()

            postPendingLogs()

            val diff = System.currentTimeMillis() - t0
            val sleepTime = pollInterval - diff
            if (sleepTime > 0) {
                pollCondition.block(sleepTime)
                pollCondition.close()
            }
        }
    }

    private fun processStdout(inputStream: InputStream?) {
        try {
            LogcatStreamReader(inputStream!!).use {
                for (log in it) {
                    logsLock.withLock {
                        pendingLogs.add(log)

                        if (pendingLogs.isFull()) {
                            pendingLogsFullCondition.awaitUninterruptibly()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // do nothing
        }
    }

    companion object {
        val DEFAULT_BUFFERS: Set<String>
        val AVAILABLE_BUFFERS: Array<String>
        const val INITIAL_LOG_CAPACITY = 250_000
        const val INITIAL_LOG_SIZE = 1_000
        private const val LOG_FILE_HEADER_FMT = "<<< log_count = %d >>>"

        init {
            DEFAULT_BUFFERS = getDefaultBuffers()
            AVAILABLE_BUFFERS = getAvailableBuffers()

            Logger.debug(
                    Logcat::class, "Available buffers: " +
                    AVAILABLE_BUFFERS.contentToString()
            )
            Logger.debug(Logcat::class, "Default buffers: $DEFAULT_BUFFERS")
        }

        fun getLogCountFromHeader(file: File): Long {
            try {
                return getLogCountFromHeader(FileInputStream(file))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return -1L
        }

        fun getLogCountFromHeader(
                context: Context,
                file: DocumentFile
        ): Long {
            try {
                val fis = context.contentResolver.openInputStream(file.uri)
                return getLogCountFromHeader(fis!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return -1L
        }

        private fun getLogCountFromHeader(inputStream: InputStream): Long {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(inputStream))
                val header = reader.readLine()
                if (header.startsWith("<<<")) {
                    var startIndex = header.indexOf('=')
                    if (startIndex != -1) {
                        startIndex += 2
                        val endIndex = header.indexOf(' ', startIndex)
                        if (endIndex != -1) {
                            val value = header.substring(startIndex, endIndex)
                            return value.toLong()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                reader?.close()
            }
            return -1L
        }

        fun writeToFile(
                logs: List<Log>,
                file: File
        ): Boolean {
            var writer: BufferedWriter? = null
            return try {
                writer = BufferedWriter(FileWriter(file, false))
                writeToFileHelper(logs, writer)
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                writer?.close()
            }
        }

        fun writeToFile(
                context: Context,
                logs: List<Log>,
                uri: Uri
        ): Boolean {
            var writer: BufferedWriter? = null
            return try {
                val fos = context.contentResolver.openOutputStream(uri)
                writer = BufferedWriter(OutputStreamWriter(fos))
                writeToFileHelper(logs, writer)
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                writer?.close()
            }
        }

        private fun writeToFileHelper(
                logs: List<Log>,
                writer: BufferedWriter
        ) {
            writer.write(LOG_FILE_HEADER_FMT.format(logs.size))
            writer.newLine()
            for (log in logs) {
                writer.write(log.toString())
            }
            writer.flush()
        }

        private fun getDefaultBuffers(): Set<String> {
            val result = mutableSetOf<String>()

            val stdoutList = mutableListOf<String>()
            CommandUtils.runCmd(cmd = listOf("logcat", "-g"), stdoutList = stdoutList)

            for (s in stdoutList) {
                val colonIndex = s.indexOf(":")
                if (colonIndex != -1) {
                    if (s.startsWith("/")) {
                        val sub = s.substring(0, colonIndex)
                        val lastSlashIndex = sub.lastIndexOf("/")
                        if (lastSlashIndex != -1) {
                            result += sub.substring(lastSlashIndex + 1)
                        }
                    } else {
                        result += s.substring(0, colonIndex)
                    }
                }
            }

            return result
        }

        private fun getAvailableBuffers(): Array<String> {
            val stdoutList = mutableListOf<String>()
            CommandUtils.runCmd(
                    cmd = listOf("logcat", "-h"),
                    stdoutList = stdoutList, redirectStderr = true
            )

            val helpText = getBufferHelpText(stdoutList)

            val buffers = mutableListOf<String>()
            if (helpText.firstOrNull()?.run {
                    contains("request alternate ring buffer", ignoreCase = true) &&
                            endsWith(":")
                } == true
            ) {
                if (helpText.size >= 2) {
                    buffers += helpText[1].split(" ")
                }
            }

            val pattern = "'[a-z]+'".toRegex()
            for (s in helpText) {
                pattern.findAll(s).forEach { match ->
                    match.value.let {
                        buffers += it.substring(1, it.length - 1)
                    }
                }
            }

            buffers -= "default"
            buffers -= "all"

            return buffers.toTypedArray().sortedArray()
        }

        private fun getBufferHelpText(stdout: List<String>): List<String> {
            val startPattern = "^\\s+-b,?.*<buffer>\\s+".toRegex()
            val start = stdout.indexOfFirst {
                startPattern.find(it)?.range?.start == 0
            }
            if (start == -1) {
                return emptyList()
            }

            val endPattern = "^\\s+-[a-zA-Z],?\\s+".toRegex()
            var end = stdout.subList(start + 1, stdout.size).indexOfFirst {
                endPattern.find(it)?.range?.start == 0
            }
            if (end == -1) {
                end = stdout.size
            } else {
                end += start + 1
            }

            return stdout.subList(start, end).map { it.trim() }.toList()
        }
    }
}
