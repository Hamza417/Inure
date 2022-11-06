package app.simple.inure.database.dao

import androidx.room.*
import app.simple.inure.models.BatchModel

@Dao
interface BatchDao {

    @Query("SELECT * FROM batch_state_data ORDER BY package_name COLLATE nocase")
    fun getBatch(): MutableList<BatchModel>

    @Query("SELECT * FROM batch_state_data WHERE selected = 1 ORDER BY package_name COLLATE nocase")
    fun getSelectedApps(): MutableList<BatchModel>

    /**
     * Delete a [BatchModel] item
     * from the table
     */
    @Delete
    suspend fun deleteBatch(batchModel: BatchModel)

    /**
     * Insert a new [BatchModel] item
     * into the table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batchModel: BatchModel)

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM batch_state_data")
    fun nukeTable()
}