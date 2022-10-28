package app.simple.inure.database.dao

import androidx.room.*
import app.simple.inure.models.StackTrace

@Dao
interface StackTraceDao {
    @Query("SELECT * FROM stacktrace ORDER BY timestamp DESC")
    suspend fun getStackTraces(): MutableList<StackTrace>

    /**
     * Delete a [StackTrace] item
     * from the table
     */
    @Delete
    suspend fun deleteStackTrace(stackTrace: StackTrace)

    /**
     * Insert [StackTrace] item
     * into the table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrace(stackTrace: StackTrace)

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM stacktrace")
    fun nukeTable()
}