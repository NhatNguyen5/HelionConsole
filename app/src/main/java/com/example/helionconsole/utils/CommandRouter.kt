package com.example.helionconsole.utils

import android.util.Log

typealias CommandHandler = (args: List<String>) -> String

object CommandRouter {
    private val routes = mutableMapOf<String, CommandHandler>()

    fun register(command: String, handler: CommandHandler) {
        routes[command.lowercase()] = handler

    }

    fun handle(input: String): String {
        val parts = input.trim().split(" ")
        if (parts.isEmpty()) return ""

        val command = parts.first().lowercase()
        val args = parts.drop(1)
        return routes[command]?.invoke(args)
            ?: "Unrecognized command: $command"
    }
}