package ru.yanus171.feedexfork.utils

import org.json.JSONObject
import ru.yanus171.feedexfork.MainApplication
import ru.yanus171.feedexfork.provider.FeedData.FeedColumns

// 白い熊: the per-feed "Request pictures as JPEG" switch. Image servers that take the output format
// in the address (imgix-style "fm=png") can serve the same picture as a JPEG at a fraction of the
// size — Deník N's og:image is a 2400×1260 PNG of ~3 MB, above the 2048 KB download limit, so the
// article got no thumbnail. With the switch on, such addresses ask for "fm=jpg" instead.
object FeedImageFormat {
    const val REQUEST_JPEG = "REQUEST_JPEG"

    // "fm=png" / "fm=webp" / "fm=avif" as a query parameter, in a plain URL or inside HTML ("&amp;").
    private val FORMAT_PARAM = Regex("""((?:[?&]|&amp;)fm=)(?:png|webp|avif)(?=[&"'\s#]|$)""", RegexOption.IGNORE_CASE)

    @JvmStatic
    fun isOn(feedId: String?): Boolean {
        if (feedId.isNullOrEmpty())
            return false
        return try {
            MainApplication.getContext().contentResolver.query(
                FeedColumns.CONTENT_URI(feedId), arrayOf(FeedColumns.OPTIONS), null, null, null
            )?.use { c ->
                if (!c.moveToFirst() || c.isNull(0)) false
                else JSONObject(c.getString(0)).optBoolean(REQUEST_JPEG, false)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    @JvmStatic
    fun toJpeg(text: String?): String? = text?.replace(FORMAT_PARAM, "$1jpg")
}
