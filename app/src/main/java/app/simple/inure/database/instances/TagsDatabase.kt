package app.simple.inure.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.simple.inure.database.dao.TagDao
import app.simple.inure.models.Tag
import app.simple.inure.util.NullSafety.isNull

@Database(entities = [Tag::class], exportSchema = true, version = 4)
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
                    .addMigrations(MIGRATION_3_4)
                    .build()
            } else {
                if (instance!!.isOpen) {
                    return instance
                } else {
                    Room.databaseBuilder(context, TagsDatabase::class.java, DB_NAME)
                        .addMigrations(MIGRATION_3_4)
                        .build()
                }
            }
            return instance
        }

        fun destroyInstance() {
            instance?.close()
            instance = null
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a new temporary table with the new schema
                db.execSQL(
                        """
                    CREATE TABLE new_tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tag TEXT NOT NULL,
                        packages TEXT,
                        icon INTEGER NOT NULL,
                        date_added INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Copy data from the old table to the new one
                db.execSQL(
                        """
                    INSERT INTO new_tags (tag, packages, icon, date_added)
                    SELECT tag, packages, icon, date_added FROM tags
                    """.trimIndent()
                )

                // Drop the old table
                db.execSQL("DROP TABLE tags")

                // Rename the new table to the old table's name
                db.execSQL("ALTER TABLE new_tags RENAME TO tags")

                // Create the unique index on the 'tag' column
                db.execSQL("CREATE UNIQUE INDEX index_tags_tag ON tags(tag)")
            }
        }
    }
}