package com.orangeman.probemanapp.customview

import android.content.Context
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.orangeman.probemanapp.activity.domain.FaceDetectedCallback

/**
 * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
 * uses this factory to create face trackers as needed -- one for each individual.
 */
class GraphicFaceTrackerFactory(
    private val faceDetectedCallback: FaceDetectedCallback,
    private val context: Context,
    private val mGraphicOverlay: GraphicOverlay
) : MultiProcessor.Factory<Face> {

    override fun create(face: Face): Tracker<Face> {
        return GraphicFaceTracker(faceDetectedCallback, context, mGraphicOverlay)
    }

}