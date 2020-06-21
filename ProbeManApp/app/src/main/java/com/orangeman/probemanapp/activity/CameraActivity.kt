package com.orangeman.probemanapp.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.*
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.orangeman.probemanapp.R
import com.orangeman.probemanapp.activity.domain.FaceDetectedCallback
import com.orangeman.probemanapp.activity.domain.MyBottomSheetCallback
import com.orangeman.probemanapp.customview.CameraSourcePreview
import com.orangeman.probemanapp.customview.GraphicFaceTrackerFactory
import com.orangeman.probemanapp.customview.GraphicOverlay
import com.orangeman.probemanapp.customview.domain.Recognition
import com.orangeman.probemanapp.util.domain.ConstValues
import com.orangeman.probemanapp.util.domain.Logger
import java.io.IOException


abstract class CameraActivity : AppCompatActivity(), View.OnClickListener, OnGlobalLayoutListener, FaceDetectedCallback {
    private lateinit var mPreview: CameraSourcePreview
    private lateinit var mGraphicOverlay: GraphicOverlay
    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var gestureLayout: LinearLayout
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var takePictureImageButton: ImageButton
    private lateinit var bottomSheetArrowImageView: ImageView

    private lateinit var salaryValueTextView: TextView
    private lateinit var salaryPercentageTextView: TextView
    private lateinit var marriageWillnessValueTextView: TextView
    private lateinit var marriageWillnessPercentageTextView: TextView
    private lateinit var familyInfoValueTextView: TextView
    private lateinit var familyInfoPercentageTextView: TextView
    private lateinit var graduationValueTextView: TextView
    private lateinit var graduationPercentageTextView: TextView
    private lateinit var drinkValueTextView: TextView
    private lateinit var drinkPercentageTextView: TextView
    private lateinit var smokeValueTextView: TextView
    private lateinit var smokePercentageTextView: TextView

