package app.simple.inure.dialogs.app

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.HtmlHelper

class AppMemory : ScopedBottomSheetFragment() {

    private lateinit var heap: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_app_memory, container, false)

        heap = view.findViewById(R.id.heap_size)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        heap.text = HtmlHelper.fromHtml("<b>Heap Size:</b> ${activityManager.largeMemoryClass} MB")
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