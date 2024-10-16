package app.simple.inure.extensions.fragments

import android.os.Bundle
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.interfaces.fragments.InstallerCallbacks
import app.simple.inure.util.ViewUtils.slideUpGone
import app.simple.inure.util.ViewUtils.visible

abstract class InstallerLoaderScopedFragment : ScopedFragment() {

    private lateinit var loader: LoaderImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = view.findViewById(R.id.loader)
        startPostponedEnterTransition()
        loader.start()
        loader.visible(animate = true)
        (parentFragment as InstallerCallbacks).onLoadingStarted()
    }

    fun onLoadingFinished() {
        (parentFragment as InstallerCallbacks).onLoadingFinished()
        loader.loaded()
        loader.slideUpGone()
    }
}
