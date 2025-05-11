package com.example.helionconsole.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.alpha

@Composable
fun TerminalLine(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontColor: Color = Color.Green,
    fontFamily: FontFamily = FontFamily.Monospace,
    heightMultiplier: Float = 2.0f, // ← works best between 1.8–2.2 for monospace
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Visible,
    alpha: Float = 1f
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = text,
            color = fontColor,
            fontSize = fontSize,
            fontFamily = fontFamily,
            softWrap = softWrap,
            maxLines = maxLines,
            overflow = overflow,
            modifier = Modifier.alpha(alpha)
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}