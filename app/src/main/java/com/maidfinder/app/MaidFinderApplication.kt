package com.maidfinder.app

import android.app.Application
import com.maidfinder.app.data.local.DataSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MaidFinderApplication : Application() {

    @Inject
    lateinit var dataSeeder: DataSeeder

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            dataSeeder.seedIfNeeded()
        }
    }
}
