package com.dicoding.asclepius.view.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.data.HistoryRepository
import com.dicoding.asclepius.data.local.entity.HistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {
    private val _historyList = MutableLiveData<List<HistoryEntity>>()
    val historyList: LiveData<List<HistoryEntity>> get() = _historyList

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val history = repository.getAllHistory()
            _historyList.postValue(history)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory()
            loadHistory()
        }
    }
}