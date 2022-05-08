package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.trackers.reflector.ClassesNamesList
import app.simple.inure.trackers.utils.UriUtils
import app.simple.inure.util.IOUtils
import dalvik.system.DexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class TrackersViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private var sha256 = ""
    private var msg = "THIS IS LongPress:\n\n==>COMPLETE CLASS LIST"

    private var Signz = 0
    private var Totalz = 0
    private var ClassTotalz = 0
    private var totalTT = 0

    private val classesList: ClassesNamesList = ClassesNamesList()
    private val classesListAll: ClassesNamesList = ClassesNamesList()
    private var Sign: Array<String>? = null
    private var Names: Array<String>? = null
    private var SignStat: IntArray? = null
    private var SignB: BooleanArray? = null

    private val classesListData: MutableLiveData<ClassesNamesList> by lazy {
        MutableLiveData<ClassesNamesList>().also {
            fetchClassesList()
        }
    }

    fun getClassesList(): LiveData<ClassesNamesList> {
        return classesListData
    }

    private fun fetchClassesList() {
        viewModelScope.launch(Dispatchers.IO) {
            loadClasses()
        }
    }

    private fun loadClasses() {
        runBlocking {
            val uriStream = UriUtils.getStreamFromUri(context, Uri.fromFile(File(packageInfo.applicationInfo.publicSourceDir)))

            try {
                val incomeFile = File.createTempFile("classes" + Thread.currentThread().id, ".dex", context.cacheDir)
                val bytes = IOUtils.toByteArray(uriStream)
                IOUtils.bytesToFile(bytes, incomeFile)
                val optimizedFile = File.createTempFile("opt" + Thread.currentThread().id, ".dex", context.cacheDir)

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
                                totalTT++ //TESTINGonly
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
        classesListData.postValue(classesList)
    }
}