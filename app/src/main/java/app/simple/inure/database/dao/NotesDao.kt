package app.simple.inure.database.dao

import androidx.room.*
import app.simple.inure.models.Notes

@Dao
interface NotesDao {
    @Query("SELECT * FROM app_notes_data ORDER BY package_id COLLATE nocase")
    suspend fun getAllNotes(): MutableList<Notes>

    @Query("SELECT * FROM app_notes_data WHERE package_id = :packageId")
    suspend fun getNoteByPackageId(packageId: String): Notes

    /**
     * Delete a [Notes] item
     * from the table
     */
    @Delete
    suspend fun deleteNote(notes: Notes)

    /**
     * Insert [Notes] item
     * into the table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(notes: Notes)

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM app_notes_data")
    fun nukeTable()
}
