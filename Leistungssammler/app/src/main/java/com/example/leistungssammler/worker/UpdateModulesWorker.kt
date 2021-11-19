package com.example.leistungssammler.worker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.leistungssammler.R
import com.example.leistungssammler.model.Module
import com.example.leistungssammler.persistence.AppDatabase
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient;
import okhttp3.Request

class UpdateModulesWorker(context: Context,
                          workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    val moduleDao = AppDatabase.getDb(context).moduleDao()

    @SuppressLint("ServiceCast")
    override suspend fun doWork(): Result {
        val notifyManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Channel name", NotificationManager.IMPORTANCE_LOW)
                .let { channel ->
                    notifyManager.createNotificationChannel(channel)
                }
        }

        val notifyBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.refresh_modules))
            .setSmallIcon(R.drawable.refresh_icon)
            .setContentText(applicationContext.getString(R.string.check_server_data))
            .setProgress(0, 0, true)
        notifyManager.notify(NOTIFICATION_ID, notifyBuilder.build())


        val httpClient = OkHttpClient.Builder().build()

        val req = Request.Builder().url(MODULES_URL).build()

        httpClient.newCall(req).execute().apply {
            if (networkResponse?.code == 200) {
                val modules = Json.decodeFromString<List<Module>>(body!!.string())
                moduleDao.deleteAll()
                moduleDao.persistAll(modules)
                notifyBuilder
                    .setContentText(applicationContext.getString(R.string.loaded_new_data_success))
                    .setProgress(0, 0, false)
                notifyManager.notify(NOTIFICATION_ID, notifyBuilder.build())
            } else {
                notifyManager.cancel(NOTIFICATION_ID)
            }
            return Result.success()
        }
    }

    companion object {
        private const val MODULES_URL = "https://ema-thm.github.io/modules.json"
        private const val CHANNEL_ID = "4711"
        private const val NOTIFICATION_ID = 42
    }
}