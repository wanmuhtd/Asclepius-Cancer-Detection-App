package com.dicoding.asclepius.view.result

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.BuildConfig
import com.dicoding.asclepius.data.HistoryRepository
import com.dicoding.asclepius.data.Result
import com.dicoding.asclepius.data.local.entity.HistoryEntity
import com.dicoding.asclepius.data.remote.response.ArticlesItem
import com.dicoding.asclepius.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ResultViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    private val _newsResponse = MutableLiveData<Result<List<ArticlesItem>>>()
    val newsResponse = _newsResponse

    init {
        getNews()
    }

    private fun getNews() {
        viewModelScope.launch {
            try {
                _newsResponse.value = Result.Loading
                val response =
                    ApiConfig.getApiService().getNews("cancer", "health", "en", BuildConfig.API_KEY)
                if (response.status == "ok") {
                    val filteredArticles = response.articles.filter { article ->
                        article.title != "[Removed]" && article.description != "[Removed]" && article.content != "[Removed]"
                    }
                    _newsResponse.value = Result.Success(filteredArticles)
                }
            } catch (e: HttpException) {
                _newsResponse.value = Result.Error(e.message())
            }
        }
    }

    fun savePredictionHistory(imageUri: String, result: String, confidenceScore: Float) {
        val historyEntity = HistoryEntity(
            imageUri = imageUri,
            result = result,
            confidenceScore = confidenceScore
        )

        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.insertHistory(historyEntity)
        }
    }
}