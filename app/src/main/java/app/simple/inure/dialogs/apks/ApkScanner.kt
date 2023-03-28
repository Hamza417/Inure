package app.simple.inure.dialogs.apks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.viewmodels.panels.ApkBrowserViewModel

class ApkScanner : ScopedBottomSheetFragment() {

    private lateinit var data: TypeFaceTextView
    private lateinit var apkBrowserViewModel: ApkBrowserViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_apk_scanner, container, false)

        data = view.findViewById(R.id.scan_info)

        apkBrowserViewModel = ViewModelProvider(requireActivity())[ApkBrowserViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apkBrowserViewModel.getPathInfo().observe(viewLifecycleOwner) {
            data.text = it
        }
    }

    companion object {
        fun newInstance(): ApkScanner {
            val args = Bundle()
            val fragment = ApkScanner()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showApkScanner(): ApkScanner {
            val fragment = newInstance()
            fragment.show(this, ApkScanner::class.java.simpleName)
            return fragment
        }
    }
}