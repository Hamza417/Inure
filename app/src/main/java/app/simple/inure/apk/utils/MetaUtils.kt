package app.simple.inure.apk.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import app.simple.inure.R

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

    fun getOrientation(orientation: Int, context: Context): String {
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
            else -> context.getString(R.string.unspecified)
        }
    }

    @Suppress("deprecation")
    fun getSoftInputString(flag: Int, context: Context): String {
        val builder = StringBuilder()

        with(builder) {
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING != 0) createString(context.getString(R.string.adjust_nothing))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN != 0) createString(context.getString(R.string.adjust_pan))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE != 0) createString(context.getString(R.string.adjust_resize))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED != 0) createString(context.getString(R.string.adjust_unspecified))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN != 0) createString(context.getString(R.string.always_hidden))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE != 0) createString(context.getString(R.string.always_visible))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN != 0) createString(context.getString(R.string.hidden))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE != 0) createString(context.getString(R.string.visible))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED != 0) createString(context.getString(R.string.unchanged))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED != 0) createString(context.getString(R.string.unspecified))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION != 0) createString(context.getString(R.string.forward_navigation))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST != 0) createString(context.getString(R.string.mask_adjust))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE != 0) createString(context.getString(R.string.mask_state))
            if (flag and WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED != 0) createString(context.getString(R.string.mode_changed))

            if (isBlank()) {
                append(context.getString(R.string.unspecified))
            }
        }

        return builder.toString()
    }

    fun getForegroundServiceType(type: Int, context: Context): String {
        val builder = StringBuilder()

        with(builder) {
            if ((0 and type) == 0) createString(context.getString(R.string.non_foreground))
            if ((1 shl 0 and type) == 1 shl 0) createString(context.getString(R.string.data_sync))
            if ((1 shl 1 and type) == 1 shl 1) createString(context.getString(R.string.media_playback))
            if ((1 shl 2 and type) == 1 shl 2) createString(context.getString(R.string.phone_call))
            if ((1 shl 3 and type) == 1 shl 3) createString(context.getString(R.string.location))
            if ((1 shl 4 and type) == 1 shl 4) createString(context.getString(R.string.connected_devices))
            if ((1 shl 5 and type) == 1 shl 5) createString(context.getString(R.string.media_projection))
            if ((1 shl 6 and type) == 1 shl 6) createString(context.getString(R.string.camera))
            if ((1 shl 7 and type) == 1 shl 7) createString(context.getString(R.string.microphone))
            if ((1 shl -1 and type) == 1 shl -1) createString(context.getString(R.string.manifest))
        }

        return builder.toString()
    }

    fun getServiceFlags(type: Int, context: Context): String {
        val builder = StringBuilder()

        with(builder) {
            if (type and 0x0001 != 0) createString(context.getString(R.string.stop_with_task))
            if (type and 0x0002 != 0) createString(context.getString(R.string.isolated_process))
            if (type and 0x0004 != 0) createString(context.getString(R.string.external_service))
            if (type and 0x0008 != 0) createString(context.getString(R.string.app_zygote))
            if (type and 0x10000 != 0) createString(context.getString(R.string.visible_to_instant_apps))
            if (type and 0x40000000 != 0) createString(context.getString(R.string.single_user))

            if (isBlank()) {
                append(context.getString(R.string.no_flags))
            }
        }

        return builder.toString()
    }

    fun getColorMode(mode: Int, context: Context): String {
        return when (mode) {
            -1 -> context.getString(R.string.not_supported)
            ActivityInfo.COLOR_MODE_DEFAULT -> context.getString(R.string.default_, "(6 or 8 Bit)")
            ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT -> context.getString(R.string.wide_color_gamut, "(10 bit)")
            ActivityInfo.COLOR_MODE_HDR -> "HDR"
            else -> context.getString(R.string.unspecified)
        }
    }

    fun getDocumentLaunchMode(mode: Int, context: Context): String {
        return when (mode) {
            ActivityInfo.DOCUMENT_LAUNCH_NONE -> context.getString(R.string.none)
            ActivityInfo.DOCUMENT_LAUNCH_ALWAYS -> context.getString(R.string.always)
            ActivityInfo.DOCUMENT_LAUNCH_INTO_EXISTING -> context.getString(R.string.into_existing)
            ActivityInfo.DOCUMENT_LAUNCH_NEVER -> context.getString(R.string.never)
            else -> context.getString(R.string.unspecified)
        }
    }

    fun getPersistableMode(mode: Int, context: Context): String {
        return when (mode) {
            ActivityInfo.PERSIST_NEVER -> context.getString(R.string.never)
            ActivityInfo.PERSIST_ROOT_ONLY -> context.getString(R.string.root_only)
            ActivityInfo.PERSIST_ACROSS_REBOOTS -> context.getString(R.string.across_reboots)
            else -> context.getString(R.string.unspecified)
        }
    }

    fun getOpenGL(reqGL: Int): String {
        val builder = StringBuilder()

        with(builder) {
            append("OpenGL ES ")

            if (reqGL != 0) {
                append((reqGL shr 16).toShort().toString() + "." + reqGL.toShort()) //Integer.toString((reqGL & 0xffff0000) >> 16);
            } else {
                append("1") // Lack of property means OpenGL ES version 1
            }
        }

        return builder.toString()
    }

    private fun StringBuilder.createString(string: String) {
        if (isNotEmpty()) {
            append(" | $string")
        } else {
            append(string)
        }
    }
}