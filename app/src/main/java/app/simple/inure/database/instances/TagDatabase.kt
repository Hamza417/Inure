package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.models.Tag
import app.simple.inure.util.ConditionUtils.isNull

@Database(entities = [Tag::class], exportSchema = true, version = 1)
abstract class TagDatabase : RoomDatabase() {
    abstract fun getTagDao(): TagDatabase?

    companion object {
        private var instance: TagDatabase? = null
        private const val db_name = "tag_data.db"

        fun getTagDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): TagDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, TagDatabase::class.java, db_name)
                    .fallbackToDestructiveMigration()
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, TagDatabase::class.java, db_name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return instance
        }
    }
}