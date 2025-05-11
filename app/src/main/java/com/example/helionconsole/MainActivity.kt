package com.example.helionconsole

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.helionconsole.ui.theme.HelionConsoleTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.example.helionconsole.components.DisplaySequenceRenderer
import com.example.helionconsole.utils.CommandRouter
import com.example.helionconsole.utils.ScreenEvent
import com.example.helionconsole.utils.TypewriterText
import com.example.helionconsole.utils.parseScreenFile
import com.example.helionconsole.utils.runBootSequence
import kotlinx.coroutines.CompletableDeferred

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CommandRouter.register("status") {
            listOf("All systems nominal. S.A.R.A.H. operational.").joinToString()
        }

        CommandRouter.register("echo") { args ->
            args.joinToString(" ")
        }

        CommandRouter.register("bridge") { _ ->
            "Redirecting to BRIDGE interface... (Not implemented yet)"
        }

        enableEdgeToEdge()
        setContent {
            HelionConsoleTheme {
                TerminalScreen()
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val events = remember { parseScreenFile(context, "boot_sequence.txt") }// for scripting boot, tbd
    val typingLock = remember { mutableStateOf(CompletableDeferred<Unit>()) }
    var currentEvent by remember { mutableStateOf<ScreenEvent?>(null) }
    var commandInput by remember { mutableStateOf("") }
    val terminalHeight =
        (LocalConfiguration.current.screenHeightDp.dp - 120.dp).coerceAtLeast(200.dp)
    var isHolding by remember { mutableStateOf(false) }
    val visibleLines = remember { mutableStateListOf<String>() }
    var displayDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        //to run whatever procedure on boost.
        runBootSequence(
            events,
            onEventEmit = { event ->
                currentEvent = event
                typingLock.value = CompletableDeferred()
            },
            waitForTypingDone = {
                typingLock.value.await()
            }
        )
    }

    fun handleCommand(input: String) {
        if (input.isBlank()) return

        visibleLines.add("> $input") // Echo back user command

        val response = CommandRouter.handle(input)
        if (response.isNotBlank()) {
            visibleLines.add(response)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .imePadding()
            .padding(WindowInsets.systemBars.asPaddingValues()) // <== magic line
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        tryAwaitRelease()
                        isHolding = false
                    }
                )
            }
    ) {
        //terminal display
        DisplaySequenceRenderer(
            events = parseScreenFile(context, "boot_screen.txt"),
            skipSignal = isHolding,
            visibleLines = visibleLines,
            modifier = Modifier
                .fillMaxWidth()
                .height(terminalHeight)
                .padding(start = 14.dp, top = 16.dp, bottom = 60.dp, end = 14.dp)
                .imePadding(),
            onComplete = { displayDone = true }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black)
                .imePadding()
                .navigationBarsPadding() // handles keyboard movement cleanly
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 0.dp),
                thickness = 2.dp,
                color = Color.DarkGray
            )

            //user input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //.background(Color.LightGray)
                    .padding(start = 14.dp,top = 20.dp, bottom = 2.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "> ",
                    color = if (displayDone) Color.Green else Color.Gray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )

                BasicTextField(
                    enabled = displayDone,
                    value = commandInput,
                    onValueChange = {
                        if (it.endsWith("\n")) {
                            handleCommand(commandInput.trim())
                            commandInput = ""
                        } else {
                            commandInput = it
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Green,
                        //fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val trimmed = commandInput.trim()
                            if (trimmed.isNotEmpty()) {
                                handleCommand(trimmed)
                                commandInput = ""
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .alpha(if (displayDone) 1f else 0.5f),
                    decorationBox = { innerTextField ->
                        Box(Modifier.fillMaxWidth()) {
                            if (commandInput.isEmpty() && !displayDone) {
                                Text(
                                    text = "Booting systems...",
                                    color = Color.DarkGray,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}


