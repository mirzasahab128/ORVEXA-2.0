package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThemeMode

@Composable
fun ThemeSwitcherButton(
    currentTheme: ThemeMode,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = currentTheme == ThemeMode.DARK

    val iconRotation by animateFloatAsState(
        targetValue = if (isDark) 0f else 180f,
        animationSpec = tween(durationMillis = 300),
        label = "theme_icon_rotation"
    )

    val buttonBg by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
        animationSpec = tween(durationMillis = 200),
        label = "theme_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF00E5FF) else Color(0xFF0F172A),
        animationSpec = tween(durationMillis = 200),
        label = "theme_border"
    )

    Surface(
        onClick = onToggleTheme,
        shape = RoundedCornerShape(12.dp),
        color = buttonBg,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        modifier = modifier
            .testTag("theme_switcher_button")
            .height(38.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isDark) "Switch to Obsidian Light Theme" else "Switch to Obsidian Dark Theme",
                tint = if (isDark) Color(0xFF00E5FF) else Color(0xFF0F172A),
                modifier = Modifier
                    .size(18.dp)
                    .rotate(iconRotation)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = if (isDark) "DARK" else "LIGHT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFF00E5FF) else Color(0xFF0F172A),
                letterSpacing = 0.8.sp
            )
        }
    }
}
