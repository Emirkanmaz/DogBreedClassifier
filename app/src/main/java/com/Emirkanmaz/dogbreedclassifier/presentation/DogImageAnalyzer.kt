package com.Emirkanmaz.dogbreedclassifier.presentation

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.Emirkanmaz.dogbreedclassifier.domain.Classification
import com.Emirkanmaz.dogbreedclassifier.domain.DogClassifier

class DogImageAnalyzer(
    private val classifier: DogClassifier,
    private val onResults: (List<Classification>) -> Unit
): ImageAnalysis.Analyzer {

    private var frameSkipCounter = 0

    override fun analyze(image: ImageProxy) {
        if(frameSkipCounter % 60 == 0) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image
                .toBitmap()
                .centerCrop(224, 224)

            val results = classifier.classify(bitmap, rotationDegrees)
            onResults(results)
        }
        frameSkipCounter++

        image.close()
    }
}