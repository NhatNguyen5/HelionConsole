package com.example.helionconsole.utils

import kotlinx.coroutines.delay

suspend fun runBootSequence(
    events: List<ScreenEvent>,
    onLineEmit: suspend (ScreenEvent) -> Unit, // <- emit full event now
    waitForTypingDone: suspend () -> Unit
) {
    for (event in events) {
        when (event) {
            is ScreenEvent.Line, is ScreenEvent.Group -> {
                onLineEmit(event)
                waitForTypingDone()
                delay(1000)
            }

            is ScreenEvent.Delay -> delay(event.millis)

            is ScreenEvent.TapToContinue -> {
                onLineEmit(ScreenEvent.Line("▶ TAP TO CONTINUE"))
                break
            }
        }
    }
}
