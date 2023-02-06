package com.example.whisper.di

import android.app.Application
import android.provider.UserDictionary.Words.APP_ID
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.whisper.utils.common.SENDBIRD_APP_ID
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import com.sendbird.android.handlers.InitResultHandler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class WhisperApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        SendBird.init(SENDBIRD_APP_ID, applicationContext, true, object : InitResultHandler {
            override fun onMigrationStarted() {
                Timber.tag("Application").i("Called when there's an update in Sendbird server.")
            }

            override fun onInitFailed(e: SendBirdException) {
                Timber.tag("Application")
                    .i("Called when initialize failed. SDK will still operate properly as if useLocalCaching is set to false.")
            }

            override fun onInitSucceed() {
                Timber.tag("Application").i("Called when initialization is completed.")
            }
        })
    }

    override fun getWorkManagerConfiguration() = Configuration
        .Builder()
        .setWorkerFactory(workerFactory)
        .build()
}
