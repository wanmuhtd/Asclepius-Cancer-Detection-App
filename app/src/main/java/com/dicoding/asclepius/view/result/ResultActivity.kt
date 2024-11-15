package com.dicoding.asclepius.view.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.R
import com.dicoding.asclepius.adapter.NewsAdapter
import com.dicoding.asclepius.data.HistoryRepository
import com.dicoding.asclepius.data.Result
import com.dicoding.asclepius.data.local.room.AppDatabase
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.view.history.HistoryActivity

@Suppress("DEPRECATION")
class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var newsAdapter: NewsAdapter

    private val resultViewModel: ResultViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val historyRepository = HistoryRepository(database.historyDao())
        ResultViewModelFactory(historyRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        val predictionResult = intent.getStringExtra(EXTRA_RESULT)
        val confidenceScore = intent.getFloatExtra(EXTRA_CONFIDENCE_SCORE, 0.0f)

        resultViewModel.newsResponse.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    newsAdapter = NewsAdapter(result.data)
                    binding.rvNews.apply {
                        layoutManager = LinearLayoutManager(
                            this@ResultActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        adapter = newsAdapter
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
        }

        predictionResult?.let {
            Log.d("Result", "showResult: $it")
            binding.resultText.text = it
        }

        imageUri?.let { uri ->
            predictionResult?.let { predictionResult ->
                savePredictionHistory(uri.toString(), predictionResult, confidenceScore)
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    private fun savePredictionHistory(
        imageUri: String,
        predictionResult: String,
        confidenceScore: Float,
    ) {
        resultViewModel.savePredictionHistory(imageUri, predictionResult, confidenceScore)
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_CONFIDENCE_SCORE = "extra_confidence_score"
    }
}