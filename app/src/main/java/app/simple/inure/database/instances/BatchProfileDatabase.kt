package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.BatchProfileDao
import app.simple.inure.models.BatchProfile
import app.simple.inure.util.ConditionUtils.invert

@Database(entities = [BatchProfile::class], exportSchema = true, version = 3)
abstract class BatchProfileDatabase : RoomDatabase() {
    abstract fun batchProfileDao(): BatchProfileDao?

    companion object {
        private var instance: BatchProfileDatabase? = null
        private const val db_name = "batch_profile.db"

        fun getBatchProfileDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): BatchProfileDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, BatchProfileDatabase::class.java, db_name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, BatchProfileDatabase::class.java, db_name)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }
    }
}