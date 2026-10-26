package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import com.example.ui.export.IdeaExportHelper
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.ThemeMode

@Composable
fun ExportIdeasDialog(
    ideas: List<Idea>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val themeMode = LocalThemeMode.current
    val isDark = themeMode == ThemeMode.DARK
    val primaryCyan = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = primaryCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Ideas Portfolio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose a format to share ${ideas.size} saved idea theses externally:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: PDF Document
                ExportOptionCard(
                    title = "Export as PDF Document",
                    subtitle = "Formatted report with executive scorecards & metrics",
                    icon = Icons.Default.PictureAsPdf,
                    accentColor = Color(0xFFEF4444),
                    tag = "export_pdf_button",
                    onClick = {
                        onDismiss()
                        IdeaExportHelper.exportAsPdf(context, ideas)
                    }
                )

                // Option 2: Text / Markdown File
                ExportOptionCard(
                    title = "Export as Text File (.txt)",
                    subtitle = "Plain text / markdown for easy copy, Slack, or Notion",
                    icon = Icons.Default.Description,
                    accentColor = primaryCyan,
                    tag = "export_txt_button",
                    onClick = {
                        onDismiss()
                        IdeaExportHelper.exportAsText(context, ideas)
                    }
                )

                // Option 3: Copy to Clipboard
                ExportOptionCard(
                    title = "Copy Summary to Clipboard",
                    subtitle = "Instant text clipboard paste for quick messaging",
                    icon = Icons.Default.ContentCopy,
                    accentColor = Color(0xFF10B981),
                    tag = "export_clipboard_button",
                    onClick = {
                        onDismiss()
                        IdeaExportHelper.copyToClipboard(context, ideas)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("export_cancel_button")
            ) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
