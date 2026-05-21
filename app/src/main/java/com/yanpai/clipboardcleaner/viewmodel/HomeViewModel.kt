package com.yanpai.clipboardcleaner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yanpai.clipboardcleaner.clipboard.ClipboardMonitor
import com.yanpai.clipboardcleaner.clipboard.ContentDetector
import com.yanpai.clipboardcleaner.clipboard.DetectResult
import com.yanpai.clipboardcleaner.data.AppDatabase
import com.yanpai.clipboardcleaner.data.NoteEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val clipboardText: String? = null,
    val originalText: String? = null,
    val detectResult: DetectResult? = null,
    val isRead: Boolean = false,
    val isSaved: Boolean = false,
    val noMatch: Boolean = false,
    val comicCount: Int = 0,
    val videoCount: Int = 0,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val noteDao = database.noteDao()
    val clipboardMonitor = ClipboardMonitor(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    // 防止写回剪贴板时触发自身监听器导致死循环
    private var lastCleanedText: String? = null

    init {
        loadCounts()
        readClipboard()
        autoCleanTrash()
    }

    private fun autoCleanTrash() {
        viewModelScope.launch {
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            noteDao.autoCleanTrash(sevenDaysAgo)
        }
    }

    /**
     * 读取当前剪贴板文本。
     */
    fun readClipboard() {
        val text = clipboardMonitor.readText()
        _uiState.value = _uiState.value.copy(
            clipboardText = text,
            detectResult = null,
            noMatch = false,
            isSaved = false
        )
    }

    /**
     * 检测剪贴板内容并清理。每次都实时读取系统剪贴板，确保拿到最新数据。
     */
    fun detectAndClean() {
        // 实时读取剪贴板，不依赖之前缓存的值
        val rawText = clipboardMonitor.readText()
        _uiState.value = _uiState.value.copy(clipboardText = rawText)

        if (rawText.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(noMatch = false, detectResult = null)
            return
        }

        // 防止写回剪贴板时触发自身监听器导致死循环
        if (rawText == lastCleanedText) return

        val result = ContentDetector.detect(rawText)
        if (result != null) {
            // 匹配成功：自动写回剪贴板，并记住本次结果防止死循环
            clipboardMonitor.writeText(result.cleaned)
            lastCleanedText = result.cleaned
            _uiState.value = _uiState.value.copy(
                detectResult = result,
                originalText = rawText,
                isRead = false,
                isSaved = false,
                noMatch = false,
                error = null
            )
            // 自动保存
            saveEntry(result, rawText)
        } else {
            _uiState.value = _uiState.value.copy(
                detectResult = null,
                noMatch = true,
                isSaved = false,
                error = null
            )
        }
    }

    /**
     * 切换已阅状态，同时更新数据库。
     */
    fun toggleRead() {
        val newRead = !_uiState.value.isRead
        _uiState.value = _uiState.value.copy(isRead = newRead)

        val result = _uiState.value.detectResult ?: return
        viewModelScope.launch {
            val existing = noteDao.findDuplicate(result.cleaned, result.category)
            if (existing != null && existing.isRead != newRead) {
                noteDao.toggleRead(existing.id)
            }
        }
    }

    /**
     * 保存/更新记录到数据库。
     */
    private fun saveEntry(result: DetectResult, rawText: String) {
        val isReadNow = _uiState.value.isRead
        viewModelScope.launch {
            try {
                val existing = noteDao.findDuplicate(result.cleaned, result.category)
                if (existing != null) {
                    // 活跃记录：更新日期
                    noteDao.updateDuplicate(
                        id = existing.id,
                        updatedAt = System.currentTimeMillis(),
                        original = rawText
                    )
                } else {
                    // 检查回收站中是否有相同内容，有则自动恢复
                    val trashed = noteDao.findDeletedDuplicate(result.cleaned, result.category)
                    if (trashed != null) {
                        noteDao.restore(trashed.id)
                        noteDao.updateDuplicate(
                            id = trashed.id,
                            updatedAt = System.currentTimeMillis(),
                            original = rawText
                        )
                    } else {
                        noteDao.insert(
                            NoteEntry(
                                originalText = rawText,
                                cleanedText = result.cleaned,
                                category = result.category,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                isRead = isReadNow
                            )
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(isSaved = true)
                loadCounts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "保存失败，请稍后再试")
            }
        }
    }

    private fun loadCounts() {
        viewModelScope.launch {
            noteDao.getCountByCategory("comic")
                .combine(noteDao.getCountByCategory("video")) { comic, video ->
                    _uiState.value = _uiState.value.copy(comicCount = comic, videoCount = video)
                }
                .collect {}
        }
    }

    /**
     * 剪贴板变化时自动调用。
     */
    fun onClipboardChanged() {
        detectAndClean()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
