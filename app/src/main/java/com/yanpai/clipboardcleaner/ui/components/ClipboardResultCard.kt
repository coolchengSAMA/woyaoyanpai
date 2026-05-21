package com.yanpai.clipboardcleaner.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yanpai.clipboardcleaner.clipboard.DetectResult
import com.yanpai.clipboardcleaner.ui.theme.ComicColor
import com.yanpai.clipboardcleaner.ui.theme.VideoColor

@Composable
fun ClipboardResultCard(
    result: DetectResult,
    originalText: String,
    isRead: Boolean,
    isSaved: Boolean,
    onToggleRead: () -> Unit
) {
    val categoryLabel = when (result.category) {
        "comic" -> "漫画"
        "video" -> "视频"
        else -> "其他"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorForCategory(result.category)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isSaved) {
                    Text(
                        text = "已保存",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "验牌后：${result.cleaned}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "原始：$originalText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isRead,
                    onCheckedChange = { onToggleRead() }
                )
                Text(
                    text = "标记为已阅",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun colorForCategory(category: String): Color = when (category) {
    "comic" -> ComicColor
    "video" -> VideoColor
    else -> MaterialTheme.colorScheme.primary
}
