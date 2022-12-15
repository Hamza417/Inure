package app.simple.inure.apk.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.view.WindowManager
import app.simple.inure.R
import app.simple.inure.util.StringUtils.createString

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

    @Suppress("deprecation", "KotlinConstantConditions")
    fun getSoftInputMode(flag: Int, context: Context): String {
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
            if (type == 0) createString(context.getString(R.string.non_foreground))
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

    fun getFlags(flag: Int, context: Context): String {
        val builder = StringBuilder()

        with(builder) {
            if (flag and ActivityInfo.FLAG_MULTIPROCESS != 0) createString(context.getString(R.string.multi_process))
            if (flag and ActivityInfo.FLAG_FINISH_ON_TASK_LAUNCH != 0) createString(context.getString(R.string.finish_on_task_launch))
            if (flag and ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH != 0) createString(context.getString(R.string.clear_on_task_launch))
            if (flag and ActivityInfo.FLAG_ALWAYS_RETAIN_TASK_STATE != 0) createString(context.getString(R.string.always_retain_task_state))
            if (flag and ActivityInfo.FLAG_STATE_NOT_NEEDED != 0) createString(context.getString(R.string.state_not_needed))
            if (flag and ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS != 0) createString(context.getString(R.string.exclude_from_recents))
            if (flag and ActivityInfo.FLAG_ALLOW_TASK_REPARENTING != 0) createString(context.getString(R.string.allow_task_reparenting))
            if (flag and ActivityInfo.FLAG_NO_HISTORY != 0) createString(context.getString(R.string.no_history))
            if (flag and ActivityInfo.FLAG_FINISH_ON_CLOSE_SYSTEM_DIALOGS != 0) createString(context.getString(R.string.finish_on_close_system_dialogs))
            if (flag and ActivityInfo.FLAG_HARDWARE_ACCELERATED != 0) createString(context.getString(R.string.hardware_accl))
            if (flag and 0x0400 != 0) createString(context.getString(R.string.show_for_all_users))
            if (flag and ActivityInfo.FLAG_IMMERSIVE != 0) createString(context.getString(R.string.immersive))
            if (flag and ActivityInfo.FLAG_RELINQUISH_TASK_IDENTITY != 0) createString(context.getString(R.string.relinquish_task_identity))
            if (flag and ActivityInfo.FLAG_AUTO_REMOVE_FROM_RECENTS != 0) createString(context.getString(R.string.auto_remove_from_recents))
            if (flag and ActivityInfo.FLAG_RESUME_WHILE_PAUSING != 0) createString(context.getString(R.string.resume_while_pausing))
            if (flag and ActivityInfo.FLAG_ENABLE_VR_MODE != 0) createString(context.getString(R.string.vr_mode))
            if (flag and 0x400000 != 0) createString(context.getString(R.string.picture_in_picture))
            if (flag and 0x800000 != 0) createString(context.getString(R.string.show_when_locked))
            if (flag and 0x1000000 != 0) createString(context.getString(R.string.turn_screen_on))
            if (flag and ActivityInfo.FLAG_PREFER_MINIMAL_POST_PROCESSING != 0) createString(context.getString(R.string.prefer_minimal_post_processing))
            if (flag and 0x20000000 != 0) createString(context.getString(R.string.system_user_only))
            if (flag and ActivityInfo.FLAG_SINGLE_USER != 0) createString(context.getString(R.string.single_user))
            if (flag and -0x80000000 != 0) createString(context.getString(R.string.allow_embedded))

            if (isBlank()) {
                append(context.getString(R.string.no_flags))
            }
        }

        return builder.toString()
    }

    fun getCategory(flag: Int, context: Context): String {
        return when (flag) {
            ApplicationInfo.CATEGORY_UNDEFINED -> context.getString(R.string.unspecified)
            ApplicationInfo.CATEGORY_GAME -> context.getString(R.string.game)
            ApplicationInfo.CATEGORY_AUDIO -> context.getString(R.string.audio)
            ApplicationInfo.CATEGORY_VIDEO -> context.getString(R.string.video)
            ApplicationInfo.CATEGORY_IMAGE -> context.getString(R.string.image)
            ApplicationInfo.CATEGORY_SOCIAL -> context.getString(R.string.social)
            ApplicationInfo.CATEGORY_NEWS -> context.getString(R.string.news)
            ApplicationInfo.CATEGORY_MAPS -> context.getString(R.string.maps)
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> context.getString(R.string.productivity)
            ApplicationInfo.CATEGORY_ACCESSIBILITY -> context.getString(R.string.accessibility)
            else -> context.getString(R.string.unspecified)
        }
    }

    fun getConfigurationsChanges(config: Int, context: Context): String {
        val builder = StringBuilder()

        with(builder) {
            if (config and ActivityInfo.CONFIG_MCC != 0) createString("MCC")
            if (config and ActivityInfo.CONFIG_MNC != 0) createString("MNC")
            if (config and ActivityInfo.CONFIG_LOCALE != 0) createString(context.getString(R.string.locale))
            if (config and ActivityInfo.CONFIG_TOUCHSCREEN != 0) createString(context.getString(R.string.touchscreen))
            if (config and ActivityInfo.CONFIG_KEYBOARD != 0) createString(context.getString(R.string.keyboard))
            if (config and ActivityInfo.CONFIG_KEYBOARD_HIDDEN != 0) createString(context.getString(R.string.keyboard_hidden))
            if (config and ActivityInfo.CONFIG_NAVIGATION != 0) createString(context.getString(R.string.navigation))
            if (config and ActivityInfo.CONFIG_ORIENTATION != 0) createString(context.getString(R.string.orientation))
            if (config and ActivityInfo.CONFIG_SCREEN_LAYOUT != 0) createString(context.getString(R.string.screen_layout))
            if (config and ActivityInfo.CONFIG_UI_MODE != 0) createString(context.getString(R.string.ui_mode))
            if (config and ActivityInfo.CONFIG_SCREEN_SIZE != 0) createString(context.getString(R.string.screen_size))
            if (config and ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE != 0) createString(context.getString(R.string.smallest_screen_size))
            if (config and ActivityInfo.CONFIG_DENSITY != 0) createString(context.getString(R.string.density))
            if (config and ActivityInfo.CONFIG_LAYOUT_DIRECTION != 0) createString(context.getString(R.string.layout_direction))
            if (config and ActivityInfo.CONFIG_COLOR_MODE != 0) createString(context.getString(R.string.color_mode))
            if (config and ActivityInfo.CONFIG_FONT_SCALE != 0) createString(context.getString(R.string.font_scale))

            if (isBlank()) {
                append(context.getString(R.string.no_flags))
            }
        }

        return builder.toString()
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
}