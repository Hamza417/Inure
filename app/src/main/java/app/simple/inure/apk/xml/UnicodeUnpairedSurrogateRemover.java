package app.simple.inure.apk.xml;

import java.io.IOException;
import java.io.Writer;

/**
 * Helper subclass to CharSequenceTranslator to remove unpaired surrogates.
 */
class UnicodeUnpairedSurrogateRemover extends CodePointTranslator {
    /**
     * Implementation of translate that throws out unpaired surrogates.
     * {@inheritDoc}
     */
    @Override
    public boolean translate(int codepoint, Writer out) throws IOException {
        // True = It's a surrogate. Write nothing and say we've translated.
        // False = It's not a surrogate. Don't translate it.
        return codepoint >= Character.MIN_SURROGATE && codepoint <= Character.MAX_SURROGATE;
    }
}