    private var previewWidth = 0
    private var previewHeight = 0
    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null
    protected var mCameraSource: CameraSource? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        LOGGER.d("onCreate $this")
        super.onCreate(null)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera)

        mPreview = findViewById(R.id.preview)
        mGraphicOverlay = findViewById(R.id.faceOverlay)
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout)
        gestureLayout = findViewById(R.id.gesture_layout)
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow)
        val vto = gestureLayout.viewTreeObserver
        vto.addOnGlobalLayoutListener(this)
        sheetBehavior.isHideable = false
        sheetBehavior.setBottomSheetCallback(MyBottomSheetCallback(bottomSheetArrowImageView))
        takePictureImageButton = findViewById(R.id.take_picture)
        takePictureImageButton.setOnClickListener(this)

        // salary
        salaryValueTextView = findViewById(R.id.salary_value_textview)
        salaryPercentageTextView = findViewById(R.id.salary_percentage_textview)

        // marriageWillness
        marriageWillnessValueTextView = findViewById(R.id.marriage_willness_value_textview)
        marriageWillnessPercentageTextView = findViewById(R.id.marriage_willness_percentage_textview)

        // familyInfo
        familyInfoValueTextView = findViewById(R.id.family_info_value_textview)
        familyInfoPercentageTextView = findViewById(R.id.family_info_percentage_textview)

        // graduation
        graduationValueTextView = findViewById(R.id.graduation_value_textview)
        graduationPercentageTextView = findViewById(R.id.graduation_percentage_textview)

        // drink
        drinkValueTextView = findViewById(R.id.drink_value_textview)
        drinkPercentageTextView = findViewById(R.id.drink_percentage_textview)

        // smoke
        smokeValueTextView = findViewById(R.id.smoke_value_textview)
        smokePercentageTextView = findViewById(R.id.smoke_percentage_textview)


        if (hasPermission()) {
            createCameraSource()
        } else {
            requestPermission()
        }
    }

    protected fun runInBackground(r: Runnable?) {
        if (handler != null) {
            handler!!.post(r)
        }
    }

    @Synchronized
    public override fun onResume() {
        LOGGER.d("onResume $this")
        super.onResume()

        startCameraSource()
        handlerThread = HandlerThread("inference")
        handlerThread!!.start()
        handler = Handler(handlerThread!!.looper)
    }

    @Synchronized
    public override fun onPause() {
        LOGGER.d("onPause $this")
        super.onPause()

        handlerThread!!.quitSafely()
        try {
            handlerThread!!.join()
            handlerThread = null
            handler = null
        } catch (e: InterruptedException) {
            LOGGER.e(e, "Exception!")
        }
        mPreview.stop()
    }

    public override fun onDestroy() {
        LOGGER.d("onDestroy $this")
        super.onDestroy()

        if (mCameraSource != null) {
            mCameraSource!!.release()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST) {
            if (allPermissionsGranted(grantResults)) {
                createCameraSource()
            } else {
                requestPermission()
            }
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(
                    this@CameraActivity,
                    R.string.permission_camera_rationale,
                    Toast.LENGTH_LONG
                ).show()
            }
            requestPermissions(
                arrayOf(PERMISSION_CAMERA),
                PERMISSIONS_REQUEST
            )
        }
    }

    // Returns true if the device supports the required hardware level, or better.
    private fun isHardwareLevelSupported(
        characteristics: CameraCharacteristics, requiredLevel: Int
    ): Boolean {
        val deviceLevel =
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)!!
        return if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            requiredLevel == deviceLevel
        } else requiredLevel <= deviceLevel
        // deviceLevel is not LEGACY, can use numerical sort
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private fun createCameraSource() {
        val context = this
        val detector = FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build()
        detector.setProcessor(
            MultiProcessor.Builder<Face>(mGraphicOverlay?.let {
                GraphicFaceTrackerFactory(context, context, it)
            }).build()
        )
        if (!detector.isOperational) {
            LOGGER.w("Face detector dependencies are not yet available.")
        }
        val displayMetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(displayMetrics)
        this.previewWidth = displayMetrics.widthPixels
        this.previewHeight = displayMetrics.heightPixels
        mCameraSource = CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(previewWidth, previewHeight)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedFps(30.0f)
            .build()
    }

    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(
                this,
                code,
                RC_HANDLE_GMS
            )
            dlg.show()
        }
        if (mCameraSource != null) {
            try {
                mPreview!!.start(mCameraSource, mGraphicOverlay!!)
            } catch (e: IOException) {
                e.message?.let { LOGGER.e(it) }
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    @UiThread
    protected fun showResultsInBottomSheet(recognitionMap: Map<Int, List<Recognition>>) {
        val salaryRecognition = recognitionMap[ConstValues.LabelIndex.Salary.value]?.get(0)
        if (salaryRecognition != null) {
            salaryValueTextView.text = salaryRecognition.title
            salaryPercentageTextView.text = String.format("%.1f%%", salaryRecognition.confidence * 100.0f)
        }

        val marriageWillnessRecognition = recognitionMap[ConstValues.LabelIndex.MarriageWillness.value]?.get(0)
        if (marriageWillnessRecognition != null) {
            marriageWillnessValueTextView.text = marriageWillnessRecognition.title
            marriageWillnessPercentageTextView.text = String.format("%.1f%%", marriageWillnessRecognition.confidence * 100.0f)
        }

        val familyInfoRecognition = recognitionMap[ConstValues.LabelIndex.FamilyInfo.value]?.get(0)
        if (familyInfoRecognition != null) {
            familyInfoValueTextView.text = familyInfoRecognition.title
            familyInfoPercentageTextView.text = String.format("%.1f%%", familyInfoRecognition.confidence * 100.0f)
        }

        val graduationRecognition = recognitionMap[ConstValues.LabelIndex.Graduation.value]?.get(0)
        if (graduationRecognition != null) {
            graduationValueTextView.text = graduationRecognition.title
            graduationPercentageTextView.text = String.format("%.1f%%", graduationRecognition.confidence * 100.0f)
        }

        val drinkRecognition = recognitionMap[ConstValues.LabelIndex.Drink.value]?.get(0)
        if (drinkRecognition != null) {
            drinkValueTextView.text = drinkRecognition.title
            drinkPercentageTextView.text = String.format("%.1f%%", drinkRecognition.confidence * 100.0f)
        }

        val smokeRecognition = recognitionMap[ConstValues.LabelIndex.Smoke.value]?.get(0)
        if (smokeRecognition != null) {
            smokeValueTextView.text = smokeRecognition.title
            smokePercentageTextView.text = String.format("%.1f%%", smokeRecognition.confidence * 100.0f)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.take_picture -> {
                takePicture()
            }
        }
    }

    abstract fun takePicture()

    override fun onGlobalLayout() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            gestureLayout.viewTreeObserver.removeGlobalOnLayoutListener(this)
        } else {
            gestureLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
        //                int width = bottomSheetLayout.getMeasuredWidth();
        val height = gestureLayout.measuredHeight
        sheetBehavior.peekHeight = height
    }

    private fun allPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    protected abstract fun getLayoutId(): Int
    abstract override fun processImage(faceImage: Bitmap)

    companion object {
        private val LOGGER: Logger = Logger()
        private const val PERMISSIONS_REQUEST = 1
        private const val RC_HANDLE_GMS = 9001
        private const val PERMISSION_CAMERA = Manifest.permission.CAMERA
    }

}
