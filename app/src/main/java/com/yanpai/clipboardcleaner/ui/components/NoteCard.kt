package com.yanpai.clipboardcleaner.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yanpai.clipboardcleaner.data.NoteEntry
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    entry: NoteEntry,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleRead: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelect: () -> Unit,
    isTrash: Boolean = false,
    onRestore: (() -> Unit)? = null
) {
    val displayTime = remember(entry.updatedAt) { formatTime(entry.updatedAt) }
    val cardColor = if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface
    val dimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSelect() },
                onLongClick = { if (!isSelectionMode && !isTrash) onLongPress() }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：多选勾选框 或 已阅勾选框
            val checkColors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary)
            if (isSelectionMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() }, colors = checkColors)
                Spacer(Modifier.width(4.dp))
            } else if (!isTrash) {
                Checkbox(checked = entry.isRead, onCheckedChange = { onToggleRead() }, colors = checkColors)
                Spacer(Modifier.width(4.dp))
            }

            // 中间：内容
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (entry.isPinned) {
                        Icon(Icons.Filled.PushPin, null, Modifier.height(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        entry.cleanedText,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (entry.isRead) dimColor else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    if (entry.isRead) {
                        Spacer(Modifier.width(6.dp))
                        Text("阅",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "原始: ${entry.originalText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = (if (entry.isRead) dimColor else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.6f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(displayTime, style = MaterialTheme.typography.labelMedium,
                    color = (if (entry.isRead) dimColor else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.5f))
            }

            // 右侧按钮
            if (!isSelectionMode) {
                if (isTrash && onRestore != null) {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Outlined.RestoreFromTrash, "恢复", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.DeleteOutline, "永久删除", tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    IconButton(onClick = onPin) {
                        Icon(if (entry.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, "置顶",
                            tint = if (entry.isPinned) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Outlined.ContentCopy, "复制", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.DeleteOutline, "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    return when {
        diff < 60_000L -> "刚刚"
        diff < 3_600_000L -> "${diff / 60_000} 分钟前"
        diff < 86_400_000L -> "${diff / 3_600_000} 小时前"
        diff < 172_800_000L -> "昨天 ${timeFormatter.format(dt)}"
        else -> dateFormatter.format(dt)
    }
}
