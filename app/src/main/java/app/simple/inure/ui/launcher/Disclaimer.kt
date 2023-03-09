package app.simple.inure.ui.launcher

import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.HtmlHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Disclaimer : ScopedFragment() {

    private lateinit var txt: TypeFaceTextView
    private lateinit var agree: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_disclaimer, container, false)

        txt = view.findViewById(R.id.disclaimer)
        agree = view.findViewById(R.id.agree)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        // Set justifying text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            txt.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            requireContext().assets.open("txt/disclaimer.txt").bufferedReader().use {
                HtmlHelper.fromHtml(it.readText())
            }.let {
                requireActivity().runOnUiThread {
                    txt.text = it
                }
            }
        }

        agree.setOnClickListener {
            MainPreferences.setDisclaimerAgreed(true)
            openFragmentSlide(SplashScreen.newInstance(skip = false))
        }
    }

    companion object {
        fun newInstance(): Disclaimer {
            val args = Bundle()
            val fragment = Disclaimer()
            fragment.arguments = args
            return fragment
        }
    }
}