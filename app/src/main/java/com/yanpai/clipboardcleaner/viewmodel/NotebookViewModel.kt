package com.yanpai.clipboardcleaner.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yanpai.clipboardcleaner.data.AppDatabase
import com.yanpai.clipboardcleaner.data.NoteEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class NotebookUiState(
    val selectedTab: String = "comic",
    val comicEntries: List<NoteEntry> = emptyList(),
    val videoEntries: List<NoteEntry> = emptyList(),
    val comicCount: Int = 0,
    val videoCount: Int = 0
)

class NotebookViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val noteDao = database.noteDao()

    private val _uiState = MutableStateFlow(NotebookUiState())
    val uiState: StateFlow<NotebookUiState> = _uiState

    init {
        // 两边数据始终同时加载，滑动时直接取缓存
        loadEntries("comic") { entries -> _uiState.value = _uiState.value.copy(comicEntries = entries) }
        loadEntries("video") { entries -> _uiState.value = _uiState.value.copy(videoEntries = entries) }
        loadCounts()
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

    fun selectTab(category: String) {
        _uiState.value = _uiState.value.copy(selectedTab = category)
    }

    private fun loadEntries(category: String, onUpdate: (List<NoteEntry>) -> Unit) {
        viewModelScope.launch {
            noteDao.getByCategory(category).collect { entries ->
                onUpdate(entries)
            }
        }
    }

    fun toggleRead(entry: NoteEntry) {
        viewModelScope.launch {
            try { noteDao.toggleRead(entry.id) } catch (_: Exception) {}
        }
    }

    fun deleteEntry(entry: NoteEntry) {
        viewModelScope.launch {
            try { noteDao.delete(entry) } catch (_: Exception) {}
        }
    }

    fun clearCategory(category: String) {
        viewModelScope.launch {
            try { noteDao.deleteByCategory(category) } catch (_: Exception) {}
        }
    }

    fun copyToClipboard(text: String) {
        val manager = getApplication<Application>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("note", text)
        manager.setPrimaryClip(clip)
    }
}
