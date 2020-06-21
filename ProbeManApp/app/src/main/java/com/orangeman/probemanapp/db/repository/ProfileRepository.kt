package com.orangeman.probemanapp.db.repository

import android.content.Context
import com.google.gson.Gson
import com.orangeman.probemanapp.db.repository.domain.Profile
import com.orangeman.probemanapp.util.domain.Logger
import java.io.*

class ProfileRepository(
    val context: Context
) {

    fun resave(profileList: ArrayList<Profile>) {
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput(ProfileFileName, Context.MODE_PRIVATE))
            profileList.forEach {
                outputStreamWriter.write("${Gson().toJson(it)}\n")
            }
            outputStreamWriter.close()
        } catch (e: IOException) {
            LOGGER.e("Exception", "File write failed: $e")
        }
    }

    fun save(profile: Profile) {
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput(ProfileFileName, Context.MODE_APPEND))
            outputStreamWriter.write("${Gson().toJson(profile)}\n")
            outputStreamWriter.close()
        } catch (e: IOException) {
            LOGGER.e("Exception", "File write failed: $e")
        }
    }

    fun list(): ArrayList<Profile> {
        val results = arrayListOf<Profile>()
        try {
            val inputStream = context.openFileInput(ProfileFileName)
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var line: String? = ""
                while (bufferedReader.readLine().also { line = it } != null) {
                    try {
                        val profile = Gson().fromJson(line, Profile::class.java)
                        results.add(profile)
                    } catch (e: Exception) {
                        LOGGER.e("Error: $e")
                    }
                }
                inputStream.close()
            }
        } catch (e: FileNotFoundException) {
            LOGGER.e("File not found: $e")
        } catch (e: IOException) {
            LOGGER.e("Can not read file: $e")
        }

        return results
    }

    companion object {
        val LOGGER = Logger()
        const val ProfileFileName = "profile.txt"
    }
}