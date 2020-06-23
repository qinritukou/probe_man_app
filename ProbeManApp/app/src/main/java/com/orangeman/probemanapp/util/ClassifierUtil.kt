package com.orangeman.probemanapp.util

import android.content.Context
import com.orangeman.probemanapp.tflite.ProbeManNet
import com.orangeman.probemanapp.util.domain.Logger
import java.io.IOException

object ClassifierUtil {
    private var classifier: ProbeManNet? = null

    fun createClassifier(context: Context) {
        if (classifier != null) return
        try {
            val numThreads = 5
            classifier = ProbeManNet(context, numThreads)
            // Updates the input image size.
        } catch (e: IOException) {
            LOGGER.e(e, "Failed to create classifier.")
        }
    }

    fun getClassifier(context: Context): ProbeManNet? {
        if (classifier == null) {
            createClassifier(context)
        }
        return classifier
    }

    fun close() {
        if (classifier != null) {
            classifier!!.close()
        }
    }

    private val LOGGER = Logger()
}