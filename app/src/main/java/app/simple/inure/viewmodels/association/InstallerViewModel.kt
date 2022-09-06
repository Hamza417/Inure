package app.simple.inure.viewmodels.association

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.apk.installer.InstallerUtils
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.io.File

class InstallerViewModel(application: Application, private val uri: Uri) : WrappedViewModel(application) {

    private val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_META_DATA or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SIGNING_CERTIFICATES or
                PackageManager.GET_SHARED_LIBRARY_FILES
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_META_DATA or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SIGNATURES or
                PackageManager.GET_SHARED_LIBRARY_FILES
    }

    private val filter = IntentFilter().also {
        it.addAction(ServiceConstants.actionSessionStatus)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("InstallerViewModel action -> ${intent.action}")
            println(intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999))
        }
    }

    init {
        LocalBroadcastManager.getInstance(applicationContext()).registerReceiver(broadcastReceiver, filter)
    }

    private val packageInfo: MutableLiveData<PackageInfo> by lazy {
        MutableLiveData<PackageInfo>().also {
            viewModelScope.launch(Dispatchers.Default) {
                prepareInstallation()
            }
        }
    }

    val installing = MutableLiveData<Int>()

    private val file: MutableLiveData<File> by lazy {
        MutableLiveData<File>()
    }

    fun getPackageInfo(): LiveData<PackageInfo> {
        return packageInfo
    }

    fun getFile(): LiveData<File> {
        return file
    }

    private fun prepareInstallation() {
        kotlin.runCatching {
            PackageData.makePackageFolder(applicationContext())

            uri.let { it ->
                val name = DocumentFile.fromSingleUri(applicationContext(), it)?.name
                val file = applicationContext().getInstallerDir(name!!)

                if (!file.exists()) {
                    contentResolver.openInputStream(it).use {
                        FileUtils.copyStreamToFile(it!!, file)
                    }
                }

                val p = packageManager.getPackageArchiveInfo(file.path, flags)!!

                ApkFile(file).use {
                    p.applicationInfo.name = it.apkMeta.label
                }

                this.file.postValue(file)
                packageInfo.postValue(p)
            }
        }.getOrElse {
            error.postValue(it.stackTraceToString())
        }
    }

    fun install() {
        viewModelScope.launch(Dispatchers.Default) {
            val file = this@InstallerViewModel.file.value!!
            val sessionParams = InstallerUtils.makeInstallParams(file.length())
            val sessionCode = InstallerUtils.createSession(sessionParams, applicationContext())
            println("sessionCode -> $sessionCode")

            // TODO create a loop for split apks
            if (file.exists() && file.name.endsWith(".apk")) {
                installing.postValue((1..100).random())
                InstallerUtils.installWriteSessions(sessionCode, file, applicationContext())
            }

            InstallerUtils.commitSession(sessionCode, applicationContext())
            installing.postValue(0)
        }
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(applicationContext()).unregisterReceiver(broadcastReceiver)
    }
}