package tw.firemaples.onscreenocr.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

object JsonUtil {
    private val logger: Logger by lazy { Logger(this::class) }

    private val gson = Gson()

    fun <T> fromJsonOrNull(json: String, type: Type): T? =
        try {
            gson.fromJson(json, type)
        } catch (e: JsonSyntaxException) {
            logger.warn(t = e)
            null
        }
}
