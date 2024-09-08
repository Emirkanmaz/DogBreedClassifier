package com.Emirkanmaz.dogbreedclassifier.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import com.Emirkanmaz.dogbreedclassifier.domain.Classification
import com.Emirkanmaz.dogbreedclassifier.domain.DogClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class TfLiteDogClassifier(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 3
) : DogClassifier {

    private var classifier: ImageClassifier? = null

    private fun setupClassifier() {
        val baseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResults)
            .setScoreThreshold(threshold)
            .build()

        try {
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "model_with_metadata.tflite",
                options
            )
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun loadLabels(context: Context): List<String> {
        val labels = mutableListOf<String>()
        context.assets.open("labels.txt").bufferedReader().useLines { lines ->
            labels.addAll(lines)
        }
        return labels
    }

    override fun classify(bitmap: Bitmap, rotation: Int): List<Classification> {
        if (classifier == null) {
            setupClassifier()
        }

        val labels = loadLabels(context)

        val imageProcessor = ImageProcessor.Builder()
            .build()
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        val results = classifier?.classify(tensorImage, imageProcessingOptions)

        results?.forEach { classification ->
            classification.categories.forEach { category ->
                Log.i("DogBreedClassifier", "Index: ${category.index}, Score: ${category.score}")
            }
        }

        return results?.flatMap { classifications ->
            classifications.categories.map { category ->
                Classification(
                    name = labels.getOrNull(category.index) ?: "",  // Map index to label
                    score = category.score
                )
            }
        }?.sortedByDescending { it.score }  // Sort by score in descending order
            ?.take(3)  // Take the top 3 results
            ?: emptyList()
    }


    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when(rotation) {
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }
}