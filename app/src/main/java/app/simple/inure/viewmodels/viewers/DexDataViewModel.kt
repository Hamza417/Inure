package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import dalvik.system.DexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class DexDataViewModel(application: Application, private val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val classes = ArrayList<String>()

    private val dexData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().also {
            loadDexData()
        }
    }

    fun getDexClasses(): MutableLiveData<ArrayList<String>> {
        return dexData
    }

    private fun loadDexData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                dexData.postValue(getClassesOfPackage(packageInfo.packageName))
            }.getOrElse {
                postError(it)
            }
        }
    }

    @Suppress("DEPRECATION") // Why is Android so hard to work with? :(
    private fun getClassesOfPackage(packageName: String): ArrayList<String> {
        try {
            val appContext = applicationContext().createPackageContext(packageName, 0)
            val packageCodePath: String = appContext.packageCodePath
            val df = DexFile(packageCodePath)
            val iter = df.entries()

            while (iter.hasMoreElements()) {
                val className = iter.nextElement()
                classes.add(className)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return classes
    }
}