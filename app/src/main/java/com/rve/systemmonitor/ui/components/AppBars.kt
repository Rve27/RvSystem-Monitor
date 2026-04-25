@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.rve.systemmonitor.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.roundedfilled.R.drawable.materialsymbols_ic_settings_rounded_filled

/**
 * A collection of Top App Bars used in the application.
 */
object AppBars {
    /**
     * A simple Top App Bar with a title, subtitle, and a settings action.
     *
     * @param title The main title to display in the app bar.
     * @param subtitle The subtitle to display below the main title.
     * @param onNavigateToSettings Callback invoked when the settings icon is clicked.
     */
    @Composable
    fun SimpleTopAppBar(title: String, subtitle: String, onNavigateToSettings: () -> Unit) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            subtitle = {
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            actions = {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Left,
                    ),
                    tooltip = {
                        PlainTooltip {
                            Text("Settings")
                        }
                    },
                    state = rememberTooltipState(),
                ) {
                    IconButton(
                        onClick = onNavigateToSettings,
                    ) {
                        Icon(
                            painterResource(materialsymbols_ic_settings_rounded_filled),
                            contentDescription = "Settings",
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

/**
 * A Medium Top App Bar that adjusts its title padding and navigation icon style based on scroll.
 *
 * @param title The title to display in the app bar.
 * @param onNavigateBack Callback invoked when the navigation (back) icon is clicked.
 * @param scrollBehavior The [TopAppBarScrollBehavior] that dictates how the app bar responds to scrolling.
 */
@Composable
fun ExitUntilCollapsedMediumTopAppBar(title: String, onNavigateBack: () -> Unit, scrollBehavior: TopAppBarScrollBehavior) {
    MediumTopAppBar(
        title = {
            val titleStartPadding = animateDpAsState(
                targetValue = if (scrollBehavior.state.collapsedFraction > 0.5f) 6.dp else 0.dp,
                animationSpec = tween( 250),
                label = "titleStartPadding",
            )
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = titleStartPadding.value),
            )
        },
        navigationIcon = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Right,
                ),
                tooltip = {
                    PlainTooltip {
                        Text("Back")
                    }
                },
                state = rememberTooltipState(),
            ) {
                Crossfade(
                    targetState = scrollBehavior.state.collapsedFraction > 0.5f,
                    animationSpec = tween(500)
                ) { scrolled ->
                    if (scrolled) {
                        FilledIconButton(
                            onClick = onNavigateBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    } else {
                        FilledTonalIconButton(
                            onClick = onNavigateBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
