package com.example.helionconsole.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.example.helionconsole.utils.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ModifierParameter")
@Composable
fun DisplaySequenceRenderer(
    events: List<ScreenEvent>,
    skipSignal: Boolean = false,
    modifier: Modifier = Modifier,
    visibleLines: SnapshotStateList<String> = remember { mutableStateListOf() },
    onComplete: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val currentEvent = remember { mutableStateOf<ScreenEvent?>(null) }
    val typingLock = remember { mutableStateOf(CompletableDeferred<Unit>()) }
    val coroutineScope = rememberCoroutineScope()
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    LaunchedEffect(Unit) {
        runDisplaySequence(
            events,
            onEventEmit = {
                currentEvent.value = it
                typingLock.value = CompletableDeferred()
            },
            waitForTypingDone = {
                typingLock.value.await()
            }
        )
        onComplete()
    }

    LaunchedEffect(visibleLines.size, currentEvent.value, imeVisible) {
        val threshold = 80 // how close to bottom is considered "locked"

        val isAtBottom = scrollState.maxValue - scrollState.value < threshold

        if (isAtBottom) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        if (imeVisible) {
            coroutineScope.launch {
                if (scrollState.maxValue - scrollState.value < 735) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }
        }

        //Log.d("HELION_DEBUG", "Scroll delta: ${scrollState.maxValue} | ${scrollState.value}")
    }

    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        visibleLines.forEach { line ->
            TerminalLine(text = line)
        }

        currentEvent.value?.let { event ->
            when (event) {
                is ScreenEvent.Line -> TypewriterText(
                    fullText = event.text,
                    skipSignal = skipSignal,
                    onFinished = {
                        coroutineScope.completeDisplayStep(
                            event = event,
                            visibleLines = visibleLines,
                            currentEvent = currentEvent,
                            typingLock = typingLock
                        )
                    }
                )

                is ScreenEvent.Group -> ParallelTypewriterBlock(
                    lines = event.lines,
                    onFinished = {
                        coroutineScope.completeDisplayStep(
                            event = event,
                            visibleLines = visibleLines,
                            currentEvent = currentEvent,
                            typingLock = typingLock
                        )
                    }
                )

                else -> {}
            }
        }
    }
}

private fun CoroutineScope.completeDisplayStep(
    event: ScreenEvent,
    visibleLines: SnapshotStateList<String>,
    currentEvent: MutableState<ScreenEvent?>,
    typingLock: MutableState<CompletableDeferred<Unit>>
) {
    launch {
        currentEvent.value = null
        //delay(10) // gives Compose a frame to clear old view
        when (event) {
            is ScreenEvent.Line -> visibleLines.add(event.text)
            is ScreenEvent.Group -> visibleLines.addAll(event.lines)
            else -> {}
        }
        delay(340) // pacing delay
        typingLock.value.complete(Unit)
    }
}
