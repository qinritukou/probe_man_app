package com.orangeman.probemanapp.customview


import android.content.Context
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.orangeman.probemanapp.activity.domain.FaceDetectedCallback
import com.orangeman.probemanapp.util.ImageUtil
import com.orangeman.probemanapp.util.domain.ConstValues
import com.orangeman.probemanapp.util.domain.FacePositionInfo


/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
class GraphicFaceTracker internal constructor(
    private val faceDetectedCallback: FaceDetectedCallback,
    private val context: Context,
    private val overlay: GraphicOverlay
) : Tracker<Face>() {
    private val mFaceGraphic = FaceGraphic(context, overlay)

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    override fun onNewItem(faceId: Int, item: Face) {
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    override fun onUpdate(detectionResults: Detections<Face>, face: Face) {
        overlay.add(mFaceGraphic)

        val facePositionInfo = FacePositionInfo(
            face.position.x,
            face.position.y,
            face.width.toInt() + ConstValues.FaceMargin,
            face.height.toInt() + ConstValues.FaceMargin
        )
        mFaceGraphic.updateFacePositionInfo(facePositionInfo)
        if (facePositionInfo.width >= 224 || facePositionInfo.height >= 224) {
            val bitmap = ImageUtil.getBitmap(mFaceGraphic, overlay.width, overlay.height)
            val faceImage = ImageUtil.getBitmapPart(bitmap, facePositionInfo)
            faceDetectedCallback.processImage(faceImage)
        }
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    override fun onMissing(detectionResults: Detections<Face>) {
        overlay.remove(mFaceGraphic)
    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    override fun onDone() {
        overlay.remove(mFaceGraphic)
    }

}