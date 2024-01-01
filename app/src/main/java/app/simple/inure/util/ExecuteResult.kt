package app.simple.inure.util

import android.os.Parcel
import android.os.Parcelable

class ExecuteResult(val exitCode: Int, val error: String?, val output: String?) : Parcelable {
    val isSuccess get() = exitCode == 0

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString())

    override fun toString(): String {
        return "ExecuteResult(exitCode=$exitCode, error='$error', output='$output')"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(exitCode)
        parcel.writeString(error)
        parcel.writeString(output)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExecuteResult> {
        override fun createFromParcel(parcel: Parcel): ExecuteResult {
            return ExecuteResult(parcel)
        }

        override fun newArray(size: Int): Array<ExecuteResult?> {
            return arrayOfNulls(size)
        }
    }
}