package com.rve.systemmonitor.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.haptic.hapticClickable
import com.rve.systemmonitor.ui.theme.LocalBlurEffectEnabled
import com.rve.systemmonitor.ui.theme.LocalNavBarCornerRadius
import com.rve.systemmonitor.ui.theme.LocalNavMode
import com.rve.systemmonitor.ui.theme.LocalNavType
import com.rve.systemmonitor.utils.NavMode
import com.rve.systemmonitor.utils.NavType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object BottomNavBar {
    data class NavItem(val label: String, val iconUnselected: Int, val iconSelected: Int)

    @Composable
    fun BottomNavigationBar(pagerState: PagerState, coroutineScope: CoroutineScope, backdrop: Backdrop, modifier: Modifier = Modifier) {
        val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
        val context = LocalContext.current

        val items = remember(context) {
            listOf(
                NavItem(
                    label = context.getString(R.string.nav_label_home),
                    iconUnselected = R.drawable.home,
                    iconSelected = R.drawable.home_filled,
                ),
                NavItem(
                    label = context.getString(R.string.nav_label_cpu),
                    iconUnselected = R.drawable.memory,
                    iconSelected = R.drawable.memory_filled,
                ),
                NavItem(
                    label = context.getString(R.string.nav_label_memory),
                    iconUnselected = R.drawable.memory_alt,
                    iconSelected = R.drawable.memory_alt_filled,
                ),
                NavItem(
                    label = context.getString(R.string.nav_label_battery),
                    iconUnselected = R.drawable.battery_android_0,
                    iconSelected = R.drawable.battery_android_full,
                ),
            )
        }

        val cornerRadius = LocalNavBarCornerRadius.current
        val navMode = LocalNavMode.current
        val navType = LocalNavType.current

        val barShape = when (navMode) {
            NavMode.FLOATING -> RoundedCornerShape(cornerRadius)
            // Attached to the bottom edge, so the bottom corners would be clipped off-screen anyway.
            NavMode.STANDARD -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        }
        val indicatorShape = when (navType) {
            // Inset by the bar's 4dp padding so the indicator corners stay concentric with the bar's.
            NavType.LEGACY -> RoundedCornerShape((cornerRadius - 4.dp).coerceAtLeast(0.dp))
            NavType.MODERN -> RoundedCornerShape(12.dp)
        }

        // Sampling the backdrop without any effect would show the content behind the bar
        // unblurred through a translucent fill, so opt out of drawBackdrop entirely and
        // fall back to an opaque surface when the user disables the blur effect.
        val blurEnabled = LocalBlurEffectEnabled.current
        val barBackground = if (blurEnabled) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { barShape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx(), chromaticAberration = true)
                },
                onDrawSurface = { drawRect(backgroundColor) },
            )
        } else {
            Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
        }

        Box(
            modifier = modifier
                .clip(barShape)
                .then(barBackground),
        ) {
            Box(
                // STANDARD draws behind the system nav bar, so inset the items to keep them tappable.
                modifier = Modifier
                    .then(
                        if (navMode == NavMode.STANDARD) {
                            Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        } else {
                            Modifier
                        },
                    )
                    .padding(4.dp),
            ) {
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val baseSurface = MaterialTheme.colorScheme.surface
                val indicatorBackgroundColor = if (isDark) {
                    androidx.compose.ui.graphics.lerp(baseSurface, Color.White, 0.16f)
                } else {
                    androidx.compose.ui.graphics.lerp(baseSurface, Color.Black, 0.08f)
                }

                BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                    val spacing = 4.dp
                    val itemWidth = (maxWidth - spacing * (items.size - 1)) / items.size

                    val indicatorBackground = if (blurEnabled) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { indicatorShape },
                            effects = {
                                blur(4f.dp.toPx())
                                lens(10f.dp.toPx(), 14f.dp.toPx(), chromaticAberration = true)
                            },
                            onDrawSurface = {
                                drawRect(indicatorBackgroundColor, blendMode = BlendMode.Hue)
                                drawRect(indicatorBackgroundColor.copy(alpha = 0.75f))
                            },
                        )
                    } else {
                        Modifier.background(indicatorBackgroundColor)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(itemWidth)
                            .graphicsLayer {
                                val spacingPx = spacing.toPx()
                                val itemWidthPx = itemWidth.toPx()
                                val offset = pagerState.currentPage + pagerState.currentPageOffsetFraction
                                translationX = offset * (itemWidthPx + spacingPx)
                            }
                            .clip(indicatorShape)
                            .then(indicatorBackground),
                    )
                }

                var lastTargetIndex by remember { mutableIntStateOf(pagerState.currentPage) }

                LaunchedEffect(pagerState.currentPage) {
                    if (!pagerState.isScrollInProgress) {
                        lastTargetIndex = pagerState.currentPage
                    }
                }

                Row(
                    modifier = Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { lastTargetIndex = pagerState.currentPage },
                            onDragCancel = { lastTargetIndex = pagerState.currentPage },
                            onHorizontalDrag = { change, _ ->
                                val x = change.position.x
                                val targetIndex = (x / (size.width.toFloat() / items.size)).toInt().coerceIn(0, items.size - 1)
                                if (targetIndex != lastTargetIndex) {
                                    lastTargetIndex = targetIndex
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(targetIndex)
                                    }
                                }
                            },
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = pagerState.currentPage == index

                        BottomNavItem(
                            item = item,
                            isSelected = isSelected,
                            navType = navType,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomNavItem(
        item: NavItem,
        isSelected: Boolean,
        navType: NavType,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val contentColor by animateColorAsState(
            targetValue = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            label = "ContentColorAnimation",
        )

        // MODERN grows the selected icon; LEGACY keeps every icon at its natural size.
        val iconScale by animateFloatAsState(
            targetValue = if (navType == NavType.MODERN && isSelected) 1.18f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            label = "IconScaleAnimation",
        )

        Box(
            modifier = modifier.hapticClickable(onClick = onClick, ripple = false),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Crossfade(
                    targetState = isSelected,
                    animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                    label = "Icon Crossfade Animation",
                ) {
                    Icon(
                        painter = painterResource(if (it) item.iconSelected else item.iconUnselected),
                        contentDescription = item.label,
                        tint = contentColor,
                        modifier = Modifier.graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                    )
                }

                Text(
                    text = item.label,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (navType == NavType.MODERN && isSelected) FontWeight.Bold else null,
                )
            }
        }
    }
}
