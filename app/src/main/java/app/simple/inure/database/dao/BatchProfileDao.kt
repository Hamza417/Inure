package app.simple.inure.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.simple.inure.models.BatchProfile

@Dao
interface BatchProfileDao {

    @Query("SELECT * FROM batch_profile ORDER BY date_created DESC")
    fun getBatchProfiles(): List<BatchProfile>

    @Query("SELECT * FROM batch_profile WHERE id = :id")
    fun getBatchProfile(id: Int): BatchProfile

    @Query("SELECT * FROM batch_profile WHERE profile_name = :name")
    fun getBatchProfile(name: String): BatchProfile

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBatchProfile(batchProfile: BatchProfile)

    @Update
    fun updateBatchProfile(batchProfile: BatchProfile)

    @Query("DELETE FROM batch_profile WHERE id = :id")
    fun deleteBatchProfile(id: Int)

    @Delete
    fun deleteBatchProfile(batchProfile: BatchProfile)

    @Query("DELETE FROM batch_profile")
    fun nukeTable()
}