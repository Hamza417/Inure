package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.TagDao
import app.simple.inure.models.Tag
import app.simple.inure.util.NullSafety.isNull

@Database(entities = [Tag::class], exportSchema = true, version = 3)
abstract class TagsDatabase : RoomDatabase() {

    abstract fun getTagDao(): TagDao?

    companion object {
        private var instance: TagsDatabase? = null
        private const val DB_NAME = "tag_data.db"

        fun getTagDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): TagsDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, TagsDatabase::class.java, DB_NAME)
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, TagsDatabase::class.java, DB_NAME)
                        .build()
                }
            }

            return instance
        }

        fun destroyInstance() {
            instance?.close()
            instance = null
        }
    }
}
