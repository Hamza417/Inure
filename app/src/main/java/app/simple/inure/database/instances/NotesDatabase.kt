package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.NotesDao
import app.simple.inure.models.NotesModel
import app.simple.inure.util.NullSafety.isNull

@Database(entities = [NotesModel::class], exportSchema = true, version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun getNotesDao(): NotesDao?

    companion object {
        private var instance: NotesDatabase? = null
        private const val DB_NAME = "notes_data.db"

        fun getNotesDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): NotesDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, NotesDatabase::class.java, DB_NAME)
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, NotesDatabase::class.java, DB_NAME)
                        .build()
                }
            }

            return instance
        }
    }
}