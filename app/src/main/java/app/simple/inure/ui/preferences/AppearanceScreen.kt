package app.simple.inure.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.simple.inure.R
import app.simple.inure.decorations.animatedbackground.AnimatedBackgroundConstraintLayout
import app.simple.inure.dialogs.AppearanceTypeFace

class AppearanceScreen : Fragment() {

    private lateinit var typeface: AnimatedBackgroundConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_appearances, container, false)

        typeface = view.findViewById(R.id.appearance_app_typeface)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        typeface.setOnClickListener {
            AppearanceTypeFace.newInstance().show(childFragmentManager, "appearance_type_face")
        }
    }

    companion object {
        fun newInstance(): AppearanceScreen {
            val args = Bundle()
            val fragment = AppearanceScreen()
            fragment.arguments = args
            return fragment
        }
    }
}