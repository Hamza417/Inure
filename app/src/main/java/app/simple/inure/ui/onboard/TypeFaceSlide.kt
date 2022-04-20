package app.simple.inure.ui.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedFragment

class TypeFaceSlide : ScopedFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.onboard_typeface, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

    }

    companion object {
        fun newInstance(): TypeFaceSlide {
            val args = Bundle()
            val fragment = TypeFaceSlide()
            fragment.arguments = args
            return fragment
        }
    }
}