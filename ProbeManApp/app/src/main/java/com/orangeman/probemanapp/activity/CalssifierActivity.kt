package com.orangeman.probemanapp.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.orangeman.probemanapp.R
import com.orangeman.probemanapp.customview.domain.Recognition
import com.orangeman.probemanapp.db.repository.ProfileRepository
import com.orangeman.probemanapp.db.repository.domain.Profile
import com.orangeman.probemanapp.tflite.ProbeManNet
import com.orangeman.probemanapp.util.ClassifierUtil
import com.orangeman.probemanapp.util.ImageUtil
import com.orangeman.probemanapp.util.domain.Logger
import java.io.IOException


class ClassifierActivity : CameraActivity() {
    private var recognitionMap: Map<Int, List<Recognition>>? = null

    private var lastProcessingTimeMs: Long = 0
    private var sensorOrientation: Int = 0
    private var isProcessing: Boolean = false

    private val profileRepository = ProfileRepository(this)

    override fun getLayoutId(): Int {
        return R.layout.camera_connection_fragment
    }

    override fun processImage(faceImage: Bitmap) {
        if (isProcessing) return
        runInBackground(Runnable {
            val classifier = ClassifierUtil.getClassifier(this)
            if (classifier != null) {
                val startTime = SystemClock.uptimeMillis()
                isProcessing = true
                recognitionMap = classifier!!.recognizeImage(faceImage, sensorOrientation)
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
                LOGGER.v("Detect: %s", recognitionMap)
                runOnUiThread {
                    showResultsInBottomSheet(recognitionMap!!)
                    isProcessing = false
                }
            }
        })
    }

    override fun takePicture() {
        if (recognitionMap == null) {
            Toast.makeText(this, R.string.iswaiting_for_recognition_tip, Toast.LENGTH_LONG).show()
            return
        }
        try {
            mCameraSource!!.takePicture(null,
                CameraSource.PictureCallback { data ->
                    try {
                        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                        val imageName = ImageUtil.writeToFile(this, bitmap)
                        val profile = Profile(imageName, recognitionMap!!)
                        profileRepository.save(profile)
                        val returnIntent = Intent()
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this, "${e}", Toast.LENGTH_LONG).show()
                    }
                })
        } catch (ex: Exception) {
            LOGGER.w("${ex.message}")
            Toast.makeText(this, R.string.camera_not_usable_tip, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val LOGGER: Logger = Logger()
    }
}
