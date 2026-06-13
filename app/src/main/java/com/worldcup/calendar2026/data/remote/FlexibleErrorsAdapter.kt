package com.worldcup.calendar2026.data.remote

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Types
import java.lang.reflect.Type

/**
 * API-Football returns `"errors": []` (empty array) when there are no errors, but
 * `"errors": {"token": "..."}` (an object) when there are errors. This adapter
 * handles both forms, mapping either to a `Map<String, String>`.
 */
class FlexibleErrorsAdapter : JsonAdapter<Map<String, String>>() {

    override fun fromJson(reader: JsonReader): Map<String, String> {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_ARRAY -> {
                // Empty-array form — no errors
                reader.beginArray()
                while (reader.hasNext()) reader.skipValue()
                reader.endArray()
                emptyMap()
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                val map = mutableMapOf<String, String>()
                reader.beginObject()
                while (reader.hasNext()) {
                    map[reader.nextName()] = reader.nextString()
                }
                reader.endObject()
                map
            }
            else -> {
                reader.skipValue()
                emptyMap()
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: Map<String, String>?) {
        if (value == null || value.isEmpty()) {
            writer.beginArray().endArray()
        } else {
            writer.beginObject()
            value.forEach { (k, v) -> writer.name(k).value(v) }
            writer.endObject()
        }
    }

    companion object {
        private val stringStringMapType: Type =
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)

        /** Registers [FlexibleErrorsAdapter] for every `Map<String, String>` field. */
        val FACTORY = JsonAdapter.Factory { type, _, _ ->
            if (type == stringStringMapType) FlexibleErrorsAdapter() else null
        }
    }
}
