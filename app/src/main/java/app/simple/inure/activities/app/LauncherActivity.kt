package app.simple.inure.activities.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import app.simple.inure.R
import app.simple.inure.ui.launcher.SplashScreen

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.app_container, SplashScreen.newInstance()).commit()

        Handler(Looper.getMainLooper()).postDelayed({
                                  val intent = Intent(this, MainActivity::class.java)
                                  startActivity(intent)
                              }, 1000L)
    }
}
