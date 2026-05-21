package com.yanpai.clipboardcleaner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_entries",
    indices = [Index(value = ["cleaned", "category"], unique = true)]
)
data class NoteEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "original") val originalText: String,
    @ColumnInfo(name = "cleaned") val cleanedText: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_read") val isRead: Boolean = false,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null  // null = 未删除
)
