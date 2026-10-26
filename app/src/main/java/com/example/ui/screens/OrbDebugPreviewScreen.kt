package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassBackdrop
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.OrbCore
import com.example.ui.components.OrbState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.util.HapticManager

/**
 * Phase 2 Debug Preview Screen: Test and verify all 8 Orb states,
 * RMS voice pulsation, parallax tilt, and Night/Morning theme tokens.
 */
@Composable
fun OrbDebugPreviewScreen(
    currentThemeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    var orbState by remember { mutableStateOf(OrbState.ANALYZING) }
    var rmsLevel by remember { mutableFloatStateOf(4f) }
    var enableParallax by remember { mutableStateOf(true) }
    val tokens = AppTheme.tokens

    GlassBackdrop(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = tokens.textPrimary
                    )
                }
                Text(
                    text = "Orb Core Preview",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = tokens.textPrimary
                )
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (tokens.isNight) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = tokens.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Hero Orb (220dp)
            OrbCore(
                state = orbState,
                size = 220.dp,
                rmsLevel = rmsLevel,
                enableParallax = enableParallax
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = orbState.statusText.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.16.sp
                ),
                color = tokens.textLabel
            )

            Spacer(modifier = Modifier.height(20.dp))

            // State Selector Chips
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SELECT ORB STATE (8 STATES)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = tokens.textLabel
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrbState.values().forEach { st ->
                            GlassChip(
                                selected = st == orbState,
                                onClick = {
                                    orbState = st
                                },
                                label = st.name,
                                hapticManager = hapticManager
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Simplified Dock Orb Preview (48dp)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Simplified Dock Orb (48dp)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Mirrors live state with single ring",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = tokens.inputChipFill,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            OrbCore(
                                state = orbState,
                                size = 48.dp,
                                rmsLevel = rmsLevel,
                                isSimplified = true
                            )
                        }
                    }
                }
            }

            if (orbState == OrbState.LISTENING) {
                Spacer(modifier = Modifier.height(14.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Microphone RMS Level: ${rmsLevel.toInt()}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = tokens.textPrimary
                        )
                        Slider(
                            value = rmsLevel,
                            onValueChange = { rmsLevel = it },
                            valueRange = 0f..10f
                        )
                    }
                }
            }
        }
    }
}
