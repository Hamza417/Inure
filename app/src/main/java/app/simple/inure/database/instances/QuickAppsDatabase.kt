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
        private const val db_name = "quickapps.db"

        @Synchronized
        fun getInstance(context: Context): QuickAppsDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, QuickAppsDatabase::class.java, db_name)
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, QuickAppsDatabase::class.java, db_name)
                        .build()
                }
            }

            return instance
        }

        @Synchronized
        fun getInstance(context: Context, db_name: String): QuickAppsDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, QuickAppsDatabase::class.java, db_name)
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, QuickAppsDatabase::class.java, db_name)
                        .build()
                }
            }

            return instance
        }
    }
}