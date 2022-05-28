package app.simple.inure.activities.association

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import app.simple.inure.extension.activities.BaseActivity
import com.anggrayudi.storage.file.getAbsolutePath
import java.io.File

class AppDetailsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println(DocumentFile.fromSingleUri(applicationContext, intent!!.data!!)?.getAbsolutePath(applicationContext)!!)

        val p = packageManager.getPackageArchiveInfo(DocumentFile.fromSingleUri(applicationContext, intent!!.data!!)?.getAbsolutePath(applicationContext)!!,
                                                     PackageManager.GET_META_DATA)

        println(p!!.packageName)
    }

    private fun handleIntent(): String? {
        // A File object containing the path to the transferred files
        // Get intent, action and MIME type
        val intent = intent
        val action = intent.action
        val type = intent.type

        /*
         * For ACTION_VIEW, the Activity is being asked to display data.
         * Get the URI.
         */
        if (Intent.ACTION_VIEW == action && type != null) {
            // Get the URI from the Intent
            val uri = intent.data!!
            if (TextUtils.equals(uri.scheme, "file")) {
                return handleFileUri(uri)
            } else if (TextUtils.equals(uri.scheme, "content")) {
                return handleContentUri(uri)
            }
        }
        return "not a path"
    }

    private fun handleFileUri(beamUri: Uri?): String {
        // Get the path part of the URI
        val fileName = beamUri!!.path!!
        val copiedFile = File(fileName)
        // Get a string containing the file's parent directory
        return copiedFile.parent!!
    }

    @Suppress("deprecation")
    private fun handleContentUri(beamUri: Uri?): String? {
        // Position of the filename in the query Cursor
        val filenameIndex: Int
        // File object for the filename
        val copiedFile: File
        // The filename stored in MediaStore
        val fileName: String
        // Test the authority of the URI
        return if (!TextUtils.equals(beamUri!!.authority, MediaStore.AUTHORITY)) {
            /*
             * Handle content URIs for other content providers
             * For a MediaStore content URI
             */
            beamUri.toString()
        } else {
            // Get the column that contains the file name
            val projection = arrayOf(MediaStore.MediaColumns.DATA)
            val pathCursor = contentResolver.query(beamUri, projection, null, null, null)
            // Check for a valid cursor
            if (pathCursor != null &&
                pathCursor.moveToFirst()) {
                // Get the column index in the Cursor
                filenameIndex = pathCursor.getColumnIndex(
                    MediaStore.MediaColumns.DATA)
                // Get the full file name including path
                fileName = pathCursor.getString(filenameIndex)
                // Create a File object for the filename
                copiedFile = File(fileName)
                // Return the parent directory of the file
                pathCursor.close()
                copiedFile.absolutePath
            } else {
                // The query didn't work; return null
                null
            }
        }
    }
}
