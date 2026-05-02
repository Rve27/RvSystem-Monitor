package com.rve.systemmonitor.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick

data class InfoCardData(
    val title: String,
    val headline: String,
    val subhead: String,
    val iconRes: Int,
    val backgroundIconRes: Int = iconRes,
    val backgroundIconOffset: Dp = 30.dp,
    val badges: List<String> = emptyList(),
    val secondaryBadgeIndices: List<Int> = listOf(0),
    val onHelpClick: (() -> Unit)? = null,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoOverviewCard(data: InfoCardData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(data.backgroundIconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(160.dp)
                    .offset(y = data.backgroundIconOffset)
                    .alpha(0.30f),
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(data.iconRes),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        Text(
                            text = data.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    if (data.onHelpClick != null) {
                        IconButton(
                            onClick = rememberHapticOnClick(data.onHelpClick),
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.help_filled),
                                contentDescription = "Help",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = data.headline,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.subhead,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (data.badges.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        data.badges.forEachIndexed { index, badge ->
                            val isSecondary = data.secondaryBadgeIndices.contains(index)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSecondary) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.tertiary,
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSecondary) MaterialTheme.colorScheme.onSecondary
                                    else MaterialTheme.colorScheme.onTertiary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
