package com.example.helionconsole.utils

import android.content.Context

fun convertTextToJson(context: Context, filename: String): String {
    val input = context.assets.open(filename)
    val lines = input.bufferedReader().readLines()

    val jsonLines = lines.joinToString(separator = ",\n") {
        "    ${'"'}$it${'"'}"
    }

    return "{\n  \"screen\": [\n$jsonLines\n  ]\n}"
}

sealed class ScreenEvent {
    data class Line(val text: String, val skipDelay: Boolean = false) : ScreenEvent()
    data class Group(val lines: List<String>) : ScreenEvent()
    data class Delay(val millis: Long) : ScreenEvent()
    data object TapToContinue : ScreenEvent()
}

fun parseScreenFile(context: Context, filename: String): List<ScreenEvent> {
    val input = context.assets.open(filename)
    val rawLines = input.bufferedReader().readLines()

    val events = mutableListOf<ScreenEvent>()
    val buffer = mutableListOf<String>()
    var inGroup = false

    for (line in rawLines) {
        when {
            line.trim() == "/(" -> {
                inGroup = true
                buffer.clear()
            }

            line.trim() == "/)" -> {
                inGroup = false
                events.add(ScreenEvent.Group(buffer.toList()))
                buffer.clear()
            }

            inGroup -> {
                buffer.add(line)
            }

            line.startsWith("[DELAY:") -> {
                val time = line.removePrefix("[DELAY:").removeSuffix("]").toLongOrNull() ?: 1000
                events.add(ScreenEvent.Delay(time))
            }

            line == "[TAP_TO_CONTINUE]" -> {
                events.add(ScreenEvent.TapToContinue)
            }

            else -> {
                events.add(ScreenEvent.Line(line))
            }
        }
    }

    return events
}

