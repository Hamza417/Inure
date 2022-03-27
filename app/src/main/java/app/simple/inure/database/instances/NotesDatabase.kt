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
        private const val db_name = "notes_data.db"

        @Synchronized
        fun getInstance(context: Context): NotesDatabase? {
            instance = if (instance.isNull()) {
                Room.databaseBuilder(context, NotesDatabase::class.java, db_name)
                    .fallbackToDestructiveMigration()
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, NotesDatabase::class.java, db_name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return instance
        }
    }
}