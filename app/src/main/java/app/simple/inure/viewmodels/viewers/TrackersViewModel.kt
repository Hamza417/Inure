package app.simple.inure.viewmodels.viewers

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.TrackersPreferences
import app.simple.inure.trackers.utils.PackageUtils.*
import app.simple.inure.trackers.utils.UriUtils
import app.simple.inure.util.IOUtils
import dalvik.system.DexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.security.MessageDigest

class TrackersViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private var sha256 = ""
    private var msg = "THIS IS LongPress:\n\n==>COMPLETE CLASS LIST"

    var keyword: String? = null
        set(value) {
            field = value
            organizeData()
        }

    private var isLoaded = false
    private var Signz = 0
    private var Totalz = 0
    private var ClassTotalz = 0
    private var totalTT = 0

    private val classesList = ArrayList<String>()
    private val classesListAll = ArrayList<String>()
    private var Sign: Array<String>? = null
    private var Names: Array<String>? = null
    private var SignStat: IntArray? = null
    private var SignB: BooleanArray? = null

    private val classesListData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().also {
            fetchClassesList()
        }
    }

    private val message: MutableLiveData<Pair<String, String>> by lazy {
        MutableLiveData<Pair<String, String>>()
    }

    fun getClassesList(): LiveData<ArrayList<String>> {
        return classesListData
    }

    fun getMessage(): LiveData<Pair<String, String>> {
        return message
    }

    private fun fetchClassesList() {
        viewModelScope.launch(Dispatchers.IO) {
            organizeData()
        }
    }

    fun organizeData() {
        if (isLoaded) {
            if (keyword.isNullOrEmpty()) {
                if (TrackersPreferences.isFullClassesLis()) {
                    classesListData.postValue(classesListAll)
                } else {
                    classesListData.postValue(classesList)
                }
            } else {
                filterClasses()
            }
        } else {
            kotlin.runCatching {
                loadClasses()
                subStats()
            }.getOrElse {
                error.postValue(it.stackTraceToString())
            }
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun loadClasses() {
        runBlocking {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_META_DATA or PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_META_DATA or PackageManager.GET_SIGNATURES)
            }

            val uriStream = UriUtils.getStreamFromUri(context, Uri.fromFile(File(packageInfo.applicationInfo.publicSourceDir)))

            try {
                val incomeFile = File.createTempFile("classes" + Thread.currentThread().id, ".dex", createTrackersCacheDirectory())
                val bytes = IOUtils.toByteArray(uriStream)
                IOUtils.bytesToFile(bytes, incomeFile)
                val optimizedFile = File.createTempFile("opt" + Thread.currentThread().id, ".dex", createTrackersCacheDirectory())

                val dexFile = DexFile.loadDex(incomeFile.path, optimizedFile.path, 0)

                msg = ""
                Sign = context.resources.getStringArray(R.array.trackers)
                SignStat = IntArray(Sign!!.size)
                SignB = BooleanArray(Sign!!.size) //Arrays.fill(SignB,false);
                Names = context.resources.getStringArray(R.array.tname)

                val classNames = dexFile.entries()

                while (classNames.hasMoreElements()) {
                    val className = classNames.nextElement()
                    classesListAll.add(className)
                    ClassTotalz++
                    if (className.length > 8) {
                        if (className.contains(".")) {
                            Signz = 0
                            while (Signz < Sign!!.size) {
                                totalTT++ //TESTING only
                                if (className.contains(Sign!![Signz])) {
                                    classesList.add(className)
                                    SignStat!![Signz]++
                                    SignB!![Signz] = true
                                    if (msg.contains(Names!![Signz])) {
                                        break
                                    } else {
                                        msg += Names!![Signz] + "\n"
                                    }
                                    Totalz++
                                    break
                                }
                                Signz++
                            }
                        }
                    }
                }

                sha(bytes)
                context.deleteFile("*")
                incomeFile.delete()
                optimizedFile.delete()
            } catch (e: Exception) {
                // ODEX, need to see how to handle
                e.printStackTrace()
            } finally {
                uriStream.close()
            }
        }

        // Post classes list to the UI Controller
        if (TrackersPreferences.isFullClassesLis()) {
            classesListData.postValue(classesListAll)
        } else {
            classesListData.postValue(classesList)
        }

        isLoaded = true
    }

    private fun filterClasses() {
        val list = arrayListOf<String>()

        val listOfAllClasses = if (TrackersPreferences.isFullClassesLis()) {
            classesListAll
        } else {
            classesList
        }

        for (classes in listOfAllClasses) {
            if (classes.lowercase().contains(keyword!!.lowercase())) {
                list.add(classes)
            }
        }

        classesListData.postValue(list)
    }

    private fun sha(bytes: ByteArray) {
        sha256 += "\nMD5sum: " + convertS(MessageDigest.getInstance("md5").digest(bytes))
            .toString() + "\nSHA1sum: " + convertS(MessageDigest.getInstance("sha1").digest(bytes))
            .toString() + "\nSHA256sum: " + convertS(MessageDigest.getInstance("sha256").digest(bytes))

        val pInfo = packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_META_DATA or PackageManager.GET_SERVICES or PackageManager.GET_SERVICES)
        if (Build.VERSION.SDK_INT >= 29) sha256 += apkIsolatedZygote(packageInfo, getString(R.string.app_zygote).trimIndent())
        sha256 += apkCert(pInfo)
    }

    private fun subStats() {
        var statsMsg = ""
        var i = 0
        val message = StringBuilder()
        val title: String?

        if (Signz >= 0) {
            i = 0
            while (i < Sign!!.size) {
                if (SignB!![i]) {
                    if (!statsMsg.contains(Names!![i])) {
                        statsMsg += "*${Names!![i]}".trimIndent()
                    }

                    statsMsg += "${SignStat!![i]}${Sign!![i]}".trimIndent()
                }
                i++
            }
        }

        message.append("$i tested signatures on $ClassTotalz classes ($totalTT)")
        message.append("\n\n")
        message.append(msg)
        message.append("\n")
        message.append(statsMsg)
        message.append("\n")
        message.append(sha256)

        title = Totalz.toString() + " Trackers = " + classesList.size + " Classes"

        this.message.postValue(Pair(title, message.toString()))
    }

    private fun createTrackersCacheDirectory(): File {
        val file = File("${context.cacheDir}/trackers_cache/")
        if (!file.exists()) {
            file.mkdir()
            if (file.isDirectory) {
                return file
            } else {
                // Technically we should be able to create a dir in
                // app directory without raising any flags
                // so it will never reach this block
                throw IOException("Cannot create directory")
            }
        }

        return file
    }
}