package com.example.helionconsole

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helionconsole.ui.theme.HelionConsoleTheme
import androidx.compose.ui.platform.LocalContext
import com.example.helionconsole.utils.ScreenEvent
import com.example.helionconsole.utils.TypewriterText
import com.example.helionconsole.utils.ParallelTypewriterBlock
import com.example.helionconsole.utils.parseScreenFile
import com.example.helionconsole.utils.runBootSequence
import com.example.helionconsole.components.TerminalLine
import kotlinx.coroutines.CompletableDeferred

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelionConsoleTheme {
                TerminalScreen()
            }
        }
    }
}

@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val events = remember { parseScreenFile(context, "boot_screen.txt") }
    val visibleLines = remember { mutableStateListOf<String>() }
    val typingLock = remember { mutableStateOf(CompletableDeferred<Unit>()) }
    val currentState = remember { mutableStateOf<ScreenEvent?>(null) }
    var currentlyTyping by currentState


    LaunchedEffect(Unit) {
        runBootSequence(
            events,
            onLineEmit = { event ->
                currentlyTyping = event
                typingLock.value = CompletableDeferred()
            },
            waitForTypingDone = {
                typingLock.value.await()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        visibleLines.forEach { line ->
            TerminalLine(
                text = line,
                fontSize = 14.sp,
                heightMultiplier = 2.0f // tweak if needed
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        currentlyTyping?.let { event ->
            when (event) {
                is ScreenEvent.Line -> {
                    TypewriterText(
                        fullText = event.text,
                        onFinished = {
                            visibleLines.add(event.text)
                            currentlyTyping = null
                            typingLock.value.complete(Unit)
                        }
                    )
                }

                is ScreenEvent.Group -> {
                    ParallelTypewriterBlock(
                        lines = event.lines,
                        onFinished = {
                            visibleLines.addAll(event.lines)
                            currentlyTyping = null
                            typingLock.value.complete(Unit)
                        }
                    )
                }

                else -> {}
            }
        }
    }
}


