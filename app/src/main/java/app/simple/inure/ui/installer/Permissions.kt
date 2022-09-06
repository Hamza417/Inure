package app.simple.inure.ui.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.adapters.installer.AdapterInstallerPermissions
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.dongliu.apk.parser.ApkFile
import java.io.File

class Permissions : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var file: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_permissions, container, false)

        recyclerView = view.findViewById(R.id.permissions_recycler_view)
        file = requireArguments().getSerializable(BundleConstants.file) as File?

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val permissions = ApkFile(file).use {
                    it.apkMeta.usesPermissions
                }

                withContext(Dispatchers.Main) {
                    recyclerView.adapter = AdapterInstallerPermissions(permissions)
                }
            }.getOrElse {
                it.printStackTrace()
            }
        }
    }

    companion object {
        fun newInstance(file: File): Permissions {
            val args = Bundle()
            args.putSerializable(BundleConstants.file, file)
            val fragment = Permissions()
            fragment.arguments = args
            return fragment
        }
    }
}