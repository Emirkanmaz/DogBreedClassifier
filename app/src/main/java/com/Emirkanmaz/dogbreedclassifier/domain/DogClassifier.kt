package com.Emirkanmaz.dogbreedclassifier.domain

import android.graphics.Bitmap

interface DogClassifier {
    fun classify(bitmap: Bitmap, rotation: Int): List<Classification>
}