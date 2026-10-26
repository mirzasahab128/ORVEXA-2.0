package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.ThemeMode

@Composable
fun OrvexaObsidianFab(
    onClick: () -> Unit,
    extended: Boolean = true,
    modifier: Modifier = Modifier
) {
    val themeMode = LocalThemeMode.current
    val isDark = themeMode == ThemeMode.DARK

    val bgGradient = if (isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B)
            )
        )
    }

    val borderColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF38BDF8)
    val contentColor = if (isDark) Color(0xFF00E5FF) else Color(0xFFF8FAFC)
    val glowColor = if (isDark) Color(0x6600E5FF) else Color(0x33000000)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = 8.dp,
        modifier = modifier
            .testTag("obsidian_fab")
            .height(56.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
    ) {
        Box(
            modifier = Modifier
                .background(bgGradient)
                .padding(horizontal = if (extended) 20.dp else 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Electric indicator badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0x3300E5FF) else Color(0x3338BDF8)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Idea",
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (extended) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Capture Idea",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.White,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
