package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.BatchDao
import app.simple.inure.util.NullSafety.isNull

abstract class BatchDatabase : RoomDatabase(), BatchDao {
    abstract fun batchDao(): BatchDao?

    companion object {
        private var instance: BatchDao? = null
        private const val db_name = "batch_data.db"

        @Synchronized
        fun getInstance(context: Context): BatchDao? {
            if (instance.isNull()) {
                instance = Room.databaseBuilder(context, BatchDatabase::class.java, db_name)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }
    }
}