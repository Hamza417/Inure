package app.simple.inure.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.adapters.dialogs.AdapterPathPicker
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import java.io.File

class PathPicker : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var select: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var path: TypeFaceTextView

    private lateinit var adapter: AdapterPathPicker
    private var backPressed: OnBackPressedDispatcher? = null

    private var currentPath = "/storage/emulated/0"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_path_picker, container, false)

        recyclerView = view.findViewById(R.id.path_recycler_view)
        select = view.findViewById(R.id.select)
        close = view.findViewById(R.id.close)
        path = view.findViewById(R.id.current_path)

        adapter = AdapterPathPicker()
        backPressed = requireActivity().onBackPressedDispatcher

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter
        adapter.paths = getCurrentPathDirectories(currentPath)
        path.text = currentPath

        adapter.onPathSelected = {
            currentPath += "/$it"
            path.text = currentPath
            adapter.paths = getCurrentPathDirectories(currentPath)
            setupBackPressed()
        }
    }

    private fun getCurrentPathDirectories(path: String): ArrayList<String> {
        return File(path).listDirectories()
    }

    private fun File.listDirectories(): ArrayList<String> {
        val directories = ArrayList<String>()
        listFiles()?.forEach {
            if (it.isDirectory) {
                directories.add(it.name)
            }
        }
        return directories
    }

    private fun setupBackPressed() {
        backPressed?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentPath.count { it == '/' } == 1) {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    currentPath = currentPath.substringBeforeLast("/")
                    path.text = currentPath
                    adapter.paths = getCurrentPathDirectories(currentPath)
                }
            }
        })
    }

    companion object {
        private const val TAG = "PathPicker"

        fun newInstance(): PathPicker {
            val args = Bundle()
            val fragment = PathPicker()
            fragment.arguments = args
            return fragment
        }

        fun AppCompatActivity.showPathPicker() {
            supportFragmentManager.let {
                newInstance().apply {
                    show(it, TAG)
                }
            }
        }

        fun FragmentManager.showPathPicker() {
            newInstance().apply {
                show(this@showPathPicker, TAG)
            }
        }
    }
}