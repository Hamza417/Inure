package app.simple.inure.dialogs.app

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.HtmlHelper

class AppMemory : ScopedBottomSheetFragment() {

    private lateinit var memory: TypeFaceTextView

    private var activityManager: ActivityManager? = null
    private val stringBuilder = StringBuilder()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_app_memory, container, false)

        memory = view.findViewById(R.id.memory)
        activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getHeap()
        getLowRamDeviceState()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getBackgroundRestrictedState()
        }

        memory.text = HtmlHelper.fromHtml(stringBuilder.toString())
    }

    private fun getHeap() {
        stringBuilder.append("<b>Heap Size: </b>")
        stringBuilder.append("${activityManager?.largeMemoryClass} MB")
    }

    private fun getLowRamDeviceState() {
        stringBuilder.append("<br>")
        stringBuilder.append("<b>Low Ram Device: </b>")

        if (activityManager?.isLowRamDevice == true) {
            stringBuilder.append(getString(R.string.yes))
        } else {
            stringBuilder.append(getString(R.string.no))
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getBackgroundRestrictedState() {
        stringBuilder.append("<br>")
        stringBuilder.append("<b>Background Restricted: </b>")

        if (activityManager?.isBackgroundRestricted == true) {
            stringBuilder.append(getString(R.string.yes))
        } else {
            stringBuilder.append(getString(R.string.no))
        }
    }

    companion object {
        fun newInstance(): AppMemory {
            val args = Bundle()
            val fragment = AppMemory()
            fragment.arguments = args
            return fragment
        }
    }
}