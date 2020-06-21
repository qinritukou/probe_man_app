package com.orangeman.probemanapp.util

object LabelUtil {

    fun getLabelMap(labels: List<String>, outputProbabilityBuffer: Array<FloatArray>): Map<String, Float> {
        val map = hashMapOf<String, Float>()
        for (i in labels.indices) {
            map[labels[i]] = outputProbabilityBuffer[0][i]
        }
        return map
    }

}