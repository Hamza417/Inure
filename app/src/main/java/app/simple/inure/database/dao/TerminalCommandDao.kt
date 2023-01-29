package app.simple.inure.database.dao

import androidx.room.*
import app.simple.inure.models.TerminalCommand

@Dao
interface TerminalCommandDao {
    @Query("SELECT * FROM terminal_commands ORDER BY date_created COLLATE nocase DESC")
    fun getAllTerminalCommands(): MutableList<TerminalCommand>

    /**
     * Delete a [TerminalCommand] item
     * from the table
     */
    @Delete
    suspend fun deleteTerminalCommand(terminalCommand: TerminalCommand)

    /**
     * Insert a new [TerminalCommand] item
     * into the table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerminalCommand(terminalCommand: TerminalCommand)

    /**
     * Update a [TerminalCommand] item
     */
    @Update
    suspend fun updateTerminalCommand(terminalCommand: TerminalCommand)

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM terminal_commands")
    fun nukeTable()
}