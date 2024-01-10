package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.FOSSDao
import app.simple.inure.database.migration.FOSSMigration
import app.simple.inure.models.FOSS
import app.simple.inure.util.NullSafety.isNull

@Database(entities = [FOSS::class], exportSchema = true, version = 2)
abstract class FOSSDatabase : RoomDatabase() {
    abstract fun getFOSSDao(): FOSSDao?

    companion object {
        private var instance: FOSSDatabase? = null
        private var migration_1_2 = FOSSMigration(1, 2)
        private const val db_name = "foss_markings.db"

        fun getFOSSDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): FOSSDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, FOSSDatabase::class.java, db_name)
                    .addMigrations(migration_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, FOSSDatabase::class.java, db_name)
                        .addMigrations(migration_1_2)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return instance
        }
    }
}