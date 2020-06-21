package com.orangeman.probemanapp.util

import android.content.Context
import com.orangeman.probemanapp.util.domain.Logger
import java.io.File

object DirectoryUtil {

    fun appRootFolder(context: Context): File? {
        val appRootFolder = context.getDir("pm", Context.MODE_PRIVATE)
        if (!appRootFolder.exists()) {
            val flag = appRootFolder.mkdirs()
            LOGGER.i("Create AppRootFolder : $flag")
        }
        return appRootFolder
    }

    private val LOGGER = Logger()
}
