package app.simple.inure.activities.association

import android.content.Intent
import android.os.Bundle
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.terminal.RunScript
import java.io.File

class BashAssociation : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showWarning("ERR: This is not supported anymore, kindly use the terminal app to run scripts")

        return

        @Suppress("UNREACHABLE_CODE")
        intent.data?.let {
            contentResolver.openInputStream(it)?.use { inputStream ->
                val file = File(applicationContext.cacheDir?.path + "/" + DocumentFile.fromSingleUri(applicationContext, it)!!.name)
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                val intent = Intent(this, RunScript::class.java)
                intent.data = FileProvider.getUriForFile(
                        /* context = */ applicationContext,
                        /* authority = */ "${packageName}.provider",
                        /* file = */ file)
                intent.action = RunScript.ACTION_RUN_SCRIPT
                intent.putExtra(RunScript.EXTRA_SCRIPT_PATH, file.absolutePath)
                startActivity(intent)
                finish()
            }
        }
    }
}