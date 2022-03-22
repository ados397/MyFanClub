package com.ados.myfanclub

import android.app.Application
import androidx.lifecycle.*

class IsRunApp : Application(), LifecycleEventObserver {

    companion object{
        var isForeground =false
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                isForeground = false
            }
            Lifecycle.Event.ON_START -> {
                isForeground = true
            }
            Lifecycle.Event.ON_CREATE -> {

            }
            Lifecycle.Event.ON_RESUME -> {

            }
            Lifecycle.Event.ON_DESTROY -> {

            }
            Lifecycle.Event.ON_PAUSE -> {

            }
            Lifecycle.Event.ON_ANY -> {

            }
        }
    }
}