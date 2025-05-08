package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.QuickAppsDao
import app.simple.inure.models.QuickApp
import app.simple.inure.util.NullSafety.isNull

@Database(entities = [QuickApp::class], exportSchema = true, version = 1)
abstract class QuickAppsDatabase : RoomDatabase() {

    abstract fun quickAppsDao(): QuickAppsDao?

    companion object {
        private var instance: QuickAppsDatabase? = null
        private const val DB_NAME = "quickapps.db"

        fun getQuickAppsDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): QuickAppsDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, QuickAppsDatabase::class.java, DB_NAME)
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, QuickAppsDatabase::class.java, DB_NAME)
                        .build()
                }
            }

            return instance
        }

        @Synchronized
        fun getInstance(context: Context, dbName: String): QuickAppsDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, QuickAppsDatabase::class.java, dbName)
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, QuickAppsDatabase::class.java, dbName)
                        .build()
                }
            }

            return instance
        }
    }
}