package app.simple.inure.loaders

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

object AppDataLoader {

    private const val filename = "inure_data_@date.inrbkp"

    fun Context.exportAppData(): String {
        val paths = mutableListOf(
                SharedPreferences.getSharedPreferencesPath(this).toFile(),
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

        val zipPath = filesDir.path + "/backups/" + "/${filename.replace("@date", System.currentTimeMillis().toString())}"
        ZipFile(zipPath).addFiles(paths)
        return zipPath
    }

    fun Context.importAppData(path: String) {

    }
}