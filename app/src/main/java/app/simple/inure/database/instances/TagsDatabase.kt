package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.TagDao
import app.simple.inure.models.Tag
import app.simple.inure.util.ConditionUtils.isNull

@Database(entities = [Tag::class], exportSchema = true, version = 1)
abstract class TagsDatabase : RoomDatabase() {

    abstract fun getTagDao(): TagDao?

    companion object {
        private var instance: TagsDatabase? = null
        private const val db_name = "tag_data.db"

        fun getTagDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): TagsDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, TagsDatabase::class.java, db_name)
                    .fallbackToDestructiveMigration()
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, TagsDatabase::class.java, db_name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return instance
        }
    }
}