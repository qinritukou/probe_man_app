package com.orangeman.probemanapp.tflite

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.os.Trace
import com.orangeman.probemanapp.customview.domain.Recognition
import com.orangeman.probemanapp.util.DirectoryUtil
import com.orangeman.probemanapp.util.LabelUtil
import com.orangeman.probemanapp.util.domain.ConstValues
import com.orangeman.probemanapp.util.domain.Logger
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap


/** This TensorFlowLite classifier works with the float EfficientNet model.  */
class ProbeManNet {
    /** Get the image size along the x axis.  */
    /** Image size along the x axis.  */
    val imageSizeX: Int

    /** Get the image size along the y axis.  */
    /** Image size along the y axis.  */
    val imageSizeY: Int

    /** Options for configuring the Interpreter.  */
    private val tfLiteOptions = Interpreter.Options()

    /** Labels corresponding to the output of the vision model.  */
    private val labelsMap = hashMapOf<Int, List<String>>()

    /** The loaded TensorFlow Lite model.  */
    private lateinit var tfLiteModel: MappedByteBuffer

    /** An instance of the driver class to run model inference with Tensorflow Lite.  */
    private lateinit var tfLite: Interpreter

    /** Input image TensorBuffer.  */
    private var inputImageBuffer: TensorImage

    /** Output probability TensorBuffer.  */
    private var outputProbabilityBufferMap: HashMap<Int, Any>

    /** Processer to apply post processing of the output probability.  */
    private val probabilityProcessor: TensorProcessor

    private fun loadMappedFile(context: Context): MappedByteBuffer {
        val inputStream = FileInputStream(File(DirectoryUtil.appRootFolder(context), ConstValues.ModelFileName))

        try {
            val fileChannel: FileChannel = inputStream.channel
            val result =  fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
            inputStream.close()
            return result
        } catch (var12: Throwable) {
            try {
                inputStream.close()
            } catch (var11: Throwable) {
                var12.addSuppressed(var11)
            }
            throw var12
        }
    }

    constructor(context: Context, numThreads: Int) {
        loadMappedFile(context)?.let {
            tfLiteModel = it
        }

        tfLiteOptions.setNumThreads(numThreads)
        tfLite = Interpreter(tfLiteModel, tfLiteOptions)

        // Loads labels out from the label file.
        for (i in 0 until ConstValues.LabelFileNames.size) {
            val labels = FileUtil.loadLabels(context, ConstValues.LabelFileNames[i])
            labelsMap[i] = labels
        }

        // Reads type and shape of input and output tensors, respectively.
        val imageTensorIndex = 0
        val imageShape = tfLite.getInputTensor(imageTensorIndex).shape()
        imageSizeY = imageShape[1]
        imageSizeX = imageShape[2]
        val imageDataType = tfLite.getInputTensor(imageTensorIndex).dataType()
        // Creates the input tensor.
        inputImageBuffer = TensorImage(imageDataType)
        outputProbabilityBufferMap = initOutputMap(tfLite)

        // Creates the post processor for the output probability.
        probabilityProcessor = TensorProcessor.Builder().add(postprocessNormalizeOp).build()
        LOGGER.d("Created a Tensorflow Lite Image Classifier.")
    }

    /**
     * Initializes an outputMap of 1 * x * y * z FloatArrays for the model processing to populate.
     */
    private fun initOutputMap(interpreter: Interpreter): HashMap<Int, Any> {
        val outputMap = HashMap<Int, Any>()
        for (i in 0 until interpreter!!.outputTensorCount) {
            val ot = interpreter.getOutputTensor(i).shape()
            outputMap[i] = Array(ot[0]) {
                FloatArray(ot[1])
            }
        }
        return outputMap
    }

    /** Runs inference and returns the classification results.  */
    fun recognizeImage(bitmap: Bitmap, sensorOrientation: Int): Map<Int, List<Recognition>> {
        val results = hashMapOf<Int, List<Recognition>>()

        // Logs this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage")
        Trace.beginSection("loadImage")
        val startTimeForLoadImage = SystemClock.uptimeMillis()
        inputImageBuffer = loadImage(bitmap, sensorOrientation)
        val endTimeForLoadImage = SystemClock.uptimeMillis()
        Trace.endSection()
        LOGGER.v("Timecost to load the image: " + (endTimeForLoadImage - startTimeForLoadImage))

        // Runs the inference call.
        Trace.beginSection("runInference")
        val startTimeForReference = SystemClock.uptimeMillis()
        tfLite.runForMultipleInputsOutputs(arrayOf(inputImageBuffer.buffer), outputProbabilityBufferMap)
        val endTimeForReference = SystemClock.uptimeMillis()
        Trace.endSection()
        LOGGER.v("Timecost to run model inference: " + (endTimeForReference - startTimeForReference))

        // Gets the map of label and probability.
        for ((i, outputProbabilityBuffer) in outputProbabilityBufferMap) {
            val labeledProbability = LabelUtil.getLabelMap(labelsMap[i]!!, outputProbabilityBuffer as Array<FloatArray>)
            // Gets top-k results.
            results[i] = getTopKProbability(labeledProbability!!)
        }
        Trace.endSection()

        return results
    }

    /** Closes the interpreter and model to release resources.  */
    fun close() {
        tfLite.close()
    }

    /** Loads input image, and applies preprocessing.  */
    private fun loadImage(bitmap: Bitmap, sensorOrientation: Int): TensorImage {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap)

        // Creates processor for the TensorImage.
        val cropSize = Math.min(bitmap.width, bitmap.height)
        val numRotation = sensorOrientation / 90
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(Rot90Op(numRotation))
            .add(preprocessNormalizeOp)
            .build()
        return imageProcessor.process(inputImageBuffer)
    }

    private val preprocessNormalizeOp = NormalizeOp(
        IMAGE_MEAN,
        IMAGE_STD
    )

    private val postprocessNormalizeOp = NormalizeOp(
        PROBABILITY_MEAN,
        PROBABILITY_STD
    )

    companion object {
        private val IMAGE_MEAN: Float = 127.0f
        private val IMAGE_STD: Float = 128.0f

        /**
         * Float model does not need dequantization in the post-processing. Setting mean and std as 0.0f
         * and 1.0f, repectively, to bypass the normalization.
         */
        private val PROBABILITY_MEAN: Float = 0.0f
        private val PROBABILITY_STD: Float = 1.0f

        private val LOGGER: Logger = Logger()

        /** Number of results to show in the UI.  */
        private const val MAX_RESULTS = 3

        /** Gets the top-k results.  */
        private fun getTopKProbability(labelProb: Map<String, Float>): List<Recognition> {
            // Find the best classifications.
            val pq = PriorityQueue(
                MAX_RESULTS,
                Comparator<Recognition> { lhs, rhs -> // Intentionally reversed to put high confidence at the head of the queue.
                    rhs.confidence.compareTo(lhs.confidence)
                })
            for ((key, value) in labelProb) {
                pq.add(
                    Recognition(
                        key,
                        value
                    )
                )
            }
            val recognitions = ArrayList<Recognition>()
            val recognitionsSize =
                Math.min(pq.size, MAX_RESULTS)
            for (i in 0 until recognitionsSize) {
                recognitions.add(pq.poll())
            }
            return recognitions
        }
    }

}
