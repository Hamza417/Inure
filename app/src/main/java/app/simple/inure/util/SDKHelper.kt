package app.simple.inure.util

import android.content.Context
import android.graphics.Color
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import app.simple.inure.R

object SDKHelper {

    const val totalSDKs = 32

    /**
     * List of all SDK names of Android since the beginning
     *
     * @param sdkCode - code of the sdk used to develop and compile
     *                  the given application
     */
    fun getSdkTitle(@IntRange(from = 1, to = 31) sdkCode: Int): String {
        return when (sdkCode) {
            1 -> "Android 1.0"
            2 -> "Android 1.1"
            3 -> "Cupcake 1.5"
            4 -> "Donut 1.6"
            5 -> "Eclair 2.0"
            6 -> "Eclair 2.0.1"
            7 -> "Eclair 2.1"
            8 -> "Froyo 2.0"
            9 -> "Gingerbread 2.3"
            10 -> "Gingerbread 2.3.3"
            11 -> "Honeycomb 3.0"
            12 -> "Honeycomb 3.1"
            13 -> "Honeycomb 3.2"
            14 -> "Ice Cream Sandwich 4.0"
            15 -> "Ice Cream Sandwich 4.0.3"
            16 -> "Jelly Bean 4.1"
            17 -> "Jelly Bean 4.2"
            18 -> "Jelly Bean 4.3"
            19 -> "KitKat 4.4"
            20 -> "Kitkat 4.4.4"
            21 -> "Lollipop 5.0"
            22 -> "Lollipop 5.1.1"
            23 -> "Marshmallow 6.0"
            24 -> "Nougat 7.0"
            25 -> "Nougat 7.1.2"
            26 -> "Oreo 8.0"
            27 -> "Oreo 8.1"
            28 -> "Pie 9"
            29 -> "Android 10"
            30 -> "Android 11"
            31 -> "Android 12"
            32 -> "Android 13"
            else -> ""
        }
    }

    /**
     * List of all SDK names of Android since the beginning
     *
     * @param sdkCode - code of the sdk used to develop and compile
     *                  the given application
     */
    fun getSdkCode(@IntRange(from = 1, to = 31) sdkCode: Int): String {
        return when (sdkCode) {
            1 -> "1.0"
            2 -> "1.1"
            3 -> "1.5"
            4 -> "1.6"
            5 -> "2.0"
            6 -> "2.0.1"
            7 -> "2.1"
            8 -> "2.0"
            9 -> "2.3"
            10 -> "2.3.3"
            11 -> "3.0"
            12 -> "3.1"
            13 -> "3.2"
            14 -> "4.0"
            15 -> "4.0.3"
            16 -> "4.1"
            17 -> "4.2"
            18 -> "4.3"
            19 -> "4.4"
            20 -> "4.4.4"
            21 -> "5.0"
            22 -> "5.1.1"
            23 -> "6.0"
            24 -> "7.0"
            25 -> "7.1.2"
            26 -> "8.0"
            27 -> "8.1"
            28 -> "9"
            29 -> "10"
            30 -> "11"
            31 -> "12"
            32 -> "13"
            else -> ""
        }
    }

    /**
     * List of all SDK names of Android since the beginning
     *
     * @param sdkCode - code of the sdk used to develop and compile
     *                  the given application
     */
    fun getSdkTitle(sdkCode: String): String {
        return when (sdkCode) {
            "1" -> "Android 1.0"
            "2" -> "Android 1.1"
            "3" -> "Cupcake 1.5"
            "4" -> "Donut 1.6"
            "5" -> "Eclair 2.0"
            "6" -> "Eclair 2.0.1"
            "7" -> "Eclair 2.1"
            "8" -> "Froyo 2.0"
            "9" -> "Gingerbread 2.3"
            "10" -> "Gingerbread 2.3.3"
            "11" -> "Honeycomb 3.0"
            "12" -> "Honeycomb 3.1"
            "13" -> "Honeycomb 3.2"
            "14" -> "Ice Cream Sandwich 4.0"
            "15" -> "Ice Cream Sandwich 4.0.3"
            "16" -> "Jelly Bean 4.1"
            "17" -> "Jelly Bean 4.2"
            "18" -> "Jelly Bean 4.3"
            "19" -> "KitKat 4.4"
            "20" -> "Kitkat 4.4.4"
            "21" -> "Lollipop 5.0"
            "22" -> "Lollipop 5.1.1"
            "23" -> "Marshmallow 6.0"
            "24" -> "Nougat 7.0"
            "25" -> "Nougat 7.1.2"
            "26" -> "Oreo 8.0"
            "27" -> "Oreo 8.1"
            "28" -> "Pie 9"
            "29" -> "Android 10"
            "30" -> "Android 11"
            "31" -> "Android 12"
            "32" -> "Android 13"
            else -> ""
        }
    }

    /**
     * Get color values based on SDK number of the apk file.
     *
     * @param sdkCode - code of the sdk used to develop and compile
     *                  the given application
     */
    fun getSdkColor(sdkCode: Int, context: Context): Int {
        return when (sdkCode) {
            1 -> getColor(context, R.color.android_1)
            2 -> getColor(context, R.color.android_1_1)
            3 -> getColor(context, R.color.cupcake_1_5)
            4 -> getColor(context, R.color.donut_1_6)
            5 -> getColor(context, R.color.eclair_2_0)
            6 -> getColor(context, R.color.eclair_2_0_1)
            7 -> getColor(context, R.color.eclair_2_1)
            8 -> getColor(context, R.color.froyo_2_0)
            9 -> getColor(context, R.color.gingerbread_2_3)
            10 -> getColor(context, R.color.gingerbread_2_3_3)
            11 -> getColor(context, R.color.honeycomb_3_0)
            12 -> getColor(context, R.color.honeycomb_3_1)
            13 -> getColor(context, R.color.honeycomb_3_2)
            14 -> getColor(context, R.color.ice_cream_sandwich_4_0)
            15 -> getColor(context, R.color.ice_cream_sandwich_4_0_3)
            16 -> getColor(context, R.color.jellybean_4_1)
            17 -> getColor(context, R.color.jellybean_4_2)
            18 -> getColor(context, R.color.jellybean_4_3)
            19 -> getColor(context, R.color.kitkat_4_4)
            20 -> getColor(context, R.color.kitkat_4_4_4)
            21 -> getColor(context, R.color.lollipop_5_0)
            22 -> getColor(context, R.color.lollipop_5_1_1)
            23 -> getColor(context, R.color.marshmallow_6_0)
            24 -> getColor(context, R.color.nougat_7_0)
            25 -> getColor(context, R.color.nougat_7_1_2)
            26 -> getColor(context, R.color.oreo_8_0)
            27 -> getColor(context, R.color.oreo_8_1)
            28 -> getColor(context, R.color.pie_9)
            29 -> getColor(context, R.color.android_10)
            30 -> getColor(context, R.color.android_11)
            31 -> getColor(context, R.color.android_12)
            32 -> getColor(context, R.color.android_13)
            else -> Color.WHITE
        }
    }

    private fun getColor(context: Context, id: Int): Int {
        return ContextCompat.getColor(context, id)
    }
}