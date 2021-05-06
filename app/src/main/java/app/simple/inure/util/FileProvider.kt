package app.simple.inure.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import app.simple.inure.R
import java.io.File

object FileProvider {
     fun openFolder(context: Context, location: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val myDir: Uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", File(location))
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setDataAndType(myDir, DocumentsContract.Document.MIME_TYPE_DIR)

        if (intent.resolveActivityInfo(context.packageManager, 0) != null) {
            context.startActivity(intent)
        } else {

        }
    }
}