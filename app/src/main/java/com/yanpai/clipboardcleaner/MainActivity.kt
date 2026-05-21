package com.yanpai.clipboardcleaner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yanpai.clipboardcleaner.ui.navigation.AppNavGraph
import com.yanpai.clipboardcleaner.ui.theme.ClipboardCleanerTheme
import com.yanpai.clipboardcleaner.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemIsDark = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val savedTheme = prefs.getBoolean("dark", systemIsDark)

        setContent {
            var isDarkTheme by remember { mutableStateOf(savedTheme) }

            ClipboardCleanerTheme(darkTheme = isDarkTheme) {
                AppNavGraph(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = {
                        isDarkTheme = !isDarkTheme
                        prefs.edit().putBoolean("dark", isDarkTheme).apply()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 注册剪贴板监听（ClipboardMonitor 内部管理 SAM 实例防止泄漏）
        homeViewModel.clipboardMonitor.setOnChangeListener {
            homeViewModel.onClipboardChanged()
        }
        homeViewModel.onClipboardChanged()
        // 延迟再检测一次，确保焦点转移后能读到剪贴板
        Handler(Looper.getMainLooper()).postDelayed({
            homeViewModel.onClipboardChanged()
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        homeViewModel.clipboardMonitor.removeChangeListener()
    }
}
