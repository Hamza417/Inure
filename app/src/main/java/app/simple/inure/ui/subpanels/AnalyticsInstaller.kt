package app.simple.inure.ui.subpanels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.adapters.analytics.AnalyticsDataAdapter
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.app.AppMenu.Companion.showAppMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.subpanels.AnalyticsViewModelFactory
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.subviewers.AnalyticsInstallerViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyticsInstaller : ScopedFragment() {

    private lateinit var back: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var count: TypeFaceTextView
    private lateinit var loader: CustomProgressBar
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var analyticsInstallerViewModel: AnalyticsInstallerViewModel

    private var pieEntry: PieEntry? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analytics_sdk, container, false)

        back = view.findViewById(R.id.back_button)
        title = view.findViewById(R.id.sdk_name)
        count = view.findViewById(R.id.count)
        loader = view.findViewById(R.id.loader)
        recyclerView = view.findViewById(R.id.recycler_view)

        pieEntry = requireArguments().parcelable(BundleConstants.ENTRY)!!

        val analyticsViewModelFactory = AnalyticsViewModelFactory(pieEntry!!)
        analyticsInstallerViewModel = ViewModelProvider(this, analyticsViewModelFactory)[AnalyticsInstallerViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (analyticsInstallerViewModel.getInstallerApps().value != null) {
            postponeEnterTransition()
        } else {
            startPostponedEnterTransition()
        }

        lifecycleScope.launch(Dispatchers.Default) {
            runCatching {
                val label = requirePackageManager().getPackageInfo(pieEntry!!.label, 0).safeApplicationInfo
                    .loadLabel(requirePackageManager()).toString()

                withContext(Dispatchers.Main) {
                    title.text = label
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    title.text = pieEntry!!.label
                }
            }
        }

        back.setOnClickListener {
            popBackStack()
        }

        analyticsInstallerViewModel.getInstallerApps().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            count.text = getString(R.string.total_apps, it.size)
            val adapter = AnalyticsDataAdapter(it)

            adapter.setOnAdapterCallbacks(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    childFragmentManager.showAppMenu(packageInfo)
                }
            })

            recyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        fun newInstance(e: Entry?): AnalyticsInstaller {
            val args = Bundle()
            args.putParcelable(BundleConstants.ENTRY, e)
            val fragment = AnalyticsInstaller()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "AnalyticsInstaller"
    }
}
