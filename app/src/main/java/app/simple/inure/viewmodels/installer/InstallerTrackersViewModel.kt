package app.simple.inure.viewmodels.installer

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.TrackersPreferences
import app.simple.inure.trackers.utils.PackageUtils
import app.simple.inure.trackers.utils.UriUtils
import app.simple.inure.util.IOUtils
import dalvik.system.DexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import app.simple.inure.apk.utils.PackageUtils as PackageUtils1

class InstallerTrackersViewModel(application: Application, private val apkFile: File) : WrappedViewModel(application) {
    private var sha256 = ""
    private var msg = "THIS IS LongPress:\n\n==>COMPLETE CLASS LIST"

    var keyword: String? = null
        set(value) {
            field = value
            organizeData()
        }

    private var isLoaded = false
    private var signs = 0
    private var totals = 0
    private var classTotals = 0
    private var totalTT = 0

    private val classesList = ArrayList<String>()
    private val classesListAll = ArrayList<String>()
    private var sign: Array<String>? = null
    private var names: Array<String>? = null
    private var signStat: IntArray? = null
    private var signB: BooleanArray? = null

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
                if (TrackersPreferences.isFullClassesList()) {
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
                postError(it)
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @SuppressLint("PackageManagerGetSignatures")
    private fun loadClasses() {
        runBlocking {
            val uriStream = UriUtils.getStreamFromUri(context, Uri.fromFile(apkFile))

            try {
                val incomeFile = File.createTempFile("classes" + Thread.currentThread().id, ".dex", createTrackersCacheDirectory())
                val bytes = IOUtils.toByteArray(uriStream)
                IOUtils.bytesToFile(bytes, incomeFile)
                val optimizedFile = File.createTempFile("opt" + Thread.currentThread().id, ".dex", createTrackersCacheDirectory())

                val dexFile = DexFile.loadDex(incomeFile.path, optimizedFile.path, 0)

                msg = ""
                sign = context.resources.getStringArray(R.array.trackers)
                signStat = IntArray(sign!!.size)
                signB = BooleanArray(sign!!.size) //Arrays.fill(SignB,false);
                names = context.resources.getStringArray(R.array.tname)

                val classNames = dexFile.entries()

                while (classNames.hasMoreElements()) {
                    val className = classNames.nextElement()
                    classesListAll.add(className)
                    classTotals++
                    if (className.length > 8) {
                        if (className.contains(".")) {
                            signs = 0
                            while (signs < sign!!.size) {
                                totalTT++ //TESTING only
                                if (className.contains(sign!![signs])) {
                                    classesList.add(className)
                                    signStat!![signs]++
                                    signB!![signs] = true
                                    if (msg.contains(names!![signs])) {
                                        break
                                    } else {
                                        msg += names!![signs] + "\n"
                                    }
                                    totals++
                                    break
                                }
                                signs++
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
                withContext(Dispatchers.IO) {
                    uriStream.close()
                }
            }
        }

        // Post classes list to the UI Controller
        if (TrackersPreferences.isFullClassesList()) {
            classesListData.postValue(classesListAll)
        } else {
            classesListData.postValue(classesList)
        }

        isLoaded = true
    }

    private fun filterClasses() {
        val list = arrayListOf<String>()

        val listOfAllClasses = if (TrackersPreferences.isFullClassesList()) {
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
        sha256 += "\nMD5sum: " + PackageUtils.convertS(MessageDigest.getInstance("md5").digest(bytes))
            .toString() + "\nSHA1sum: " + PackageUtils.convertS(MessageDigest.getInstance("sha1").digest(bytes))
            .toString() + "\nSHA256sum: " + PackageUtils.convertS(MessageDigest.getInstance("sha256").digest(bytes))

        val packageName = ApkFile(apkFile).use {
            it.apkMeta.packageName
        }

        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageArchiveInfo(apkFile.path, PackageManager.PackageInfoFlags.of(PackageUtils1.flags))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(apkFile.path, PackageUtils1.flags.toInt())
        }
        if (Build.VERSION.SDK_INT >= 29) sha256 += PackageUtils.apkIsolatedZygote(packageInfo, getString(R.string.app_zygote).trimIndent())
        sha256 += PackageUtils.apkCert(packageInfo)
    }

    private fun subStats() {
        var statsMsg = ""
        var i = 0
        val message = StringBuilder()
        val title: String?

        if (signs >= 0) {
            i = 0
            while (i < sign!!.size) {
                if (signB!![i]) {
                    if (!statsMsg.contains(names!![i])) {
                        statsMsg += "*${names!![i]}".trimIndent()
                    }

                    statsMsg += "${signStat!![i]}${sign!![i]}".trimIndent()
                }
                i++
            }
        }

        message.append("$i tested signatures on $classTotals classes ($totalTT)")
        message.append("\n\n")
        message.append(msg)
        message.append("\n")
        message.append(statsMsg)
        message.append("\n")
        message.append(sha256)

        title = totals.toString() + " Trackers = " + classesList.size + " Classes"

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