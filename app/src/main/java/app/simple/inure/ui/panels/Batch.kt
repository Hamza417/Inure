package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginTop
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBatch
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.panels.BatchViewModel

class Batch : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var batchMenuContainer: DynamicCornerLinearLayout
    private lateinit var delete: DynamicRippleImageButton
    private lateinit var send: DynamicRippleImageButton
    private lateinit var extract: DynamicRippleImageButton
    private lateinit var menu: DynamicRippleImageButton

    private val batchViewModel: BatchViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_batch, container, false)

        recyclerView = view.findViewById(R.id.batch_recycler_view)
        batchMenuContainer = view.findViewById(R.id.batch_menu_container)
        delete = view.findViewById(R.id.delete)
        send = view.findViewById(R.id.send)
        extract = view.findViewById(R.id.extract)
        menu = view.findViewById(R.id.menu)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        batchMenuContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.apply {
                    setPadding(paddingLeft,
                               paddingTop,
                               paddingRight,
                               batchMenuContainer.measuredHeight +
                                       batchMenuContainer.marginTop +
                                       paddingBottom)
                }

                batchMenuContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        batchViewModel.getAppData().observe(viewLifecycleOwner) {
            val adapterBatch = AdapterBatch(it)
            recyclerView.adapter = adapterBatch

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        delete.setOnClickListener {

        }

        send.setOnClickListener {

        }

        extract.setOnClickListener {

        }

        menu.setOnClickListener {

        }
    }

    companion object {
        fun newInstance(): Batch {
            val args = Bundle()
            val fragment = Batch()
            fragment.arguments = args
            return fragment
        }
    }
}