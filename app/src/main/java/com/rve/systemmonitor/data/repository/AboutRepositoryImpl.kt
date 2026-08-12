package com.rve.systemmonitor.data.repository

import com.rve.systemmonitor.domain.model.GitHubContributor
import com.rve.systemmonitor.domain.repository.AboutRepository
import javax.inject.Inject

class AboutRepositoryImpl @Inject constructor() : AboutRepository {

    override suspend fun getContributors(): Result<List<GitHubContributor>> {
        // Hardcoded contributors to avoid network calls to api.github.com
        // This prevents the "NonFreeNet" anti-feature on F-Droid
        val contributors = listOf(
            GitHubContributor(
                login = "Rve27",
                avatarUrl = "https://github.com/Rve27.png",
                htmlUrl = "https://github.com/Rve27",
                name = "Radika",
            ),
            GitHubContributor(
                login = "pavelc4",
                avatarUrl = "https://github.com/pavelc4.png",
                htmlUrl = "https://github.com/pavelc4",
                name = "Dimas Dwi Ariyanto",
            ),
            GitHubContributor(
                login = "Kugumin",
                avatarUrl = "https://github.com/Kugumin.png",
                htmlUrl = "https://github.com/Kugumin",
                name = "Key",
            ),
            GitHubContributor(
                login = "theovilardo",
                avatarUrl = "https://github.com/theovilardo.png",
                htmlUrl = "https://github.com/theovilardo",
                name = null,
            ),
            GitHubContributor(
                login = "chenlongapps",
                avatarUrl = "https://github.com/chenlongapps.png",
                htmlUrl = "https://github.com/chenlongapps",
                name = "陈龙",
            ),
        )
        return Result.success(contributors)
    }
}
