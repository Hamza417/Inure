package app.simple.inure

import android.app.Application
import app.simple.inure.singletons.ApplicationUtils

class InureApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApplicationUtils.setApplication(this)
    }
}