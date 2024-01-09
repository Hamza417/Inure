package app.simple.inure.services

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import app.simple.inure.IUserService
import app.simple.inure.util.ExecuteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

class ShizukuService() : IUserService.Stub() {
    @Keep
    constructor(context: Context) : this() {
        Log.i(TAG, "constructor with Context: context=$context")
    }

    override fun destroy() {
        Log.d("ShizukuService", "destroy")
        exitProcess(0)
    }

    override fun exit() {
        Log.d("ShizukuService", "exit")
        destroy()
    }

    override fun execute(cmdarray: MutableList<String>?, envp: MutableList<String>?, dir: String?): ExecuteResult = runBlocking(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(cmdarray?.toTypedArray(), envp?.toTypedArray(), dir?.let {
            File(it)
        })
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        return@runBlocking ExecuteResult(exitCode, error, output)
    }

    override fun simpleExecute(command: String?): ExecuteResult = runBlocking(Dispatchers.IO) {
        Log.d("ShizukuService", "simpleExecute: $command")
        val process = Runtime.getRuntime().exec(command)
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        return@runBlocking ExecuteResult(exitCode, error, output)
    }

    companion object {
        private const val TAG = "UserService"
    }
}