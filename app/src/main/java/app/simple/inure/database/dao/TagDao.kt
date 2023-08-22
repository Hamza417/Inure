package app.simple.inure.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.simple.inure.models.Tag

@Dao
interface TagDao {

    /**
     * Get all tags
     */
    @Query("SELECT * FROM tags ORDER BY tag COLLATE nocase")
    fun getTags(): MutableList<Tag>

    /**
     * Get tag where [Tag.tag] is equal to [tag]
     */
    @Query("SELECT * FROM tags WHERE tag = :tag")
    fun getTag(tag: String): Tag

    /**
     * Get tag where package exists in [Tag.packages]
     *
     * The packages are stored in a string separated
     * by a comma
     */
    @Query("SELECT * FROM tags WHERE packages LIKE '%' || :packageName || '%'")
    fun getTagByPackage(packageName: String): Tag

    /**
     * Delete a [Tag] item
     * from the table
     */
    @Delete
    suspend fun deleteTag(tag: Tag)

    /**
     * Insert a new [Tag] item
     * into the table
     */
    @Insert
    suspend fun insertTag(tag: Tag)

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM tags")
    fun nukeTable()
}