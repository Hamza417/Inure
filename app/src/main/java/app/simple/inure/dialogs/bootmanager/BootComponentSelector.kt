package app.simple.inure.dialogs.bootmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.adapters.dialogs.AdapterBootSelector
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.BootManagerModel
import app.simple.inure.util.ParcelUtils.parcelable

class BootComponentSelector : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var enable: DynamicRippleTextView
    private lateinit var disable: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    private val selectedSet: MutableSet<String> = mutableSetOf()
    private val mutableList: MutableList<Pair<String, Boolean>> by lazy {
        requireArguments().parcelable<BootManagerModel>(BundleConstants.bootManagerModel)!!.let {
            val list = mutableListOf<Pair<String, Boolean>>()

            for (component in it.enabledComponents) {
                list.add(Pair(component, true))
            }

            for (component in it.disabledComponents) {
                list.add(Pair(component, false))
            }

            list
        }
    }

    private var adapterBootSelector: AdapterBootSelector? = null
    private var bootComponentSelectorCallbacks: BootComponentSelectorCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_selector_boot_component, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        enable = view.findViewById(R.id.enable)
        disable = view.findViewById(R.id.disable)
        close = view.findViewById(R.id.close)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterBootSelector = AdapterBootSelector(mutableList)
        selectedSet.addAll(mutableList.filter { it.second }.map { it.first })

        adapterBootSelector?.setBootSelectorCallbacks(object : AdapterBootSelector.Companion.BootSelectorCallbacks {
            override fun onBootSelected(path: String, isChecked: Boolean) {
                if (isChecked) {
                    selectedSet.add(path)
                } else {
                    selectedSet.remove(path)
                }
            }
        })

        recyclerView.adapter = adapterBootSelector

        enable.setOnClickListener {
            bootComponentSelectorCallbacks?.onBootSelected(selectedSet, true)
            dismiss()
        }

        disable.setOnClickListener {
            bootComponentSelectorCallbacks?.onBootSelected(selectedSet, false)
            dismiss()
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    fun setBootComponentSelectorCallbacks(bootComponentSelectorCallbacks: BootComponentSelectorCallbacks) {
        this.bootComponentSelectorCallbacks = bootComponentSelectorCallbacks
    }

    companion object {
        fun newInstance(bootManagerModel: BootManagerModel): BootComponentSelector {
            val args = Bundle()
            args.putParcelable(BundleConstants.bootManagerModel, bootManagerModel)
            val fragment = BootComponentSelector()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBootComponentSelector(bootManagerModel: BootManagerModel): BootComponentSelector {
            val dialog = newInstance(bootManagerModel)
            dialog.show(this, "boot_component_selector")
            return dialog
        }

        interface BootComponentSelectorCallbacks {
            fun onBootSelected(selectedSet: Set<String>, enable: Boolean)
        }
    }
}