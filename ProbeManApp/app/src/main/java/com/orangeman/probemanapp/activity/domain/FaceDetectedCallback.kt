package com.orangeman.probemanapp.activity.domain

import android.graphics.Bitmap

interface FaceDetectedCallback {
    fun processImage(faceImage: Bitmap)
}