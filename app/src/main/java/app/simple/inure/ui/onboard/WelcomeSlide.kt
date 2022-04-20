package app.simple.inure.ui.onboard

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.OnBoardingPreferences
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.visible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeSlide : ScopedFragment() {

    private lateinit var icon: ImageView
    private lateinit var message: TypeFaceTextView
    private lateinit var next: DynamicRippleImageButton
    private lateinit var skip: DynamicRippleImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.onboard_welcome, container, false)

        icon = view.findViewById(R.id.imageView)
        message = view.findViewById(R.id.welcome)
        next = view.findViewById(R.id.next)
        skip = view.findViewById(R.id.previous)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        (icon.drawable as AnimatedVectorDrawable).start()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            message.visible(true)
        }

        next.setOnClickListener {
            OnBoardingPreferences.setWelcomeState(true)
            clearExitTransition()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        PermissionsSlide.newInstance(),
                                        null)
        }

        skip.setOnClickListener {
            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                              SplashScreen.newInstance(true), icon, null, 500)
        }
    }

    companion object {
        fun newInstance(): WelcomeSlide {
            val args = Bundle()
            val fragment = WelcomeSlide()
            fragment.arguments = args
            return fragment
        }
    }
}