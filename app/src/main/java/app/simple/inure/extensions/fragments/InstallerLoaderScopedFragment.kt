package app.simple.inure.extensions.fragments

import android.os.Bundle
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.theme.ThemeLinearProgressIndicator
import app.simple.inure.interfaces.fragments.InstallerCallbacks

abstract class InstallerLoaderScopedFragment : ScopedFragment() {

    private lateinit var loader: ThemeLinearProgressIndicator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = view.findViewById(R.id.loader)
        startPostponedEnterTransition()
        loader.show()
        (parentFragment as InstallerCallbacks).onLoadingStarted()
    }

    fun onLoadingFinished() {
        (parentFragment as InstallerCallbacks).onLoadingFinished()
        loader.hide()
    }
}
