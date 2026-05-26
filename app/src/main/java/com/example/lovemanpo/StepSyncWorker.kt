package com.example.lovemanpo

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StepSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // --- サービスの生存確認と再起動 ---
        // OSによってサービスが殺されていた場合、ここで叩き起こす
        val serviceIntent = Intent(applicationContext, StepCounterService::class.java)
        applicationContext.startForegroundService(serviceIntent)

        return Result.success()
    }
}
