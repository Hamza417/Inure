package app.simple.inure.database.dao

import androidx.room.*
import app.simple.inure.models.QuickApp

@Dao
interface QuickAppsDao {

    /**
     * Get list of all [QuickApp] entities sorted in
     * descending order by [QuickApp.date]
     */
    @Query("SELECT * FROM quick_apps ORDER BY date_added DESC")
    fun getAllQuickApps(): MutableList<QuickApp>

    /**
     * Get list of all [QuickApp] entities sorted in
     * ascending order by [QuickApp.packageName]
     */
    @Query("SELECT * FROM quick_apps ORDER BY package_id COLLATE nocase ASC")
    fun getQuickApps(): MutableList<QuickApp>

    /**
     * Insert a new entry in the table. If the entry
     * already exist it will ignore/cancel
     * the insertion.
     *
     * @param quickApp [QuickApp]
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuickApp(quickApp: QuickApp)

    /**
     * Deletes the given quick app instance
     * from the table
     */
    @Delete
    suspend fun deleteQuickApp(quickApp: QuickApp)

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM quick_apps")
    fun nukeTable()

}