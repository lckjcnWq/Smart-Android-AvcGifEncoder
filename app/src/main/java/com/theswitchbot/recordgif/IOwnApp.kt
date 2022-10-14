package com.theswitchbot.recordgif

import android.app.Application

class IOwnApp :Application() {

    companion object {
        lateinit var instance: IOwnApp
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}