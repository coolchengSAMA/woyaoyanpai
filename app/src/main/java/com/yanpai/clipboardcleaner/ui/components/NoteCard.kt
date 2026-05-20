package com.yanpai.clipboardcleaner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yanpai.clipboardcleaner.data.NoteEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 复用对象，避免每次 recompose 都创建
private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
fun NoteCard(
    entry: NoteEntry,
    onToggleRead: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val isRead = entry.isRead
    val displayTime = remember(entry.updatedAt) { formatTime(entry.updatedAt) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isRead,
                onCheckedChange = { onToggleRead() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.cleanedText,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "原始: ${entry.originalText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = (if (isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = displayTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = (if (isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "复制",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L -> "刚刚"
        diff < 3_600_000L -> "${diff / 60_000} 分钟前"
        diff < 86_400_000L -> "${diff / 3_600_000} 小时前"
        diff < 172_800_000L -> "昨天 ${timeFormatter.format(Date(timestamp))}"
        else -> dateFormatter.format(Date(timestamp))
    }
}
