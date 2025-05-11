package com.example.helionconsole.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helionconsole.components.TerminalLine
import kotlinx.coroutines.delay

@SuppressLint("RememberReturnType")
@Composable
fun SpinnerLine() {
    val spinnerChars = listOf("|", "/", "-", "\\")
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10)
            index = (index + 1) % spinnerChars.size
        }
    }

    TerminalLine(
        text = "[SCANNING] ${spinnerChars[index]}",
        //fontSize = 12.sp,
        heightMultiplier = 2.0f // tweak if needed
    )
}

@Composable
fun TypewriterText(
    fullText: String,
    charDelay: Long = 25L,
    skipSignal: Boolean = false,
    onFinished: (() -> Unit)? = null
) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(fullText, skipSignal) {
        visibleText = ""
        if (skipSignal) {
            visibleText = fullText
            onFinished?.invoke()
        } else {
            for (char in fullText) {
                visibleText += char
                delay(charDelay)
            }
            onFinished?.invoke()
        }
    }

    TerminalLine(
        text = visibleText,
        //fontSize = 12.sp,
        heightMultiplier = 2.0f // tweak if needed
    )
}

@Composable
fun ParallelTypewriterBlock(
    lines: List<String>,
    charDelay: Long = 25L,
    onFinished: () -> Unit
) {
    val lineStates = remember { lines.map { mutableStateOf("") } }
    var completedCount by remember { mutableIntStateOf(0) }

    // Launch typing for each line in parallel
    lines.forEachIndexed { index, line ->
        LaunchedEffect(line) {
            lineStates[index].value = ""
            for (char in line) {
                lineStates[index].value += char
                delay(charDelay)
            }
            completedCount += 1
        }
    }

    // Once all lines are typed, call onFinished
    LaunchedEffect(completedCount) {
        if (completedCount == lines.size) {
            onFinished()
        }
    }

    val lineHeight = with(LocalDensity.current) { 26.sp.toDp() }

    Column {
        lines.forEachIndexed { index, _ ->
            val animatedText = lineStates[index].value

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lineHeight)
            ) {
                TerminalLine(
                    text = animatedText,
                    //fontSize = 12.sp,
                    heightMultiplier = 2.0f // tweak if needed
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

