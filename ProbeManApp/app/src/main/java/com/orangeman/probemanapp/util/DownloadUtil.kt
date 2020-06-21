package com.orangeman.probemanapp.util

import android.content.Context
import com.orangeman.probemanapp.util.domain.ConstValues
import com.orangeman.probemanapp.util.domain.Logger
import java.io.File

object DownloadUtil {

    fun downloadModel(context: Context): Boolean {
        val localFile = File(DirectoryUtil.appRootFolder(context), ConstValues.ModelFileName)
        if (localFile.exists()) return true

        val obb = File(context.obbDir, ConstValues.ObbFileName)
        if (!obb.exists()) return false
        try {
            obb.copyTo(localFile)
        } catch (e: Exception) {
            LOGGER.e("${e.message}")
            return false
        }
        return true
    }

    val LOGGER = Logger()
}