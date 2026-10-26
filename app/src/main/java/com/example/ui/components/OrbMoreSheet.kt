package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.AppTheme
import com.example.ui.util.HapticManager

data class MoreModuleItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val accentColor: Color? = null
)

/**
 * Spatial Glass Sheet replacing old sidebar drawer with an intuitive 3-column grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrbMoreSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelectScreen: (Screen) -> Unit,
    onNewIdeaClick: () -> Unit,
    onExportClick: () -> Unit,
    isAdminOrOwner: Boolean = false,
    hapticManager: HapticManager? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val tokens = AppTheme.tokens

    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            GlassSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MORE MODULES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.16.sp
                                ),
                                color = tokens.textLabel
                            )
                            Text(
                                text = "Executive Tools & Intelligence",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                hapticManager?.performClick()
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = tokens.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val modules = buildList {
                        add(MoreModuleItem("New Idea", Icons.Default.Add, Screen.MyIdeas, tokens.modeListening))
                        add(MoreModuleItem("Experiments", Icons.Default.Science, Screen.Experiments, tokens.modeValidate))
                        add(MoreModuleItem("Actions", Icons.Default.Checklist, Screen.Actions, tokens.modeResearch))
                        add(MoreModuleItem("Reminders", Icons.Default.Notifications, Screen.Reminders, tokens.modeError))
                        add(MoreModuleItem("Insights", Icons.Default.Psychology, Screen.Insights, tokens.modeAnalyze))
                        add(MoreModuleItem("Charts", Icons.Default.BarChart, Screen.Charts, tokens.modeResearch))
                        add(MoreModuleItem("Matrix", Icons.Default.ViewQuilt, Screen.Matrix, tokens.modeCompare))
                        add(MoreModuleItem("Recycle Bin", Icons.Default.Delete, Screen.RecycleBin, tokens.modeError))
                        add(MoreModuleItem("Profile", Icons.Default.Person, Screen.Profile, tokens.textPrimary))
                        add(MoreModuleItem("Settings", Icons.Default.Settings, Screen.Settings, tokens.textSecondary))
                        add(MoreModuleItem("Export", Icons.Default.FileDownload, Screen.Dashboard, tokens.modeAnalyze))
                        if (isAdminOrOwner) {
                            add(MoreModuleItem("Admin", Icons.Default.AdminPanelSettings, Screen.Dashboard, tokens.modeListening))
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(modules) { item ->
                            MoreGridTile(
                                item = item,
                                onClick = {
                                    hapticManager?.performClick()
                                    onDismiss()
                                    when (item.title) {
                                        "New Idea" -> onNewIdeaClick()
                                        "Export" -> onExportClick()
                                        else -> onSelectScreen(item.screen)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MoreGridTile(
    item: MoreModuleItem,
    onClick: () -> Unit
) {
    val tokens = AppTheme.tokens
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = tokens.inputChipFill,
        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.inputChipBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        (item.accentColor ?: tokens.modeAnalyze).copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.accentColor ?: tokens.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = tokens.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
