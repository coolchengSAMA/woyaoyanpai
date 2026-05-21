package com.yanpai.clipboardcleaner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yanpai.clipboardcleaner.ui.navigation.AppNavGraph
import com.yanpai.clipboardcleaner.ui.theme.ClipboardCleanerTheme
import com.yanpai.clipboardcleaner.viewmodel.HomeViewModel
import com.yanpai.clipboardcleaner.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemIsDark = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val savedTheme = prefs.getBoolean("dark", systemIsDark)

        setContent {
            var isDarkTheme by remember { mutableStateOf(savedTheme) }

            // 监听 isDarkTheme 或 activePresetId 变化重建 ColorScheme
            val activePresetId by themeViewModel.activePresetId.collectAsState()
            val allPresets by themeViewModel.allPresets.collectAsState()
            val effectiveScheme = remember(isDarkTheme, activePresetId, allPresets) {
                themeViewModel.colorSchemeFor(isDarkTheme)
            }
            val bgConfig = remember(isDarkTheme, activePresetId, allPresets) {
                themeViewModel.backgroundConfig(isDarkTheme)
            }

            ClipboardCleanerTheme(
                colorScheme = effectiveScheme,
                backgroundImagePath = bgConfig.path,
                backgroundScaleMode = bgConfig.scaleMode,
                backgroundOffsetX = bgConfig.offsetX,
                backgroundOffsetY = bgConfig.offsetY,
                backgroundScale = bgConfig.scale
            ) {
                AppNavGraph(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = {
                        isDarkTheme = !isDarkTheme
                        prefs.edit().putBoolean("dark", isDarkTheme).apply()
                    },
                    themeViewModel = themeViewModel
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
