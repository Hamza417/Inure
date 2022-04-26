package app.simple.inure.text

import android.text.ParcelableSpan
import android.text.SpannableStringBuilder
import android.text.style.*
import com.google.gson.*
import java.lang.reflect.Type

class SpannableSerializer : JsonSerializer<SpannableStringBuilder?>, JsonDeserializer<SpannableStringBuilder?> {

    private val gson: Gson
        get() {
            val runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(ParcelableSpan::class.java, ParcelableSpan::class.java.simpleName)
                .registerSubtype(ForegroundColorSpan::class.java, ForegroundColorSpan::class.java.simpleName)
                .registerSubtype(BackgroundColorSpan::class.java, BackgroundColorSpan::class.java.simpleName)
                .registerSubtype(StyleSpan::class.java, StyleSpan::class.java.simpleName)
                .registerSubtype(RelativeSizeSpan::class.java, RelativeSizeSpan::class.java.simpleName)
                .registerSubtype(SuperscriptSpan::class.java, SuperscriptSpan::class.java.simpleName)
                .registerSubtype(UnderlineSpan::class.java, UnderlineSpan::class.java.simpleName)
                .registerSubtype(BulletSpan::class.java, BulletSpan::class.simpleName)
                .registerSubtype(SubscriptSpan::class.java, SubscriptSpan::class.simpleName)
                .registerSubtype(StrikethroughSpan::class.java, StrikethroughSpan::class.simpleName)
                .registerSubtype(AbsoluteSizeSpan::class.java, AbsoluteSizeSpan::class.simpleName)
                .registerSubtype(SuggestionSpan::class.java, SuggestionSpan::class.simpleName)
                .registerSubtype(EasyEditSpan::class.java, EasyEditSpan::class.simpleName)
                .registerSubtype(URLSpan::class.java, URLSpan::class.simpleName)
                .registerSubtype(QuoteSpan::class.java, QuoteSpan::class.simpleName)

            /**
             * Cannot be serialized since [MaskFilterSpan] does not implement
             * [ParcelableSpan] class interface
             */
            // .registerSubtype(MaskFilterSpan::class.java, MaskFilterSpan::class.simpleName)

            return GsonBuilder()
                .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
                .create()
        }

    override fun serialize(src: SpannableStringBuilder?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val spanTypes = src?.getSpans(0, src.length, ParcelableSpan::class.java)
        val spanStart = IntArray(spanTypes?.size ?: 0)
        val spanEnd = IntArray(spanTypes?.size ?: 0)
        val spanFlags = IntArray(spanTypes?.size ?: 0)
        val spanInfo = DoubleArray(spanTypes?.size ?: 0)

        spanTypes?.forEachIndexed { i, span ->
            when (span) {
                is ForegroundColorSpan -> spanInfo[i] = span.foregroundColor.toDouble()
                is StyleSpan -> spanInfo[i] = span.style.toDouble()
                is RelativeSizeSpan -> spanInfo[i] = span.sizeChange.toDouble()
            }
            spanStart[i] = src.getSpanStart(span)
            spanEnd[i] = src.getSpanEnd(span)
            spanFlags[i] = src.getSpanFlags(span)
        }

        val jsonSpannable = JsonObject()
        jsonSpannable.addProperty(INPUT_STRING, src.toString())
        jsonSpannable.addProperty(SPAN_TYPES, gson.toJson(spanTypes))
        jsonSpannable.addProperty(SPAN_START, gson.toJson(spanStart))
        jsonSpannable.addProperty(SPAN_END, gson.toJson(spanEnd))
        jsonSpannable.addProperty(SPAN_FLAGS, gson.toJson(spanFlags))
        jsonSpannable.addProperty(SPAN_INFO, gson.toJson(spanInfo))
        return jsonSpannable
    }

    override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): SpannableStringBuilder {
        val jsonSpannable = jsonElement.asJsonObject
        val spannableString = jsonSpannable[INPUT_STRING].asString
        val spannableStringBuilder = SpannableStringBuilder(spannableString)
        val spanObjectJson = jsonSpannable[SPAN_TYPES].asString
        val spanTypes: Array<ParcelableSpan> = gson.fromJson(spanObjectJson, Array<ParcelableSpan>::class.java)
        val spanStartJson = jsonSpannable[SPAN_START].asString
        val spanStart: IntArray = gson.fromJson(spanStartJson, IntArray::class.java)
        val spanEndJson = jsonSpannable[SPAN_END].asString
        val spanEnd: IntArray = gson.fromJson(spanEndJson, IntArray::class.java)
        val spanFlagsJson = jsonSpannable[SPAN_FLAGS].asString
        val spanFlags: IntArray = gson.fromJson(spanFlagsJson, IntArray::class.java)
        val spanInfoJson = jsonSpannable[SPAN_INFO].asString
        val spanInfo: DoubleArray = gson.fromJson(spanInfoJson, DoubleArray::class.java)

        for (i in spanTypes.indices) {
            when (spanTypes[i]) {
                is ForegroundColorSpan -> spannableStringBuilder.setSpan(ForegroundColorSpan(spanInfo[i].toInt()), spanStart[i], spanEnd[i], spanFlags[i])
                is StyleSpan -> spannableStringBuilder.setSpan(StyleSpan(spanInfo[i].toInt()), spanStart[i], spanEnd[i], spanFlags[i])
                is RelativeSizeSpan -> spannableStringBuilder.setSpan(RelativeSizeSpan(spanInfo[i].toFloat()), spanStart[i], spanEnd[i], spanFlags[i])
                else -> spannableStringBuilder.setSpan(spanTypes[i], spanStart[i], spanEnd[i], spanFlags[i])
            }
        }

        return spannableStringBuilder
    }

    companion object {
        private const val PREFIX = "SSB:"
        private const val INPUT_STRING = PREFIX + "string"
        private const val SPAN_TYPES = PREFIX + "spanTypes"
        private const val SPAN_START = PREFIX + "spanStart"
        private const val SPAN_END = PREFIX + "spanEnd"
        private const val SPAN_FLAGS = PREFIX + "spanFlags"
        private const val SPAN_INFO = PREFIX + "spanInfo"
    }
}