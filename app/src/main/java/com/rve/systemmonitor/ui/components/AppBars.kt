@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.rve.systemmonitor.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
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
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.navigation.TRANSITION_DURATION

/**
 * A collection of Top App Bars used in the application.
 */
object AppBars {
    /**
     * A simple Top App Bar with a title, subtitle, and actions.
     *
     * @param title The main title to display in the app bar.
     * @param subtitle The subtitle to display below the main title.
     * @param onNavigateToSettings Callback invoked when the settings icon is clicked.
     * @param onNavigateToOverlaySettings Callback invoked when the overlay icon is clicked.
     */
    @Composable
    fun SimpleTopAppBar(title: String, subtitle: String, onNavigateToSettings: () -> Unit, onNavigateToOverlaySettings: () -> Unit) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            subtitle = {
                AnimatedContent(
                    targetState = subtitle,
                    transitionSpec = {
                        (
                            slideInHorizontally(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ) + scaleIn(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                            ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ) + scaleOut(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ),
                        )
                    },
                    label = "SubtitleAnimation",
                ) { targetSubtitle ->
                    Text(
                        text = targetSubtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            actions = {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Left,
                    ),
                    tooltip = {
                        PlainTooltip {
                            Text("Overlay Settings")
                        }
                    },
                    state = rememberTooltipState(),
                ) {
                    IconButton(
                        onClick = onNavigateToOverlaySettings,
                    ) {
                        Icon(
                            painterResource(R.drawable.layers_filled),
                            contentDescription = "Overlay Settings",
                        )
                    }
                }

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
                            painterResource(R.drawable.settings_filled),
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
                animationSpec = tween(250),
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
                    animationSpec = tween(500),
                ) { scrolled ->
                    if (scrolled) {
                        FilledIconButton(
                            onClick = onNavigateBack,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back_ios_new),
                                contentDescription = "Back",
                            )
                        }
                    } else {
                        FilledTonalIconButton(
                            onClick = onNavigateBack,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back_ios_new),
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
