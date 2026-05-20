package com.yanpai.clipboardcleaner

import android.os.Bundle
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
    private var clipboardListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clipboardListener = {
            homeViewModel.onClipboardChanged()
        }

        val systemIsDark = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        setContent {
            var isDarkTheme by remember { mutableStateOf(systemIsDark) }

            ClipboardCleanerTheme(darkTheme = isDarkTheme) {
                AppNavGraph(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        clipboardListener?.let {
            homeViewModel.clipboardMonitor.setOnChangeListener(it)
        }
        homeViewModel.onClipboardChanged()
        window.decorView.postDelayed({
            homeViewModel.onClipboardChanged()
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        clipboardListener?.let {
            homeViewModel.clipboardMonitor.removeChangeListener(it)
        }
    }
}
