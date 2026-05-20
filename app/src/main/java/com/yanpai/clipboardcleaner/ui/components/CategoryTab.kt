package com.yanpai.clipboardcleaner.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class TabItem(
    val key: String,
    val label: String,
    val count: Int
)

@Composable
fun CategoryTabRow(
    tabs: List<TabItem>,
    selectedKey: String,
    onTabSelected: (String) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.key == selectedKey }.coerceAtLeast(0),
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        edgePadding = 16.dp
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab.key == selectedKey,
                onClick = { onTabSelected(tab.key) },
                text = {
                    Text(
                        text = "${tab.label}(${tab.count})",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
