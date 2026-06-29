package com.parentdashboard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.parentdashboard.domain.model.ChallengeType
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.repository.ChallengeRepository
import com.parentdashboard.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ChallengeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val challengeRepository: ChallengeRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "challenge_worker"

        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<ChallengeWorker>(24, TimeUnit.HOURS)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                    .build()
            )
        }
    }

    override suspend fun doWork(): Result = try {
        ensureChallenge(ChallengeType.DAILY, DateUtils.todayKey(), Difficulty.MEDIUM)
        ensureChallenge(ChallengeType.WEEKLY, DateUtils.weekKey(), Difficulty.HARD)
        ensureChallenge(ChallengeType.MONTHLY, DateUtils.monthKey(), Difficulty.EXPERT)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    private suspend fun ensureChallenge(type: ChallengeType, key: String, difficulty: Difficulty) {
        if (challengeRepository.getChallenge(type, key) == null) {
            challengeRepository.createChallenge(type, key, difficulty)
        }
    }
}
