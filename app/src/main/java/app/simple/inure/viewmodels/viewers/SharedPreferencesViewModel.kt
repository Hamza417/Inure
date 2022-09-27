package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.libsu.IRootService
import app.simple.inure.services.RootService
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedPreferencesViewModel(private val packageInfo: PackageInfo, application: Application) : WrappedViewModel(application) {

    private val tag = javaClass.simpleName
    private var aidlConn: AIDLConnection? = null
    private var daemonConn: AIDLConnection? = null
    private var fileSystemManager: FileSystemManager? = null

    init {
        val intent = Intent(applicationContext(), RootService::class.java)
        com.topjohnwu.superuser.ipc.RootService.bind(intent, AIDLConnection(false))
    }

    private val sharedPrefsFiles: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    fun getSharedPrefs(): LiveData<ArrayList<String>> {
        return sharedPrefsFiles
    }

    private fun loadSharedPrefsFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(Shell.Builder.create()
                                        .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                        .setTimeout(10))
            sharedPrefsFiles.postValue(fileSystemManager?.getFile(packageInfo.applicationInfo.dataDir + "/shared_prefs/")?.list()?.toList() as ArrayList<String>?)
        }
    }

    private fun stopService() {
        com.topjohnwu.superuser.ipc.RootService.unbind(aidlConn!!)
    }

    override fun onCleared() {
        super.onCleared()
        stopService()
    }

    inner class AIDLConnection(private val isDaemon: Boolean) : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(tag, "AIDL onServiceConnected")
            if (isDaemon) {
                daemonConn = this
            } else {
                aidlConn = this
            }
            val ipc: IRootService = IRootService.Stub.asInterface(service)
            try {
                // consoleList.add("AIDL PID : " + ipc.pid)
                // consoleList.add("AIDL UID : " + ipc.uid)
                // consoleList.add("AIDL UUID: " + ipc.uuid)
                if (!isDaemon) {
                    // Get the remote file system service proxy through AIDL
                    val binder: IBinder = ipc.fileSystemService
                    // Create a fs manager with the binder proxy.
                    // We will use this fs manager in our stress test.
                    fileSystemManager = FileSystemManager.getRemote(binder)
                }
            } catch (e: RemoteException) {
                Log.e(tag, "Remote error", e)
            }
            loadSharedPrefsFiles()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(tag, "AIDL onServiceDisconnected")
            if (isDaemon) {
                daemonConn = null
            } else {
                aidlConn = null
                fileSystemManager = null
            }
        }
    }
}