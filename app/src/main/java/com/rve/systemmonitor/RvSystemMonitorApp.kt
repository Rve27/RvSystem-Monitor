package com.rve.systemmonitor

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.rve.systemmonitor.ui.components.AppBars.SimpleTopAppBar
import com.rve.systemmonitor.ui.navigation.BottomNavBar.BottomNavigationBar
import com.rve.systemmonitor.ui.screens.CPUScreen
import com.rve.systemmonitor.ui.screens.HomeScreen
import com.rve.systemmonitor.ui.screens.ProcessesScreen
import com.rve.systemmonitor.ui.screens.MemoryScreen
import kotlinx.coroutines.launch

@Composable
fun RvSystemMonitorApp(onNavigateToSettings: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    val pageHistory = remember { mutableStateListOf(0) }
    var isNavigatingBack by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        if (isNavigatingBack) {
            isNavigatingBack = false
        } else {
            val currentPage = pagerState.currentPage

            if (pageHistory.lastOrNull() != currentPage) {
                pageHistory.remove(currentPage)
                pageHistory.add(currentPage)
            }
        }
    }

    BackHandler(enabled = pageHistory.size > 1) {
        coroutineScope.launch {
            isNavigatingBack = true

            if (Build.VERSION.SDK_INT >= 35) {
                pageHistory.removeLast()
            } else {
                pageHistory.removeAt(pageHistory.lastIndex)
            }

            val previousPage = pageHistory.last()
            pagerState.animateScrollToPage(previousPage)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val backdropBackgroundColor = MaterialTheme.colorScheme.background
    val backdrop = rememberLayerBackdrop {
        drawRect(backdropBackgroundColor)
        drawContent()
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = "RvSystem Monitor",
                subtitle = when (pagerState.currentPage) {
                    0 -> "Home"
                    1 -> "CPU"
                    2 -> "Memory"
                    3 -> "Processes"
                    else -> ""
                },
                onNavigateToSettings = onNavigateToSettings,
            )
        },
        containerColor = backgroundColor,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(backgroundColor),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = 32.dp,
                    topEnd = 32.dp,
                ),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.layerBackdrop(backdrop),
                    beyondViewportPageCount = 0,
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(
                            isActive = pagerState.settledPage == 0,
                        )

                        1 -> CPUScreen(
                            isActive = pagerState.settledPage == 1,
                        )

                        2 -> MemoryScreen(
                            isActive = pagerState.settledPage == 2,
                        )

                        3 -> ProcessesScreen()
                    }
                }
            }

            BottomNavigationBar(
                pagerState = pagerState,
                coroutineScope = coroutineScope,
                backdrop = backdrop,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            )
        }
    }
}
