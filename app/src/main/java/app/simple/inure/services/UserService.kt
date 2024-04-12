package app.simple.inure.services

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.Keep
import app.simple.inure.IUserService
import app.simple.inure.util.ExecuteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

class UserService() : IUserService.Stub() {
    @Keep
    constructor(context: Context) : this() {
        Log.i(TAG, "constructor with Context: context=$context")
    }

    override fun destroy() {
        Log.d(TAG, "destroy")
        exitProcess(0)
    }

    override fun exit() {
        Log.d(TAG, "exit")
        destroy()
    }

    override fun execute(cmdarray: MutableList<String>?, envp: MutableList<String>?, dir: String?): ExecuteResult = runBlocking(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(cmdarray?.toTypedArray(), envp?.toTypedArray(), dir?.let {
            File(it)
        })
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        Log.d(TAG, "output: \n$output")
        return@runBlocking ExecuteResult(exitCode, error, output)
    }

    override fun executeInputStream(cmdarray: MutableList<String>?, envp: MutableList<String>?, dir: String?, inputPipe: ParcelFileDescriptor?): ExecuteResult = runBlocking(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(cmdarray?.toTypedArray(), envp?.toTypedArray(), dir?.let {
            File(it)
        })
        process.outputStream.write(inputPipe?.let {
            val inputStream = ParcelFileDescriptor.AutoCloseInputStream(it)
            inputStream.readBytes()
        })
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        return@runBlocking ExecuteResult(exitCode, error, output)
    }

    override fun simpleExecute(command: String?): ExecuteResult = runBlocking(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(command)
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        return@runBlocking ExecuteResult(exitCode, error, output)
    }

    override fun forceStopApp(packageName: String?): Boolean {
        Log.d(TAG, "forceStopApp: $packageName")
        return try {
            Runtime.getRuntime().exec("am force-stop $packageName").waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "forceStopApp: $e")
            false
        }
    }

    companion object {
        private const val TAG = "UserService"
    }
}
