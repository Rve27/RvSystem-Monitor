package com.rve.systemmonitor.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.rve.systemmonitor.BuildConfig
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.GitHubContributor
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.components.haptic.hapticClickable
import com.rve.systemmonitor.ui.viewmodel.AboutViewModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AboutScreen(viewModel: AboutViewModel = hiltViewModel(), onNavigateBack: () -> Unit) {
    val contributors by viewModel.contributors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    AboutScreenContent(
        contributors = contributors,
        isLoading = isLoading,
        error = error,
        onRetry = { viewModel.fetchContributors() },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutScreenContent(
    contributors: ImmutableList<GitHubContributor>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val openUrl = { url: String ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_no_browser), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExitUntilCollapsedMediumTopAppBar(
                title = stringResource(R.string.title_about),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 32.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                HeroCard()
            }
            item {
                Column {
                    Text(
                        text = stringResource(R.string.label_project_owner),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                    )
                    ProfileCard(
                        name = stringResource(R.string.about_owner_name),
                        role = stringResource(R.string.about_owner_role),
                        githubUsername = "Rve27",
                        onClick = { openUrl("https://github.com/Rve27") },
                    )
                }
            }

            item {
                Column {
                    Text(
                        text = stringResource(R.string.label_support),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                    )
                    SponsorCard(
                        onClick = { openUrl("https://ko-fi.com/rve27") },
                    )
                }
            }

            item {
                Column {
                    Text(
                        text = stringResource(R.string.label_contributors),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                    )

                    AnimatedContent(
                        targetState = Triple(isLoading, error, contributors),
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)).togetherWith(
                                fadeOut(animationSpec = tween(500)),
                            )
                        },
                        label = "ContributorsTransition",
                    ) { (loading, err, list) ->
                        when {
                            loading && list.isEmpty() -> {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(3) { index ->
                                        val shape = when (index) {
                                            0 -> RoundedCornerShape(
                                                topStart = 36.dp,
                                                topEnd = 36.dp,
                                                bottomStart = 4.dp,
                                                bottomEnd = 4.dp,
                                            )

                                            2 -> RoundedCornerShape(
                                                topStart = 4.dp,
                                                topEnd = 4.dp,
                                                bottomStart = 36.dp,
                                                bottomEnd = 36.dp,
                                            )

                                            else -> RoundedCornerShape(4.dp)
                                        }
                                        ContributorSkeleton(shape = shape)
                                    }
                                }
                            }

                            err != null && list.isEmpty() -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.globe_2_cancel_rounded),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp),
                                    )
                                    Text(
                                        text = stringResource(R.string.error_no_internet),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center,
                                    )
                                    Button(
                                        onClick = onRetry,
                                        shapes = ButtonDefaults.shapes(),
                                    ) {
                                        Text(stringResource(R.string.action_try_again))
                                    }
                                }
                            }

                            else -> {
                                val otherContributors = list.filter { it.login.lowercase() != "rve27" }

                                if (otherContributors.isEmpty() && !loading) {
                                    Text(
                                        text = stringResource(R.string.contributors_empty),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        otherContributors.forEachIndexed { index, contributor ->
                                            val shape = when {
                                                otherContributors.size == 1 -> CircleShape

                                                index == 0 -> RoundedCornerShape(
                                                    topStart = 36.dp,
                                                    topEnd = 36.dp,
                                                    bottomStart = 4.dp,
                                                    bottomEnd = 4.dp,
                                                )

                                                index == otherContributors.lastIndex -> RoundedCornerShape(
                                                    topStart = 4.dp,
                                                    topEnd = 4.dp,
                                                    bottomStart = 36.dp,
                                                    bottomEnd = 36.dp,
                                                )

                                                else -> RoundedCornerShape(4.dp)
                                            }

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = shape,
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                ),
                                            ) {
                                                ContributorRow(
                                                    contributor = contributor,
                                                    onClick = { openUrl(contributor.htmlUrl) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ContributorSkeleton(shape: RoundedCornerShape) {
    val transition = rememberInfiniteTransition(label = "SkeletonTransition")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "SkeletonAlpha",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.2f)),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.2f)),
                )
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.1f)),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroCard() {
    val badgeIds = remember {
        listOf(
            R.string.badge_free,
            R.string.badge_open_source,
            R.string.badge_rust,
            R.string.badge_kotlin,
            R.string.badge_material3_expressive,
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rvsystem_monitor),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.app_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = stringResource(R.string.about_app_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.8f),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                badgeIds.forEach { resId ->
                    val badge = stringResource(resId)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.tertiary)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(name: String, role: String, githubUsername: String, onClick: () -> Unit) {
    val avatarModel = remember(githubUsername) {
        when (githubUsername.lowercase()) {
            "rve27" -> R.drawable.avatar_rve27
            else -> "https://github.com/$githubUsername.png"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
    ) {
        Row(
            modifier = Modifier
                .hapticClickable(onClick = onClick)
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = avatarModel,
                contentDescription = stringResource(R.string.cd_avatar),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = githubUsername,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (name.isNotBlank()) {
                        Text(
                            text = " ($name)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun ContributorRow(contributor: GitHubContributor, onClick: () -> Unit) {
    val avatarModel = remember(contributor.login) {
        when (contributor.login.lowercase()) {
            "rve27" -> R.drawable.avatar_rve27
            "pavelc4" -> R.drawable.avatar_pavelc4
            "kugumin" -> R.drawable.avatar_kugumin
            "theovilardo" -> R.drawable.avatar_theovilardo
            "chenlongapps" -> R.drawable.avatar_chenlongapps
            else -> contributor.avatarUrl
        }
    }

    Row(
        modifier = Modifier
            .hapticClickable(onClick = onClick)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = avatarModel,
            contentDescription = contributor.login,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contributor.login,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!contributor.name.isNullOrBlank()) {
                    Text(
                        text = " (${contributor.name})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun SponsorCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .hapticClickable(onClick = onClick)
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.paid),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.about_sponsor_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = stringResource(R.string.about_sponsor_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}
