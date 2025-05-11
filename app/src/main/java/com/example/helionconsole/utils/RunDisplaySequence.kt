package com.example.helionconsole.utils

import kotlinx.coroutines.delay

suspend fun runDisplaySequence(
    events: List<ScreenEvent>,
    onEventEmit: suspend (ScreenEvent) -> Unit,
    waitForTypingDone: suspend () -> Unit
) {
    for (event in events) {
        when (event) {
            is ScreenEvent.Line,
            is ScreenEvent.Group -> {
                onEventEmit(event)
                waitForTypingDone()
                delay(350)
            }

            is ScreenEvent.Delay -> delay(event.millis)

            is ScreenEvent.TapToContinue -> {
                onEventEmit(ScreenEvent.Line("")) // visual spacing
                break
            }
        }
    }
}