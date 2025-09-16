package com.yapp.fortune.scheduler

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.yapp.alarm.scheduler.PostFortuneTaskScheduler
import com.yapp.fortune.worker.PostFortuneWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerPostFortuneTaskScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : PostFortuneTaskScheduler {
    override fun enqueueOnceForToday() {
        val name = "post_fortune_${LocalDate.now()}"
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val req = OneTimeWorkRequestBuilder<PostFortuneWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(name, ExistingWorkPolicy.KEEP, req)
    }
}
