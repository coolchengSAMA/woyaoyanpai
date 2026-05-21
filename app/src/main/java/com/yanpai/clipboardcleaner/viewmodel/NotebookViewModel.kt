package com.yanpai.clipboardcleaner.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yanpai.clipboardcleaner.data.AppDatabase
import com.yanpai.clipboardcleaner.data.NoteEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class NotebookUiState(
    val selectedTab: String = "comic",
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val isTrashMode: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val comicEntries: List<NoteEntry> = emptyList(),
    val videoEntries: List<NoteEntry> = emptyList(),
    val trashEntries: List<NoteEntry> = emptyList(),
    val comicCount: Int = 0,
    val videoCount: Int = 0,
    val totalCount: Int = 0,
    val trashTab: String = "comic",
    val toastMessage: String? = null
)

class NotebookViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val noteDao = database.noteDao()
    private val _uiState = MutableStateFlow(NotebookUiState())
    val uiState: StateFlow<NotebookUiState> = _uiState

    init {
        loadEntries("comic") { _uiState.value = _uiState.value.copy(comicEntries = it) }
        loadEntries("video") { _uiState.value = _uiState.value.copy(videoEntries = it) }
        loadTrash()
        loadCounts()
    }

    // ── 分类 ──

    fun selectTab(category: String) {
        _uiState.value = _uiState.value.copy(selectedTab = category, isTrashMode = false)
    }

    // ── 搜索 ──

    fun toggleSearch() {
        val now = !_uiState.value.isSearching
        _uiState.value = _uiState.value.copy(isSearching = now, searchQuery = "")
        if (!now) {
            loadEntries("comic") { _uiState.value = _uiState.value.copy(comicEntries = it) }
            loadEntries("video") { _uiState.value = _uiState.value.copy(videoEntries = it) }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        val cat = _uiState.value.selectedTab
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            loadEntries(cat) { onUpdate(cat, it) }
        } else {
            viewModelScope.launch {
                noteDao.searchByCategory(cat, trimmed).collect { onUpdate(cat, it) }
            }
        }
    }

    private fun onUpdate(cat: String, list: List<NoteEntry>) {
        val s = _uiState.value
        _uiState.value = if (cat == "comic") s.copy(comicEntries = list) else s.copy(videoEntries = list)
    }

    // ── 回收站 ──

    fun enterTrash() { _uiState.value = _uiState.value.copy(isTrashMode = true, trashTab = "comic") }
    fun leaveTrash() { _uiState.value = _uiState.value.copy(isTrashMode = false) }
    fun selectTrashTab(category: String) { _uiState.value = _uiState.value.copy(trashTab = category) }

    fun softDelete(entry: NoteEntry) {
        viewModelScope.launch {
            noteDao.softDelete(entry.id)
        }
    }

    fun restoreEntry(entry: NoteEntry) {
        viewModelScope.launch {
            noteDao.restore(entry.id)
        }
    }

    fun permanentDelete(entry: NoteEntry) {
        viewModelScope.launch {
            noteDao.permanentDelete(entry.id)
        }
    }

    fun clearTrash(category: String) {
        viewModelScope.launch {
            noteDao.clearTrashByCategory(category)
        }
    }

    // ── 置顶 ──

    fun togglePin(entry: NoteEntry) {
        viewModelScope.launch {
            noteDao.togglePin(entry.id)
        }
    }

    // ── 已阅 / 复制 ──

    fun toggleRead(entry: NoteEntry) {
        viewModelScope.launch {
            noteDao.toggleRead(entry.id)
        }
    }

    fun copyToClipboard(text: String) {
        val manager = getApplication<Application>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("note", text))
    }

    // ── 清空分类（软删除） ──

    fun clearCategory(category: String) {
        viewModelScope.launch {
            noteDao.softDeleteByCategory(category)
        }
    }

    // ── 批量操作 ──

    fun toggleSelection(id: Long) {
        val ids = _uiState.value.selectedIds.toMutableSet()
        if (ids.contains(id)) ids.remove(id) else ids.add(id)
        _uiState.value = _uiState.value.copy(selectedIds = ids)
    }

    fun enterSelectionMode(id: Long) {
        _uiState.value = _uiState.value.copy(isSelectionMode = true, selectedIds = setOf(id))
    }

    fun exitSelectionMode() {
        _uiState.value = _uiState.value.copy(isSelectionMode = false, selectedIds = emptySet())
    }

    fun batchMarkRead() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch { noteDao.batchMarkRead(ids) }
        exitSelectionMode()
    }

    fun batchDelete() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch { noteDao.batchSoftDelete(ids) }
        exitSelectionMode()
    }

    fun batchPermanentDelete() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { noteDao.permanentDelete(it) }
        }
        exitSelectionMode()
    }

    // ── 导出/导入 ──

    fun exportToJson(uri: Uri) {
        viewModelScope.launch {
            try {
                val entries = noteDao.getAllForExport()
                val jsonString = JSONArray().also { json ->
                    for (e in entries) {
                        json.put(JSONObject().apply {
                            put("original", e.originalText)
                            put("cleaned", e.cleanedText)
                            put("category", e.category)
                            put("created_at", e.createdAt)
                            put("updated_at", e.updatedAt)
                            put("is_read", if (e.isRead) 1 else 0)
                            put("is_pinned", if (e.isPinned) 1 else 0)
                            put("deleted_at", e.deletedAt ?: 0)
                        })
                    }
                }.toString(2)

                getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                    it.write(jsonString.toByteArray())
                }
                _uiState.value = _uiState.value.copy(toastMessage = "导出成功 (${entries.size} 条)")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(toastMessage = "导出失败：${e.message}")
            }
        }
    }

    fun importFromJson(uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                } ?: throw Exception("无法读取文件")
                val json = JSONArray(jsonString)
                val entries = (0 until json.length()).map { i ->
                    val obj = json.getJSONObject(i)
                    NoteEntry(
                        originalText = obj.getString("original"),
                        cleanedText = obj.getString("cleaned"),
                        category = obj.getString("category"),
                        createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updated_at", System.currentTimeMillis()),
                        isRead = obj.optInt("is_read", 0) == 1,
                        isPinned = obj.optInt("is_pinned", 0) == 1,
                        deletedAt = obj.optLong("deleted_at", 0).let { if (it == 0L) null else it }
                    )
                }
                noteDao.importEntries(entries)
                _uiState.value = _uiState.value.copy(toastMessage = "导入成功 (${entries.size} 条)")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(toastMessage = "导入失败：${e.message}")
            }
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    // ── 内部 ──

    private fun loadEntries(category: String, onUpdate: (List<NoteEntry>) -> Unit) {
        viewModelScope.launch {
            noteDao.getByCategory(category).collect { onUpdate(it) }
        }
    }

    private fun loadTrash() {
        viewModelScope.launch {
            noteDao.getTrash().collect {
                _uiState.value = _uiState.value.copy(trashEntries = it)
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
        viewModelScope.launch {
            noteDao.getTotalCount().collect {
                _uiState.value = _uiState.value.copy(totalCount = it)
            }
        }
    }
}
