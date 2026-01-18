package com.simple.notification.testing

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainApplication : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        lateinit var share: MainApplication
            private set

        private val _activityResumeFlow = MutableSharedFlow<Activity>(extraBufferCapacity = 1)
        val activityResumeFlow = _activityResumeFlow.asSharedFlow()
    }

    override fun onCreate() {
        super.onCreate()
        share = this
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        _activityResumeFlow.tryEmit(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
