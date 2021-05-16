package app.simple.inure.dialogs.miscellaneous

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.FileUtils
import app.simple.inure.util.FileUtils.copyTo
import app.simple.inure.util.NullSafety.isNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLConnection

class Preparing : ScopedBottomSheetFragment() {

    private lateinit var loader: ImageView
    private lateinit var updates: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_send_prepare, container, false)

        loader = view.findViewById(R.id.preparing_loader_indicator)
        updates = view.findViewById(R.id.preparing_updates)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.loader))

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                runCatching {
                    val file: File?

                    File(requireContext().getExternalFilesDir(null)!!.path + "/send_cache/").mkdir()

                    postUpdate(getString(R.string.cache_dir))

                    if (applicationInfo.splitSourceDirs.isNotNull()) {

                        postUpdate(getString(R.string.split_apk_detected))

                        file = File(requireContext().getExternalFilesDir(null)!!.path + "/send_cache/" + applicationInfo.name + ".zip")

                        if (!file.exists()) {

                            postUpdate(getString(R.string.creating_split_package))

                            val list = arrayOfNulls<String>(applicationInfo.splitSourceDirs.size)

                            for (i in applicationInfo.splitSourceDirs.indices) {
                                list[i] = applicationInfo.splitSourceDirs[i]
                                println(applicationInfo.splitSourceDirs[i])
                            }

                            list[list.size - 1] = applicationInfo.sourceDir
                            FileUtils.createZip(list.requireNoNulls(), file)
                        }

                    } else {
                        postUpdate(getString(R.string.preparing_apk_file))
                        file = File(requireContext().getExternalFilesDir(null)!!.path + "/send_cache/" + applicationInfo.name + ".apk")

                        if (!file.exists()) {
                            applicationInfo.sourceDir.copyTo(file)
                        }
                    }

                    postUpdate(getString(R.string.done))

                    ShareCompat.IntentBuilder.from(requireActivity())
                            .setStream(FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", file))
                            .setType(URLConnection.guessContentTypeFromName(file.name))
                            .startChooser()

                    dismiss()
                }.getOrElse { e ->
                    e.printStackTrace()
                    dismiss()
                }
            }
        }
    }

    private fun postUpdate(update: String) {
        launch {
            withContext(Dispatchers.Main) {
                updates.text = update
            }
        }
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Preparing {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Preparing()
            fragment.arguments = args
            return fragment
        }
    }
}