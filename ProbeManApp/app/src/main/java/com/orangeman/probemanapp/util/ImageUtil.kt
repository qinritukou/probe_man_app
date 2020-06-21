package com.orangeman.probemanapp.util

import android.content.Context
import android.graphics.*
import com.orangeman.probemanapp.customview.FaceGraphic
import com.orangeman.probemanapp.util.domain.FacePositionInfo
import com.orangeman.probemanapp.util.domain.Logger
import java.io.*
import java.util.*
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

object ImageUtil {
    private val LOGGER = Logger()
    private val random = Random()

    fun getBitmap(faceGraphic: FaceGraphic, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        faceGraphic.draw(c)
        return bitmap
    }

    fun getBitmapPart(originalBitmap: Bitmap, facePositionInfo: FacePositionInfo): Bitmap {
        val bitmap = Bitmap.createBitmap(facePositionInfo.width, facePositionInfo.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(originalBitmap!!, facePositionInfo.x, facePositionInfo.y, paint)
        return bitmap
    }


    @Throws(IOException::class)
    fun writeToFile(context: Context, bitmap: Bitmap): String {
        val fileName = "${random.nextInt(Int.MAX_VALUE)}.jpg"
        val testData = File(DirectoryUtil.appRootFolder(context), fileName)
        val os: OutputStream = FileOutputStream(testData)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()
        return fileName
    }

    fun decodeFile(f: File?): Bitmap? {
        var b: Bitmap? = null
        try {
            // Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            var fis = FileInputStream(f)
            BitmapFactory.decodeStream(fis, null, o)
            fis.close()
            val IMAGE_MAX_SIZE = 1000
            var scale = 1
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = 2.0.pow(
                    (ln(
                        IMAGE_MAX_SIZE / o.outHeight.coerceAtLeast(o.outWidth).toDouble()
                    ) / ln(0.5)).roundToInt() as Double
                ).toInt()
            }

            // Decode with inSampleSize
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            fis = FileInputStream(f)
            b = BitmapFactory.decodeStream(fis, null, o2)
            fis.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return b
    }

}
