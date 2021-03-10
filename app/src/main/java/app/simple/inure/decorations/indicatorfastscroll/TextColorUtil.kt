package app.simple.inure.decorations.indicatorfastscroll

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.text.clearSpans

internal object TextColorUtil {

  fun highlightAtIndex(textView: TextView, highlightedIndex: Int?, color: Int) {
    textView.apply {
      if (highlightedIndex == null) {
        clearHighlight(this)
      } else {
        text = SpannableString.valueOf(text).apply {
          clearSpans()
          val linesUpToHighlight = lineSequence().take(highlightedIndex + 1).toList() // inclusive
          val start = linesUpToHighlight
            .dropLast(1)
            .fold(0) { acc, line ->
              acc + line.length + 1
            }
          val highlightedLineSize = linesUpToHighlight.lastOrNull()?.length ?: 0

          setSpan(ForegroundColorSpan(color), start, start + highlightedLineSize, 0)
        }
      }
    }
  }

  fun clearHighlight(textView: TextView) {
    textView.apply {
      if (text is Spanned) {
        text = SpannableString.valueOf(text).apply {
          clearSpans()
        }
      }
    }
  }

}
