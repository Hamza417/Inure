package app.simple.inure.activities.app

import android.app.ActivityManager
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.app.FullVersion.Companion.showFullVersion
import app.simple.inure.dialogs.app.Sure
import app.simple.inure.dialogs.miscellaneous.Warning
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.loaders.AppDataLoader.exportAppData
import app.simple.inure.loaders.AppDataLoader.importAppData
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.services.DataLoaderService
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.activity.ManageSpaceViewModel
import com.anggrayudi.storage.extension.openInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class ManageSpace : BaseActivity() {

    private lateinit var clearData: DynamicRippleTextView
    private lateinit var clearImagesData: DynamicRippleTextView
    private lateinit var imagesSize: TypeFaceTextView
    private lateinit var clearAppDataLoader: CustomProgressBar
    private lateinit var imagesLoader: CustomProgressBar
    private lateinit var appDataLoader: CustomProgressBar
    private lateinit var import: DynamicRippleTextView
    private lateinit var export: DynamicRippleTextView

    private lateinit var manageSpaceViewModel: ManageSpaceViewModel

    private var filePath: String? = null

    private val exportData = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            appDataLoader.gone(animate = true)
            return@registerForActivityResult
        }
        try {
            applicationContext.contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(filePath?.let { File(it).readBytes() })
                outputStream.flush()
                showWarning(R.string.saved_successfully, goBack = false)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    private val pickedFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            appDataLoader.gone(animate = true)
            return@registerForActivityResult
        }

        lifecycleScope.launch(Dispatchers.IO) {
            uri.openInputStream(applicationContext).use {
                kotlin.runCatching {
                    val file: File = (applicationContext.filesDir.absolutePath + "/rstr.inrbkp").toFile()

                    if (file.exists()) {
                        file.delete()
                    }

                    file.createNewFile()
                    file.writeBytes(it?.readBytes()!!)
                    filePath = file.absolutePath

                    applicationContext.importAppData(filePath!!)

                    withContext(Dispatchers.Main) {
                        appDataLoader.gone(animate = true)
                        Log.d("ManageSpace", "onActivityResult: broadcast sent")
                        LocalBroadcastManager.getInstance(applicationContext)
                            .sendBroadcast(Intent().apply {
                                action = DataLoaderService.REFRESH
                            })

                        LocalBroadcastManager.getInstance(applicationContext)
                            .sendBroadcast(Intent().apply {
                                action = DataLoaderService.RELOAD_QUICK_APPS
                            })

                        recreate()
                    }
                }
            }.onFailure {
                it.printStackTrace()
                withContext(Dispatchers.Main) {
                    appDataLoader.gone(animate = true)
                    showWarning(it.message ?: "Unknown error", false)
                }
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    appDataLoader.gone(animate = true)
                    showWarning(R.string.done, goBack = false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_space)
        manageSpaceViewModel = ViewModelProvider(this)[ManageSpaceViewModel::class.java]

        clearData = findViewById(R.id.clear_app_data)
        clearAppDataLoader = findViewById(R.id.clear_app_data_loader)
        clearImagesData = findViewById(R.id.clear_image_data)
        imagesSize = findViewById(R.id.image_cache_size)
        imagesLoader = findViewById(R.id.image_cache_loader)
        appDataLoader = findViewById(R.id.app_data_loader)
        import = findViewById(R.id.import_app_data)
        export = findViewById(R.id.export_app_data)

        clearImagesData.gone(animate = false)
        imagesSize.gone(animate = false)

        clearData.setOnClickListener {
            clearAppData()
        }

        import.setOnClickListener {
            if (TrialPreferences.isFullVersion()) {
                kotlin.runCatching {
                    appDataLoader.visible(animate = true)
                    pickedFile.launch("application/*")
                }.onFailure {
                    appDataLoader.gone(animate = true)
                    showWarning(it.message ?: "Unknown error", false)
                }
            } else {
                supportFragmentManager.showFullVersion()
            }
        }

        export.setOnClickListener {
            if (TrialPreferences.isFullVersion()) {
                appDataLoader.visible(animate = true)
                lifecycleScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        val exportPath = applicationContext.exportAppData()

                        withContext(Dispatchers.Main) {
                            filePath = exportPath
                            exportData.launch(exportPath.substringAfterLast("/"))
                            appDataLoader.gone(animate = true)
                        }
                    }.onFailure {
                        withContext(Dispatchers.Main) {
                            appDataLoader.gone(animate = true)
                            showWarning(it.message ?: "Unknown error", false)
                        }
                    }
                }
            } else {
                supportFragmentManager.showFullVersion()
            }
        }

        manageSpaceViewModel.imagesCacheSize.observe(this) {
            imagesSize.visible(animate = true)
            imagesSize.text = it
            imagesLoader.gone(animate = true)
            clearImagesData.visible(animate = true)

            clearImagesData.setOnClickListener {
                val p = Sure.newInstance()
                p.setOnSureCallbackListener(object : SureCallbacks {
                    override fun onSure() {
                        imagesLoader.visible(animate = true)
                        manageSpaceViewModel.clearImagesData()
                    }
                })

                p.show(supportFragmentManager, "sure")
            }
        }
    }

    @Suppress("unused")
    fun clearData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // TODO - implement this
            kotlin.runCatching {
                val cache = applicationContext.cacheDir
                val appDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    applicationContext.dataDir
                } else {
                    cache.parent?.let { File(it) }
                }
                if (appDir?.exists() == true) {
                    val children = appDir.list()
                    for (child in children!!) {
                        if (!child.equals("lib")) {
                            deleteDir(File(appDir, child))
                            Log.i(javaClass.name, "File /data/data/${applicationContext.packageName}/$child DELETED")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    recreate()
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    showError(it.stackTraceToString())
                }
            }
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children!!) {
                val success = deleteDir(File(dir, i))
                if (!success) {
                    return false
                }
            }
        }

        return dir!!.delete()
    }

    private fun clearAppData() {
        val p = Sure.newInstance()
        p.setOnSureCallbackListener(object : SureCallbacks {
            override fun onSure() {
                if ((applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData().invert()) {
                    Warning.newInstance(getString(R.string.failed)).show(supportFragmentManager, "warning")
                }
            }
        })
        p.show(supportFragmentManager, "sure")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AppearancePreferences.theme -> {
                ThemeUtils.setAppTheme(resources)
            }
        }
    }
}