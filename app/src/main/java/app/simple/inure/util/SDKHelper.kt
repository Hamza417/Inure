package app.simple.inure.util

import androidx.annotation.IntRange

object SDKHelper {

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
}