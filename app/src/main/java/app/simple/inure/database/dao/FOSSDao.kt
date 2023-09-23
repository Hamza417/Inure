package app.simple.inure.database.dao

import androidx.room.*
import app.simple.inure.models.FOSS

@Dao
interface FOSSDao {
    @Query("SELECT * FROM foss")
    fun getAllFossMarkings(): List<FOSS>

    /**
     * Delete a [FOSS] item
     * from the table
     */
    @Delete
    fun deleteFOSS(foss: FOSS)

    /**
     * Delete foss item where package name
     */
    @Query("DELETE FROM foss WHERE package_name = :packageName")
    fun deleteFOSS(packageName: String)

    /**
     * Insert a [FOSS] item into the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFOSS(foss: FOSS)

    /**
     * Nuke table
     */
    @Query("DELETE FROM foss")
    suspend fun nukeTable()
}