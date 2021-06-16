package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterInformation
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.viewers.AppInformationViewModel

class Information : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var progress: ProgressBar

    private lateinit var viewModel: AppInformationViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_information, container, false)

        recyclerView = view.findViewById(R.id.information_data_recycler_view)
        back = view.findViewById(R.id.app_info_back_button)
        progress = view.findViewById(R.id.information_data_progress)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        viewModel = ViewModelProvider(this, applicationInfoFactory).get(AppInformationViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        viewModel.getInformation().observe(viewLifecycleOwner, {
            progress.animate().alpha(0F).setDuration(1000L).start()
            val adapterInformation = AdapterInformation(it)
            recyclerView.adapter = adapterInformation
        })

        viewModel.getProgress().observe(viewLifecycleOwner, {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                progress.setProgress(it, true)
            } else {
                progress.progress = it
            }
        })

        back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Information {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Information()
            fragment.arguments = args
            return fragment
        }
    }
}