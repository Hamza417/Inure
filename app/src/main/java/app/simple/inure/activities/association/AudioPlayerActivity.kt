package app.simple.inure.activities.association

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.net.toUri
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.models.AudioModel
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.ui.association.AudioPlayer
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ParcelUtils.parcelable
import java.io.File

class AudioPlayerActivity : BaseActivity() {

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                if (intent.hasExtra(BundleConstants.AUDIO)) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, AudioPlayer.newInstance(AudioModel(), fromActivity = true), AudioPlayer.TAG)
                        .commit()
                } else {
                    uri = when {
                        intent?.action == Intent.ACTION_SEND && intent?.type?.startsWith("audio/") == true -> {
                            intent.parcelable(Intent.EXTRA_STREAM)
                        }
                        intent?.action == Intent.ACTION_SEND && intent?.type == "text/plain" -> {
                            intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri()
                        }
                        else -> {
                            intent!!.data
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, AudioPlayer.newInstance(uri!!, fromActivity = true), AudioPlayer.TAG)
                        .commit()
                }
            }.getOrElse {
                showError(it.stackTraceToString())
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeUtils.setAppTheme(resources)
        ThemeUtils.setBarColors(resources, window)
    }

    override fun onThemeChanged(theme: Theme, animate: Boolean) {
        ThemeUtils.setBarColors(resources, window)
    }

    private fun handleIntent(): String? {
        // Get intent, action and MIME type
        val intent = intent
        val action = intent.action
        val type = intent.type

        /*
         * For ACTION_VIEW, the Activity is being asked to display data.
         * Get the URI.
         */
        when {
            Intent.ACTION_VIEW == action && type?.startsWith("audio/") == true -> {
                // Get the URI from the Intent
                val uri = intent.data!!
                when {
                    TextUtils.equals(uri.scheme, "file") -> {
                        return uri.toString()
                    }
                    TextUtils.equals(uri.scheme, "content") -> {
                        return uri.toString()
                    }
                }
            }
            action == Intent.ACTION_SEND && type?.startsWith("audio/") == true -> {
                val uri: Uri? = intent.parcelable(Intent.EXTRA_STREAM)

                return when {
                    TextUtils.equals(uri!!.scheme, "file") -> {
                        handleFileUri(uri)
                    }
                    TextUtils.equals(uri.scheme, "content") -> {
                        handleContentUri(uri)
                    }
                    else -> {
                        uri.toString()
                    }
                }
            }
            action == Intent.ACTION_SEND && type == "text/plain" -> {
                return intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            else -> {
                return intent!!.data.toString()
            }
        }

        return null
    }

    private fun handleFileUri(beamUri: Uri?): String? {
        // Get the path part of the URI
        val fileName = beamUri!!.path!!
        val copiedFile = File(fileName)
        // Get a string containing the file's parent directory
        return copiedFile.getParent()
    }

    private fun handleContentUri(beamUri: Uri?): String? {
        // Position of the filename in the query Cursor
        val filenameIndex: Int
        // File object for the filename
        val copiedFile: File
        // The filename stored in MediaStore
        val fileName: String
        // Test the authority of the URI
        return if (!TextUtils.equals(beamUri!!.authority, MediaStore.AUTHORITY)) {
            // Handle content URIs for other content providers
            // For a MediaStore content URI
            getPath(baseContext, beamUri)
        } else {
            // Get the column that contains the file name
            val projection = arrayOf(MediaStore.MediaColumns.DATA)
            val pathCursor = contentResolver
                .query(beamUri, projection, null, null, null)
            // Check for a valid cursor
            if (pathCursor != null && pathCursor.moveToFirst()) {
                // Get the column index in the Cursor
                filenameIndex = pathCursor.getColumnIndex(MediaStore.MediaColumns.DATA)
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

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    fun getPath(context: Context, uri: Uri?): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), id.toLong())
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                        split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                              selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
                column
        )
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri?): Boolean {
        return "com.android.externalstorage.documents" == uri!!.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri?): Boolean {
        return "com.android.providers.downloads.documents" == uri!!.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri?): Boolean {
        return "com.android.providers.media.documents" == uri!!.authority
    }
}
