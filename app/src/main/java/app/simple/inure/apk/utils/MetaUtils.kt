package app.simple.inure.apk.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import app.simple.inure.R
import app.simple.inure.util.StringUtils

object MetaUtils {
    fun getLaunchMode(mode: Int, context: Context): String {
        return when (mode) {
            ActivityInfo.LAUNCH_MULTIPLE -> context.getString(R.string.multiple)
            ActivityInfo.LAUNCH_SINGLE_INSTANCE -> context.getString(R.string.single_instance)
            ActivityInfo.LAUNCH_SINGLE_TASK -> context.getString(R.string.single_task)
            ActivityInfo.LAUNCH_SINGLE_TOP -> context.getString(R.string.single_top)
            else -> context.getString(R.string.not_available)
        }
    }

    fun getOrientationString(orientation: Int, context: Context): String {
        return when (orientation) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> context.getString(R.string.unspecified)
            ActivityInfo.SCREEN_ORIENTATION_BEHIND -> context.getString(R.string.behind)
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR -> context.getString(R.string.full_sensor)
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER -> context.getString(R.string.full_user)
            ActivityInfo.SCREEN_ORIENTATION_LOCKED -> context.getString(R.string.locked)
            ActivityInfo.SCREEN_ORIENTATION_NOSENSOR -> context.getString(R.string.no_sensor)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> context.getString(R.string.landscape)
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> context.getString(R.string.portrait)
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> context.getString(R.string.reverse_portrait)
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> context.getString(R.string.reverse_landscape)
            ActivityInfo.SCREEN_ORIENTATION_USER -> context.getString(R.string.user)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> context.getString(R.string.sensor_landscape)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT -> context.getString(R.string.sensor_portrait)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR -> context.getString(R.string.sensor)
            ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE -> context.getString(R.string.user_landscape)
            ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT -> context.getString(R.string.user_portrait)
            else -> context.getString(R.string.not_available)
        }
    }

    @Suppress("deprecation")
    fun getSoftInputString(flag: Int): String {
        val builder = StringBuilder()

        if (flag and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING != 0) builder.append("Adjust Nothing, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN != 0) builder.append("Adjust pan, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE != 0) builder.append("Adjust resize, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED != 0) builder.append("Adjust unspecified, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN != 0) builder.append("Always hidden, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE != 0) builder.append("Always visible, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN != 0) builder.append("Hidden, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE != 0) builder.append("Visible, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED != 0) builder.append("Unchanged, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED != 0) builder.append("Unspecified, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION != 0) builder.append("ForwardNav, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST != 0) builder.append("Mask adjust, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE != 0) builder.append("Mask state, ")
        if (flag and WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED != 0) builder.append("Mode changed, ")

        StringUtils.checkStringBuilderEnd(builder)
        val result = builder.toString()
        return if (result == "") "null" else result
    }
}