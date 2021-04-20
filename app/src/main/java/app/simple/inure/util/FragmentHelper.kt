package app.simple.inure.util

import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.transition.Fade
import app.simple.inure.R
import app.simple.inure.decorations.transitions.DetailsTransitionArc

object FragmentHelper {
    fun openFragment(fragmentManager: FragmentManager, fragment: Fragment, icon: ImageView, tag: String) {
        fragment.exitTransition = Fade()
        fragment.sharedElementEnterTransition = DetailsTransitionArc(1.5F)
        fragment.enterTransition = Fade()
        fragment.sharedElementReturnTransition = DetailsTransitionArc(1.5F)

        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(icon, icon.transitionName)
                .replace(R.id.app_container, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    fun openFragment(fragmentManager: FragmentManager, fragment: Fragment, tag: String) {
        fragment.exitTransition = Fade()
        fragment.sharedElementEnterTransition = DetailsTransitionArc(1.5F)
        fragment.enterTransition = Fade()
        fragment.sharedElementReturnTransition = DetailsTransitionArc(1.5F)

        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.app_container, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    fun openFragment(fragmentManager: FragmentManager, fragment: Fragment, icon: ImageView) {
        fragment.exitTransition = Fade()
        fragment.sharedElementEnterTransition = DetailsTransitionArc(1.5F)
        fragment.enterTransition = Fade()
        fragment.sharedElementReturnTransition = DetailsTransitionArc(1.5F)

        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(icon, icon.transitionName)
                .replace(R.id.app_container, fragment, null)
                .commit()
    }
}
