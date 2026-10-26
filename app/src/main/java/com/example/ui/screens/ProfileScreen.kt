package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.CloudSyncStatus
import com.example.ui.IdeaViewModel
import com.example.ui.components.AddIdeaBottomSheet
import com.example.ui.components.ExportIdeasDialog
import com.example.ui.components.OrvexaObsidianFab
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: IdeaViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val ideas by viewModel.allIdeas.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncMessage by viewModel.lastSyncMessage.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var authFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val isDark = themeMode == ThemeMode.DARK
    val primaryAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF0F172A)
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val orvexaOrange = Color(0xFFE4531F)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, if (isDark) Color(0x2EFFFFFF) else Color(0xFFE2E8F0)),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenDrawer()
                            },
                            modifier = Modifier.testTag("hamburger_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = "Profile & Cloud",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Firebase auth, cloud sync & offline cache",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    ThemeSwitcherButton(
                        currentTheme = themeMode,
                        onToggleTheme = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleTheme()
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            OrvexaObsidianFab(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showBottomSheet = true
                },
                extended = true
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("profile_lazy_column"),
            contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
        ) {

            // Firebase Cloud Authentication & Sync Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF1A1624) else Color(0xFFF9FAFB),
                    border = BorderStroke(1.2.dp, if (currentUser != null) orvexaOrange.copy(alpha = 0.5f) else cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("firebase_auth_sync_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = orvexaOrange.copy(alpha = 0.2f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = orvexaOrange,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "FIREBASE CLOUD & AUTH",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = orvexaOrange,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = if (currentUser != null) "Connected & Cloud-Synced" else "Local Session (Unauthenticated)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Cloud Status Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (syncStatus) {
                                    CloudSyncStatus.SYNCED -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    CloudSyncStatus.SYNCING -> orvexaOrange.copy(alpha = 0.2f)
                                    CloudSyncStatus.ERROR -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    else -> Color(0x22FFFFFF)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (syncStatus) {
                                            CloudSyncStatus.SYNCED -> Color(0xFF10B981)
                                            CloudSyncStatus.SYNCING -> orvexaOrange
                                            CloudSyncStatus.ERROR -> Color(0xFFEF4444)
                                            else -> Color.Gray
                                        },
                                        modifier = Modifier.size(6.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = syncStatus.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (syncStatus) {
                                            CloudSyncStatus.SYNCED -> Color(0xFF10B981)
                                            CloudSyncStatus.SYNCING -> orvexaOrange
                                            CloudSyncStatus.ERROR -> Color(0xFFEF4444)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val user = currentUser
                        if (user != null) {
                            // User Info Block
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isDark) Color(0x15FFFFFF) else Color(0x0A000000),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (user.photoUrl != null) {
                                    AsyncImage(
                                        model = user.photoUrl,
                                        contentDescription = "User Avatar",
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = orvexaOrange.copy(alpha = 0.3f),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = (user.displayName ?: user.email ?: "U").take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName ?: "Orvexa Innovator",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = user.email ?: "Signed in via Google",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.syncAllIdeasToCloud()
                                        authFeedbackMessage = "Syncing ${ideas.size} ideas to Firestore..."
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = orvexaOrange,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sync_all_ideas_cloud_btn")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync to Cloud", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.signOut()
                                        authFeedbackMessage = "Signed out of Google session"
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    modifier = Modifier.testTag("firebase_sign_out_btn")
                                ) {
                                    Text("Sign Out", fontSize = 12.sp)
                                }
                            }
                        } else {
                            // Google Sign-In Action Block
                            Text(
                                text = "Sign in with your Google Account to automatically sync ideas, research nodes, and AI evaluations to your dedicated Firebase Firestore cloud database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    authFeedbackMessage = "Opening Google Sign-In..."
                                    viewModel.signInWithGoogle(
                                        onSuccess = { user ->
                                            authFeedbackMessage = "Welcome, ${user.displayName ?: "Innovator"}! Ideas synced to cloud."
                                        },
                                        onError = { error ->
                                            authFeedbackMessage = "Sign in notice: $error"
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4285F4),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_sign_in_button")
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign in with Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Feedback message pill
                        authFeedbackMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x18FFFFFF),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Founder Identity Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primaryAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Decision Engine Operator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Primary Focus: B2B High-Margin Software",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isDark) Color(0xFF162032) else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = "THESIS CALIBRATED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Room Database Caching & Offline Strategy
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ROOM DATABASE OFFLINE CACHING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryAccent,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "OFFLINE READY",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Offline-first caching strategy: All ideas, requirements, competitors, and experiments are cached locally in SQLite via Room. Even during network latency or total offline disconnection, the entire intelligence suite functions with zero latency.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ProfileRow("Database File", "idea_intelligence_os.db")
                        ProfileRow("Cached Entities", "${ideas.size} Idea records")
                        ProfileRow("Sync Layer", if (currentUser != null) "Cloud Firestore (Active)" else "Local Room Cache Only")
                        ProfileRow("Inspector Hook", "androidx.sqlite.framework (Active)")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showResetConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryAccent,
                                contentColor = if (isDark) Color(0xFF070A0F) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Starter Dataset", fontWeight = FontWeight.Bold)
                        }

                        if (showResetConfirmDialog) {
                            AlertDialog(
                                onDismissRequest = { showResetConfirmDialog = false },
                                title = { Text("Reset to Seed Data?") },
                                text = { Text("This will restore the curated sample ideas and clear unsaved custom entries.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.resetToSeedData()
                                            showResetConfirmDialog = false
                                        }
                                    ) {
                                        Text("Reset")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showResetConfirmDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.clearAllIdeas()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Database (Test Empty State)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FilledTonalButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showExportDialog = true
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = primaryAccent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_export_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Saved Ideas (PDF / Text)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        AddIdeaBottomSheet(
            onDismiss = { showBottomSheet = false },
            onSaveIdea = { title, desc, cat, tag, priority, customTags ->
                viewModel.saveIdea(title, desc, cat, tag, priority, customTags)
            }
        )
    }

    if (showExportDialog) {
        ExportIdeasDialog(
            ideas = ideas,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
