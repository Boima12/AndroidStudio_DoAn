package com.thbt.goodbyemoney

import android.app.Application
import android.content.Context
import io.sentry.Sentry

class MyApplication : Application() {

    companion object {
        private var instance: MyApplication? = null

        val context: Context
            get() = instance!!.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Sentry.init { options ->
            options.dsn = "https://2604b4733184aea7c982b3f9a9d94680@o4508269453639680.ingest.de.sentry.io/4508269485621328"
            options.tracesSampleRate = 1.0 // Tùy chọn để theo dõi hiệu suất, thay đổi nếu cần
        }
    }
}
