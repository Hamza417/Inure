package app.simple.inure.database.instances

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.StackTraceDao
import app.simple.inure.models.StackTrace
import app.simple.inure.util.ConditionUtils.invert

@Database(entities = [StackTrace::class], exportSchema = true, version = 3, autoMigrations = [AutoMigration(from = 2, to = 3)])
abstract class StackTraceDatabase : RoomDatabase() {
    abstract fun stackTraceDao(): StackTraceDao?

    companion object {
        private var instance: StackTraceDatabase? = null
        private const val db_name = "stacktrace.db"

        @Synchronized
        fun init(context: Context) {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, StackTraceDatabase::class.java, db_name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, StackTraceDatabase::class.java, db_name)
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }

        @Synchronized
        fun getInstance(context: Context): StackTraceDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, StackTraceDatabase::class.java, db_name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, StackTraceDatabase::class.java, db_name)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }

        @Synchronized
        fun getInstance(): StackTraceDatabase? {
            return instance
        }
    }
}