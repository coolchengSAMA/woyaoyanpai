package com.yanpai.clipboardcleaner

import android.app.Application
import com.yanpai.clipboardcleaner.data.AppDatabase

class YanPaiApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        // 预初始化数据库
        database
    }
}
