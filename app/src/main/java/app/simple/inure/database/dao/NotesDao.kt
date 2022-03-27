package app.simple.inure.database.dao

import androidx.room.*
import app.simple.inure.models.NotesModel

@Dao
interface NotesDao {
    @Query("SELECT * FROM app_notes_data ORDER BY package_id COLLATE nocase")
    suspend fun getAllNotes(): MutableList<NotesModel>

    /**
     * Delete a [NotesModel] item
     * from the table
     */
    @Delete
    suspend fun deleteNote(notesModel: NotesModel)

    /**
     * Insert [NotesModel] item
     * into the table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(notesModel: NotesModel)

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM app_notes_data")
    fun nukeTable()
}