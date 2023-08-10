package app.simple.inure.loaders

import android.annotation.SuppressLint
import android.content.Context
import app.simple.inure.database.instances.BatchDatabase
import app.simple.inure.database.instances.NotesDatabase
import app.simple.inure.database.instances.QuickAppsDatabase
import app.simple.inure.database.instances.StackTraceDatabase
import app.simple.inure.database.instances.TerminalCommandDatabase
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.util.FileUtils.toFile
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object AppDataLoader {

    private const val filename = "inure_data_@date.inrbkp"
    private const val preferences = "prefs_bkp"

    fun Context.exportAppData(): String {
        val paths = mutableListOf(
                saveSharedPreferencesToFile(this).toFile(),
                BatchDatabase.getBatchDataPath(this).toFile(),
                NotesDatabase.getNotesDataPath(this).toFile(),
                QuickAppsDatabase.getQuickAppsDataPath(this).toFile(),
                StackTraceDatabase.getStackTraceDataPath(this).toFile(),
                TerminalCommandDatabase.getTerminalCommandDataPath(this).toFile()
        )

        if (File(filesDir.path + "/backups").exists().not()) {
            File(filesDir.path + "/backups").mkdir()
        } else {
            File(filesDir.path + "/backups").listFiles()?.forEach {
                if (it.name.endsWith(".inrbkp")) {
                    it.delete()
                }
            }
        }

        val pathsTxt = File(filesDir.path + "/backups/paths.txt")

        for (path in paths) {
            pathsTxt.appendText(path.path + "\n")
        }

        paths.add(pathsTxt)

        val zipPath = filesDir.path + "/backups/" +
                "/${filename.replace("@date", System.currentTimeMillis().toString())}"
        ZipFile(zipPath).addFiles(paths)

        return zipPath
    }

    fun Context.importAppData(dataPath: String) {
        val paths = mutableListOf(
                (filesDir.path + "/shared_prefs/" + preferences).toFile(),
                BatchDatabase.getBatchDataPath(this).toFile(),
                NotesDatabase.getNotesDataPath(this).toFile(),
                QuickAppsDatabase.getQuickAppsDataPath(this).toFile(),
                StackTraceDatabase.getStackTraceDataPath(this).toFile(),
                TerminalCommandDatabase.getTerminalCommandDataPath(this).toFile()
        )

        BatchDatabase.getInstance(this)?.close()
        NotesDatabase.getInstance(this)?.close()
        QuickAppsDatabase.getInstance(this)?.close()
        StackTraceDatabase.getInstance(this)?.close()
        TerminalCommandDatabase.getInstance(this)?.close()

        for (path in paths) {
            if (path.exists()) {
                path.delete()
            }
        }

        ZipFile(dataPath).use {
            for (file in it.fileHeaders) {
                for (path in paths) {
                    if (file.fileName.endsWith(path.name)) {
                        if (path.name.equals(preferences)) {
                            it.extractFile(file.fileName, path.parent)
                            loadSharedPreferencesFromFile(path)
                        } else {
                            it.extractFile(file.fileName, path.parent)
                        }
                    }
                }
            }
        }

        SharedPreferences.init(this)
        BatchDatabase.getInstance(this)
        NotesDatabase.getInstance(this)
        QuickAppsDatabase.getInstance(this)
        StackTraceDatabase.getInstance(this)
        TerminalCommandDatabase.getInstance(this)
    }

    private fun saveSharedPreferencesToFile(context: Context): String {
        var output: ObjectOutputStream? = null
        val path = (context.filesDir.path + "/shared_prefs/" + preferences).toFile()

        if (path.exists()) {
            path.delete()
        } else {
            path.parentFile?.mkdirs()
            path.createNewFile()
        }

        try {
            output = ObjectOutputStream(FileOutputStream(path))
            val pref = SharedPreferences.getSharedPreferences()
            output.writeObject(pref.all)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (output != null) {
                    output.flush()
                    output.close()
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }

        return path.absolutePath
    }

    @SuppressLint("ApplySharedPref")
    private fun loadSharedPreferencesFromFile(src: File): Boolean {
        var result = false
        var input: ObjectInputStream? = null

        try {
            input = ObjectInputStream(FileInputStream(src))
            val prefEdit: android.content.SharedPreferences.Editor = SharedPreferences.getSharedPreferences().edit()
            prefEdit.clear()
            @Suppress("UNCHECKED_CAST")
            val entries = input.readObject() as Map<String, *>

            for ((key, value) in entries) {
                when (value) {
                    is Boolean -> prefEdit.putBoolean(key, value)
                    is Float -> prefEdit.putFloat(key, value)
                    is Int -> prefEdit.putInt(key, value)
                    is Long -> prefEdit.putLong(key, value)
                    is String -> prefEdit.putString(key, value)
                    else -> throw IllegalArgumentException("Unknown object type")
                }
            }

            prefEdit.commit()
            result = true

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                input?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }

        return result
    }
}