package com.dicoding.asclepius.view.main

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.dicoding.asclepius.R
import com.yalantis.ucrop.UCrop
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.view.result.ResultActivity
import com.dicoding.asclepius.view.history.HistoryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.currentImageUri?.let {
            currentImageUri = it
            showImage()
        }

        binding.apply {
            analyzeButton.setOnClickListener {
                currentImageUri?.let {
                    analyzeImage(it)
                } ?: showToast("No image selected")
            }
            galleryButton.setOnClickListener {
                startGallery()
            }
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

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.currentImageUri = uri
            UCrop.of(uri, Uri.fromFile(cacheDir.resolve("${System.currentTimeMillis()}.jpg")))
                .withMaxResultSize(2000, 2000)
                .start(this)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            currentImageUri = resultUri
            showImage()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e("Crop Error", "onActivityResult: $cropError")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
            binding.analyzeButton.visibility = android.view.View.VISIBLE
            binding.galleryButton.apply {
                text = resources.getString(R.string.change_image)
                setOnClickListener { startGallery() }
            }
        }
    }

    private fun analyzeImage(image: Uri) {
        val imageHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    showToast("Error: $error")
                }

                override fun onResults(result: List<Classifications>?) {
                    if (!result.isNullOrEmpty() && result[0].categories.isNotEmpty()) {
                        val category = result[0].categories[0]
                        val confidenceScore = category.score // Ambil score numerik asli
                        val resultString = "${category.label}: ${(confidenceScore * 100).toInt()}%"

                        lifecycleScope.launch(Dispatchers.IO) {
                            this@MainActivity.runOnUiThread {
                                moveToResult(image, resultString, confidenceScore)
                            }
                        }
                    }
                }
            }
        )
        imageHelper.classifyStaticImage(image)
    }

    private fun moveToResult(image: Uri, result: String, confidenceScore: Float) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, image.toString())
        intent.putExtra(ResultActivity.EXTRA_RESULT, result)
        intent.putExtra(ResultActivity.EXTRA_CONFIDENCE_SCORE, confidenceScore)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}