package com.dicoding.asclepius.view.history

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.adapter.HistoryAdapter
import com.dicoding.asclepius.data.HistoryRepository
import com.dicoding.asclepius.data.local.room.AppDatabase
import com.dicoding.asclepius.databinding.ActivityHistoryBinding


@Suppress("DEPRECATION")
class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val historyViewModel: HistoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = HistoryRepository(database.historyDao())
        HistoryViewModelFactory(repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }

        val historyAdapter = HistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }

        historyViewModel.historyList.observe(this) { history ->
            historyAdapter.submitList(history)
        }

        binding.btnClearHistory.setOnClickListener {
            historyViewModel.clearAllHistory()
        }
    }
}