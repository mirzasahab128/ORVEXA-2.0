package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.HapticManager

data class TemplatePrompt(
    val title: String,
    val prompt: String,
    val icon: ImageVector,
    val category: String
)

object AnalyzerPromptPresets {
    val defaultPrompts = listOf(
        TemplatePrompt(
            title = "Risk & Assumptions",
            prompt = "Audit the 3 biggest market assumptions and fatal execution risks for this idea.",
            icon = Icons.Default.Psychology,
            category = "Strategy"
        ),
        TemplatePrompt(
            title = "Pricing Models",
            prompt = "Propose 3 revenue and pricing models (Freemium, Tiered SaaS, Usage-based) with pros and cons.",
            icon = Icons.Default.MonetizationOn,
            category = "Monetization"
        ),
        TemplatePrompt(
            title = "7-Day Validation",
            prompt = "Design a low-cost, 7-day validation experiment to test demand before writing code.",
            icon = Icons.Default.Science,
            category = "Validation"
        ),
        TemplatePrompt(
            title = "Beachhead Market",
            prompt = "Identify our exact beachhead ICP (Ideal Customer Profile) and where to find the first 100 users.",
            icon = Icons.Default.TrendingUp,
            category = "Growth"
        ),
        TemplatePrompt(
            title = "Moat & Defensibility",
            prompt = "Evaluate what defensibility or moat can prevent big incumbents from copying this solution.",
            icon = Icons.Default.AutoAwesome,
            category = "Competitive"
        )
    )
}

/**
 * Responsive scrolling chips bar for quick AI analyzer guidance.
 */
@Composable
fun AnalyzerTemplateChipsBar(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier,
    hapticManager: HapticManager? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Template Prompts",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalyzerPromptPresets.defaultPrompts.forEach { item ->
                SuggestionChip(
                    onClick = {
                        hapticManager?.performClick()
                        onSelectPrompt(item.prompt)
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}
