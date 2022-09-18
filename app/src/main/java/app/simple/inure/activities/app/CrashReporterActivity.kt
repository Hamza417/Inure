package app.simple.inure.activities.app

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.factories.dialog.ErrorViewModelFactory
import app.simple.inure.preferences.CrashPreferences
import app.simple.inure.viewmodels.dialogs.ErrorViewModel

class CrashReporterActivity : BaseActivity() {

    private lateinit var error: TypeFaceTextView
    private lateinit var send: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var errorViewModel: ErrorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)

        error = findViewById(R.id.error)
        send = findViewById(R.id.send)
        close = findViewById(R.id.close)

        intent.getStringExtra("crashLog")?.let { crash ->
            errorViewModel = ViewModelProvider(this, ErrorViewModelFactory(crash))[ErrorViewModel::class.java]

            errorViewModel.getSpanned().observe(this) {
                error.text = it
            }

            send.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.the_app_has_crashed))
                shareIntent.putExtra(Intent.EXTRA_TEXT, crash.trim().trimIndent())
                startActivity(Intent.createChooser(shareIntent, "Crash Log"))
            }
        }

        close.setOnClickListener {
            close()
        }
    }

    override fun onBackPressed() {
        close()
    }

    private fun close() {
        if (CrashPreferences.getCrashLog() != null) {
            CrashPreferences.saveCrashLog(null)
        }
        finish()
    }
}