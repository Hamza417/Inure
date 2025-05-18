package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.BatchDao
import app.simple.inure.models.BatchModel
import app.simple.inure.util.ConditionUtils.invert

@Database(entities = [BatchModel::class], exportSchema = true, version = 2)
abstract class BatchDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao?

    companion object {
        private var instance: BatchDatabase? = null
        private const val DB_NAME = "batch_data.db"

        fun getBatchDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): BatchDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, BatchDatabase::class.java, DB_NAME)
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, BatchDatabase::class.java, DB_NAME)
                    .build()
            }

            return instance
        }
    }
}