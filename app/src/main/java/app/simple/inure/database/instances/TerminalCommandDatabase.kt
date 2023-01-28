package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.TerminalCommandDao
import app.simple.inure.models.TerminalCommand
import app.simple.inure.util.ConditionUtils.invert

@Database(entities = [TerminalCommand::class], exportSchema = true, version = 1)
abstract class TerminalCommandDatabase : RoomDatabase() {

    abstract fun terminalCommandDao(): TerminalCommandDao?

    companion object {
        private var instance: TerminalCommandDatabase? = null
        private const val db_name = "terminal_commands.db"

        @Synchronized
        fun getInstance(context: Context): TerminalCommandDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, TerminalCommandDatabase::class.java, db_name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, TerminalCommandDatabase::class.java, db_name)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }
    }
}