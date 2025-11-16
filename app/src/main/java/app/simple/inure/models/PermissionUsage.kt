package app.simple.inure.models

import android.content.pm.PackageInfo
import android.os.Parcel
import android.os.Parcelable

/**
 * Model for real-time permission usage monitoring across all apps
 * Unlike AppOp which is per-app, this tracks system-wide permission access
 */
data class PermissionUsage(
    val packageInfo: PackageInfo,
    val permission: String,
    val permissionId: String?,
    val isEnabled: Boolean,
    val lastAccessTime: Long,
    val duration: String?,
    val rejectTime: String?,
    val isActive: Boolean // Currently being used right now
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(PackageInfo::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(packageInfo, flags)
        parcel.writeString(permission)
        parcel.writeString(permissionId)
        parcel.writeByte(if (isEnabled) 1 else 0)
        parcel.writeLong(lastAccessTime)
        parcel.writeString(duration)
        parcel.writeString(rejectTime)
        parcel.writeByte(if (isActive) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PermissionUsage> {
        override fun createFromParcel(parcel: Parcel): PermissionUsage {
            return PermissionUsage(parcel)
        }

        override fun newArray(size: Int): Array<PermissionUsage?> {
            return arrayOfNulls(size)
        }

        /**
         * Permissions that are considered "sensitive" and should be monitored
         */
        val MONITORED_PERMISSIONS = setOf(
            "COARSE_LOCATION",
            "FINE_LOCATION",
            "CAMERA",
            "RECORD_AUDIO",
            "READ_PHONE_STATE",
            "READ_CONTACTS",
            "READ_SMS",
            "SEND_SMS",
            "CALL_PHONE",
            "READ_CALL_LOG",
            "WRITE_CALL_LOG",
            "READ_CALENDAR",
            "WRITE_CALENDAR",
            "BODY_SENSORS",
            "ACCESS_MEDIA_LOCATION",
            "READ_CLIPBOARD",
            "WRITE_CLIPBOARD",
            "SYSTEM_ALERT_WINDOW",
            "WRITE_SETTINGS"
        )

        /**
         * Time threshold in milliseconds to consider a permission as "recently used"
         * Default: 5 minutes
         */
        const val RECENT_USAGE_THRESHOLD = 5 * 60 * 1000L
    }
}
