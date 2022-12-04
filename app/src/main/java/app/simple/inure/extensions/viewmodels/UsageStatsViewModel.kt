package app.simple.inure.extensions.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.net.NetworkCapabilities
import android.os.Build
import android.os.RemoteException
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.IntDef
import androidx.collection.SparseArrayCompat
import app.simple.inure.models.DataUsage
import app.simple.inure.util.PermissionUtils
import app.simple.inure.util.UsageInterval
import dev.rikka.tools.refine.Refine
import java.util.*
import kotlin.collections.ArrayList

open class UsageStatsViewModel(application: Application) : PackageUtilsViewModel(application) {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(value = [NetworkCapabilities.TRANSPORT_CELLULAR, NetworkCapabilities.TRANSPORT_WIFI])
    annotation class Transport

    protected var usageStatsManager: UsageStatsManager = getApplication<Application>()
        .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var networkStatsManager = getApplication<Application>()
        .getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    protected fun getMobileData(@UsageInterval.IntervalType intervalType: Int): SparseArrayCompat<DataUsage> {
        return getDataUsageForNetwork(NetworkCapabilities.TRANSPORT_CELLULAR, intervalType)
    }

    protected fun getWifiData(@UsageInterval.IntervalType intervalType: Int): SparseArrayCompat<DataUsage> {
        return getDataUsageForNetwork(NetworkCapabilities.TRANSPORT_WIFI, intervalType)
    }

    private fun getDataUsageForNetwork(@Transport networkType: Int, @UsageInterval.IntervalType intervalType: Int): SparseArrayCompat<DataUsage> {
        val dataUsageSparseArray = SparseArrayCompat<DataUsage>()
        val range: UsageInterval.UsageInterval = UsageInterval.getTimeInterval(intervalType)
        // val subscriberIds: List<String?> = getSubscriberIds(context, networkType)
        try {
            val bucket = NetworkStats.Bucket()
            networkStatsManager.querySummary(networkType, null, range.startTime, range.endTime).use { networkStats ->
                if (networkStats != null) {
                    while (networkStats.hasNextBucket()) {
                        networkStats.getNextBucket(bucket)
                        var dataUsage = dataUsageSparseArray[bucket.uid]
                        dataUsage = if (dataUsage != null) {
                            DataUsage(bucket.txBytes + dataUsage.tx, bucket.rxBytes + dataUsage.rx)
                        } else {
                            DataUsage(bucket.txBytes, bucket.rxBytes)
                        }
                        dataUsageSparseArray.put(bucket.uid, dataUsage)
                    }
                }
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return dataUsageSparseArray
    }

    /**
     * @return A list of subscriber IDs if networkType is [android.net.NetworkCapabilities.TRANSPORT_CELLULAR], or
     * a singleton array with `null` being the only element.
     */
    @SuppressLint("HardwareIds", "MissingPermission")
    @Deprecated("Requires {@code android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE} from Android 10 (API 29)")
    protected fun getSubscriberIds(@Transport networkType: Int): List<String?> {
        return if (networkType != NetworkCapabilities.TRANSPORT_CELLULAR || !PermissionUtils.hasPermission(applicationContext(), Manifest.permission.READ_PHONE_STATE)) {
            Collections.singletonList(null)
        } else try {
            val subscriptionManager = applicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val telephonyManager = Objects.requireNonNull(applicationContext().getSystemService(Context.TELEPHONY_SERVICE)) as TelephonyManager
            val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList ?: /* No telephony services */ return Collections.singletonList(null)
            val subscriberIds: MutableList<String> = ArrayList()
            for (info in subscriptionInfoList) {
                val subscriptionId = info.subscriptionId
                try {
                    val subscriberId: String = Refine.unsafeCast<TelephonyManager>(telephonyManager).subscriberId
                    subscriberIds.add(subscriberId)
                } catch (e: Exception) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            subscriberIds.add(telephonyManager.createForSubscriptionId(subscriptionId).subscriberId)
                        }
                    } catch (e2: Exception) {
                        subscriberIds.add(telephonyManager.subscriberId)
                    }
                }
            }
            if (subscriberIds.size == 0) Collections.singletonList(null) else subscriberIds
        } catch (e: SecurityException) {
            Collections.singletonList(null)
        }
        // FIXME: 24/4/21 Consider using Binder to fetch subscriber info
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {

    }
}