package com.orangeman.probemanapp.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.orangeman.probemanapp.R
import com.orangeman.probemanapp.util.domain.FacePositionInfo


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(
    val context: Context,
    overlay: GraphicOverlay
) : GraphicOverlay.Graphic(overlay) {
    private val mBoxPaint = Paint()
    private val mIdPaint = Paint()
    private var mFacePositionInfo: FacePositionInfo? = null

    fun updateFacePositionInfo(facePositionInfo: FacePositionInfo) {
        mFacePositionInfo = facePositionInfo
        postInvalidate()
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        if (mFacePositionInfo == null) return
        val fpi = mFacePositionInfo!!
        if (fpi.width >= 150 && fpi.height >= 150) {
            if ((fpi.width < 224 || fpi.height < 224)) {
                canvas.drawText(context.resources.getString(R.string.go_ahead_tip), fpi.x + 20, fpi.y - 10, mIdPaint)
            }

            val left = fpi.x
            val top = fpi.y
            val right = fpi.x + fpi.width
            val bottom = fpi.y + fpi.height
            canvas.drawRect(left, top, right, bottom, mBoxPaint)
        }
    }

    init {
        mIdPaint.color = Color.WHITE
        mIdPaint.textSize = 30.0F

        mBoxPaint.color = Color.WHITE
        mBoxPaint.style = Paint.Style.STROKE
        mBoxPaint.strokeWidth = 3.0F
    }

}
