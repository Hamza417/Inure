package app.simple.inure.util

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable

@Suppress("unused")
object ParcelUtils {
    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    inline fun <reified T : java.io.Serializable> Bundle.serializable(key: String): T? = when {
        SDK_INT >= 33 -> getSerializable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializable(key) as? T
    }

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String, default: T): T = when {
        SDK_INT >= 33 -> getParcelable(key, T::class.java) ?: default
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T ?: default
    }

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String, default: () -> T): T = when {
        SDK_INT >= 33 -> getParcelable(key, T::class.java) ?: default()
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T ?: default()
    }

    inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
    }

    inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
    }
}