package com.rve.systemmonitor.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.toPath
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.haptic.hapticClickable
import com.rve.systemmonitor.utils.NavMode
import com.rve.systemmonitor.utils.NavType
import kotlinx.coroutines.delay

const val MAX_NAV_BAR_CORNER_RADIUS = 32f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun androidx.graphics.shapes.RoundedPolygon.toShape(): Shape = object : Shape {
    private var cachedOutline: Outline? = null
    private var cachedSize: Size? = null

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size == cachedSize && cachedOutline != null) {
            return cachedOutline!!
        }
        
        val androidPath = toPath()
        val bounds = android.graphics.RectF()
        androidPath.computeBounds(bounds, true)
        
        val scaleX = size.width / bounds.width()
        val scaleY = size.height / bounds.height()
        val scale = minOf(scaleX, scaleY)
        
        val matrix = android.graphics.Matrix().apply {
            postTranslate(-bounds.left, -bounds.top)
            postScale(scale, scale)
            postTranslate((size.width - bounds.width() * scale) / 2f, (size.height - bounds.height() * scale) / 2f)
        }
        androidPath.transform(matrix)
        
        val outline = Outline.Generic(androidPath.asComposePath())
        cachedSize = size
        cachedOutline = outline
        return outline
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val TriangleShape = MaterialShapes.Triangle.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val PentagonShape = MaterialShapes.Pentagon.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val GemShape = MaterialShapes.Gem.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val DiamondShape = MaterialShapes.Diamond.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val VerySunnyShape = MaterialShapes.VerySunny.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val SunnyShape = MaterialShapes.Sunny.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val Cookie4Shape = MaterialShapes.Cookie4Sided.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val Cookie6Shape = MaterialShapes.Cookie6Sided.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val Cookie7Shape = MaterialShapes.Cookie7Sided.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val Cookie9Shape = MaterialShapes.Cookie9Sided.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val PillShape = MaterialShapes.Pill.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val OvalShape = MaterialShapes.Oval.toShape()
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val ArrowShape = MaterialShapes.Arrow.toShape()

@Composable
fun NavBarPreview(navMode: NavMode, navType: NavType, radius: Int) {
    val animatedRadius by animateFloatAsState(
        targetValue = radius.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NavBarPreviewRadius"
    )

    val bottomCornerTarget = if (navMode == NavMode.FLOATING) (animatedRadius / 2).dp else 0.dp
    val bottomCorner by animateDpAsState(targetValue = bottomCornerTarget, label = "NavBarPreviewBottomCorner")
    val topCorner = (animatedRadius / 2).dp

    val barShape = RoundedCornerShape(
        topStart = topCorner,
        topEnd = topCorner,
        bottomStart = bottomCorner,
        bottomEnd = bottomCorner
    )

    val indicatorShape = if (navType == NavType.MODERN) {
        RoundedCornerShape(8.dp)
    } else {
        RoundedCornerShape(((animatedRadius - 8f).coerceAtLeast(0f) / 2).dp)
    }

    val shapesList = remember {
        listOf(
            TriangleShape,
            PentagonShape,
            GemShape,
            DiamondShape,
            VerySunnyShape,
            SunnyShape,
            Cookie4Shape,
            Cookie6Shape,
            Cookie7Shape,
            Cookie9Shape,
            PillShape,
            OvalShape,
            ArrowShape
        )
    }

    var selectedIndex by remember { mutableIntStateOf(1) }
    var shapeCycleOffset by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            selectedIndex = (selectedIndex + 1) % 3
            shapeCycleOffset = (shapeCycleOffset + 1) % shapesList.size
        }
    }

    val indicatorOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "NavBarPreviewIndicatorOffset"
    )

    val animatedPaddingHorizontal by animateDpAsState(
        targetValue = if (navMode == NavMode.FLOATING) 12.dp else 0.dp,
        label = "NavBarPreviewPaddingHorizontal"
    )
    val animatedPaddingBottom by animateDpAsState(
        targetValue = if (navMode == NavMode.FLOATING) 8.dp else 0.dp,
        label = "NavBarPreviewPaddingBottom"
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (navMode == NavMode.FLOATING) 44.dp else 48.dp,
        label = "NavBarPreviewHeight"
    )

    val barScale = remember { Animatable(1f) }
    LaunchedEffect(navMode) {
        barScale.animateTo(
            targetValue = 1.06f,
            animationSpec = tween(120, easing = FastOutSlowInEasing)
        )
        barScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.BottomCenter,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(horizontal = animatedPaddingHorizontal)
                .padding(bottom = animatedPaddingBottom)
                .graphicsLayer {
                    scaleX = barScale.value
                    scaleY = barScale.value
                }
                .clip(barShape)
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .height(animatedHeight),
            contentAlignment = Alignment.CenterStart
        ) {
            val itemWidth = maxWidth / 3
            val innerPadding = 4.dp

            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val baseSurface = MaterialTheme.colorScheme.surface
            val indicatorBackgroundColor = remember(baseSurface, isDark) {
                if (isDark) {
                    androidx.compose.ui.graphics.lerp(baseSurface, Color.White, 0.16f)
                } else {
                    androidx.compose.ui.graphics.lerp(baseSurface, Color.Black, 0.08f)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(itemWidth)
                    .graphicsLayer {
                        translationX = indicatorOffset * itemWidth.toPx()
                    }
                    .padding(innerPadding)
                    .clip(indicatorShape)
                    .background(indicatorBackgroundColor)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = selectedIndex == index

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        animationSpec = tween(200),
                        label = "NavBarPreviewContentColor"
                    )

                    val shapeScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "NavBarPreviewShapeScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val shape = when (index) {
                            0 -> shapesList[(shapeCycleOffset + 0) % shapesList.size]
                            1 -> shapesList[(shapeCycleOffset + 1) % shapesList.size]
                            else -> shapesList[(shapeCycleOffset + 2) % shapesList.size]
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer {
                                    scaleX = shapeScale
                                    scaleY = shapeScale
                                }
                                .clip(shape)
                                .background(contentColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavPreviewCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preview: @Composable () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "NavPreviewBorder",
    )

    Card(
        modifier = modifier.hapticClickable(ripple = false, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            preview()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (selected) {
                    Icon(
                        painter = painterResource(R.drawable.check_rounded),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
