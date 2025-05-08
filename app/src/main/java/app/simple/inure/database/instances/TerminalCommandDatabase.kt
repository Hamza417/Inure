package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.inure.database.dao.TerminalCommandDao
import app.simple.inure.models.TerminalCommand
import app.simple.inure.util.ConditionUtils.invert

@Database(entities = [TerminalCommand::class], exportSchema = true, version = 2)
abstract class TerminalCommandDatabase : RoomDatabase() {

    abstract fun terminalCommandDao(): TerminalCommandDao?

    companion object {
        private var instance: TerminalCommandDatabase? = null
        private const val DB_NAME = "terminal_commands.db"

        fun getTerminalCommandDataPath(context: Context): String {
            return getInstance(context)!!.openHelper.writableDatabase.path!!
        }

        @Synchronized
        fun getInstance(context: Context): TerminalCommandDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, TerminalCommandDatabase::class.java, DB_NAME)
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, TerminalCommandDatabase::class.java, DB_NAME)
                    .build()
            }

            return instance
        }
    }
}