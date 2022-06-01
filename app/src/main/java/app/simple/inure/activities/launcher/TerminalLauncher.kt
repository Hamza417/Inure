package app.simple.inure.activities.launcher

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import app.simple.inure.R
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.terminal.Term

@Deprecated("Poor idea!!, launching terminal without a splash screen works best for now")
class TerminalLauncher : BaseActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var icon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal_splash_screen)

        icon = findViewById(R.id.imageView)
        handler.postDelayed(terminalRunnable, 500)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(terminalRunnable, 500)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(terminalRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(terminalRunnable)
    }

    private val terminalRunnable = Runnable {
        val intent = Intent(this@TerminalLauncher, Term::class.java)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@TerminalLauncher, icon, icon.transitionName)
        startActivity(intent, options.toBundle())
        finish()
    }
}