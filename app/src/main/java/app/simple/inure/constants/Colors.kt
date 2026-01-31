package app.simple.inure.constants

import android.annotation.SuppressLint
import android.graphics.Color
import app.simple.inure.preferences.AccessibilityPreferences

@SuppressLint("UseKtx")
@Suppress("MemberVisibilityCanBePrivate")
object Colors {

    const val PASTEL = 0
    const val RETRO = 1
    const val COFFEE = 2
    const val COLD = 3

    /** Colors for App Ops **/
    const val ALLOW_COLOR = "#1F7A55"
    const val DENY_COLOR = "#C11007"

    private val pastel: ArrayList<Int> by lazy {
        arrayListOf(
                Color.parseColor("#A7727D"),
                Color.parseColor("#6096B4"),
                Color.parseColor("#D0B8A8"),
                Color.parseColor("#8B7E74"),
                Color.parseColor("#7895B2"),
                Color.parseColor("#967E76"),
                Color.parseColor("#7D9D9C"),
                Color.parseColor("#748DA6"),
                Color.parseColor("#576F72"),
                Color.parseColor("#68A7AD"),
                Color.parseColor("#9A86A4"),
                Color.parseColor("#655D8A"),
                Color.parseColor("#7C99AC"),
                Color.parseColor("#8E806A"),
                Color.parseColor("#96C7C1"),
                Color.parseColor("#87AAAA"),
                Color.parseColor("#9D9D9D"),
                Color.parseColor("#89B5AF"),
                Color.parseColor("#F29191"),
                Color.parseColor("#F2C57C"),
                Color.parseColor("#A0937D"),
                Color.parseColor("#798777"),
                Color.parseColor("#B67162"),
                Color.parseColor("#865858"),
                Color.parseColor("#435560"),
                Color.parseColor("#557174"),
                Color.parseColor("#7C9473"),
                Color.parseColor("#6155A6"),
        )
    }

    private val retro: ArrayList<Int> by lazy {
        arrayListOf(
                Color.parseColor("#B31312"),
                Color.parseColor("#2B2A4C"),
                Color.parseColor("#5C8984"),
                Color.parseColor("#545B77"),
                Color.parseColor("#068DA9"),
                Color.parseColor("#A459D1"),
                Color.parseColor("#1B9C85"),
                Color.parseColor("#B71375"),
                Color.parseColor("#3C486B"),
                Color.parseColor("#245953"),
                Color.parseColor("#408E91"),
                Color.parseColor("#4E6E81"),
                Color.parseColor("#EA5455"),
                Color.parseColor("#BAD7E9"),
                Color.parseColor("#609EA2"),
                Color.parseColor("#EB455F"),
                Color.parseColor("#434242"),
                Color.parseColor("#E26868"),
                Color.parseColor("#ADDDD0"),
                Color.parseColor("#FFDE00"),
                Color.parseColor("#7A4495"),
                Color.parseColor("#1A4D2E"),
                Color.parseColor("#4B5D67"),
                Color.parseColor("#413F42"),
                Color.parseColor("#6A67CE"),
                Color.parseColor("#8D8DAA"),
                Color.parseColor("#006E7F"),
                Color.parseColor("#874356"),
        )
    }

    private val cold: ArrayList<Int> by lazy {
        arrayListOf(
                Color.parseColor("#27374D"),
                Color.parseColor("#526D82"),
                Color.parseColor("#11009E"),
                Color.parseColor("#57C5B6"),
                Color.parseColor("#19A7CE"),
                Color.parseColor("#635985"),
                Color.parseColor("#3E54AC"),
                Color.parseColor("#3C84AB"),
                Color.parseColor("#6096B4"),
                Color.parseColor("#497174"),
                Color.parseColor("#557153"),
                Color.parseColor("#6D9886"),
                Color.parseColor("#256D85"),
                Color.parseColor("#395B64"),
                Color.parseColor("#495C83"),
                Color.parseColor("#635666"),
                Color.parseColor("#3BACB6"),
                Color.parseColor("#069A8E"),
                Color.parseColor("#39AEA9"),
                Color.parseColor("#557B83"),
                Color.parseColor("#1C658C"),
                Color.parseColor("#D3DEDC"),
                Color.parseColor("#6998AB"),
                Color.parseColor("#009DAE"),
                Color.parseColor("#678983"),
                Color.parseColor("#E6DDC4"),
                Color.parseColor("#345B63"),
                Color.parseColor("#D4ECDD"),
        )
    }

    private val coffee: ArrayList<Int> by lazy {
        arrayListOf(
                Color.parseColor("#884A39"),
                Color.parseColor("#A9907E"),
                Color.parseColor("#ABC4AA"),
                Color.parseColor("#FFC26F"),
                Color.parseColor("#8D7B68"),
                Color.parseColor("#C8B6A6"),
                Color.parseColor("#A7727D"),
                Color.parseColor("#A75D5D"),
                Color.parseColor("#343434"),
                Color.parseColor("#8B7E74"),
                Color.parseColor("#65647C"),
                Color.parseColor("#D45D79"),
                Color.parseColor("#704F4F"),
                Color.parseColor("#D1512D"),
                Color.parseColor("#3F4E4F"),
                Color.parseColor("#A27B5C"),
                Color.parseColor("#DCD7C9"),
                Color.parseColor("#87805E"),
                Color.parseColor("#AC7D88"),
                Color.parseColor("#826F66"),
                Color.parseColor("#3A3845"),
                Color.parseColor("#54BAB9"),
                Color.parseColor("#8E806A"),
                Color.parseColor("#DBD0C0"),
                Color.parseColor("#4B6587"),
                Color.parseColor("#C8C6C6"),
                Color.parseColor("#7C7575"),
                Color.parseColor("#8E8B82"),
        )
    }

    fun getPastelColor(): ArrayList<Int> {
        return pastel
    }

    fun getRetroColor(): ArrayList<Int> {
        return retro
    }

    fun getColdColor(): ArrayList<Int> {
        return cold
    }

    fun getCoffeeColor(): ArrayList<Int> {
        return coffee
    }

    fun getColors(): ArrayList<Int> {
        return when (AccessibilityPreferences.getColorfulIconsPalette()) {
            COLD -> getColdColor()
            RETRO -> getRetroColor()
            COFFEE -> getCoffeeColor()
            else -> getPastelColor()
        }
    }

    fun getColorsTwice(): ArrayList<Int> {
        val colors = getColors()
        colors.addAll(colors)
        return colors
    }
}