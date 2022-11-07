package app.simple.inure.util

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import app.simple.inure.preferences.AppearancePreferences
import java.util.regex.Matcher
import java.util.regex.Pattern

object JavaSyntaxUtils {

    private val quotations: Pattern = Pattern.compile("\"([^\"]*)\"",
                                                      Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

    private val javaKeywords: Pattern = Pattern
        .compile("abstract|continue|for|new|switch|assert|default|goto|package" +
                         "|synchronized|boolean|do|if|private|this|break|double|implements" +
                         "|protected|throw|byte|else|import|public|throws|case|enum|instanceof" +
                         "|return|transient|catch|extends|short|try|char|final|interface|int" +
                         "|static|void|class|finally|long|strictfp|volatile|const|float|native" +
                         "|super|while|String", Pattern.CASE_INSENSITIVE)

    private val comments: Pattern = Pattern
        .compile("//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/")

    private val parametersAndVariables: Pattern = Pattern
        .compile("([a-zA-Z][a-zA-Z0-9_\$]+)\\s*\\(\\s*([a-zA-Z0-9_\$]+(?:\\s*,\\s*[a-zA-Z0-9_\$]+)*)\\s*\\)" +
                         "|^[a-zA-Z_\$][a-zA-Z_\$0-9]*\$")

    private val symbols: Pattern = Pattern
        .compile("^[0-9,;]+$")

    fun String.highlightJava(): SpannableString {
        val formatted = SpannableString(this)

        val matcher: Matcher = javaKeywords.matcher(this)
        while (matcher.find()) {
            formatted.setSpan(ForegroundColorSpan(AppearancePreferences.getAccentColor()), matcher.start(),
                              matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        matcher.usePattern(comments)
        while (matcher.find()) {
            formatted.setSpan(ForegroundColorSpan(Color.GRAY), matcher.start(),
                              matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        matcher.usePattern(quotations)
        while (matcher.find()) {
            formatted.setSpan(ForegroundColorSpan(Color.parseColor("#45b39d")), matcher.start(),
                              matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        matcher.usePattern(parametersAndVariables)
        while (matcher.find()) {
            formatted.setSpan(ForegroundColorSpan(Color.parseColor("#2980B9")), matcher.start(),
                              matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        matcher.usePattern(symbols)
        while (matcher.find()) {
            formatted.setSpan(ForegroundColorSpan(Color.parseColor("#2980B9")), matcher.start(),
                              matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return formatted
    }
}