package com.orangeman.probemanapp.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orangeman.probemanapp.R
import com.orangeman.probemanapp.activity.adapter.ProfileListAdapter
import com.orangeman.probemanapp.db.repository.ProfileRepository
import com.orangeman.probemanapp.util.DownloadUtil
import com.orangeman.probemanapp.util.domain.ConstValues.TAKE_PICTURE_ACTIVITY_RESULT
import com.orangeman.probemanapp.util.domain.Logger

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val profileRepository = ProfileRepository(this)


    private lateinit var adView : AdView
    private lateinit var profileListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        adView = findViewById(R.id.adView)
        val request =
            AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // All emulators
                .build()
        adView.loadAd(request)

        findViewById<FloatingActionButton>(R.id.openTakePictureView).setOnClickListener(this)
        profileListView = findViewById<ListView>(R.id.profile_list_view)
        val profileList = profileRepository.list()
        val adapter = ProfileListAdapter(this, profileList)
        profileListView.adapter = adapter

        DownloadUtil.downloadModel(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_retry_download_model -> {
                val succeeded = DownloadUtil.downloadModel(this)
                val resultMsg = if (succeeded) {
                    R.string.download_model_succeeded_tip
                } else {
                    R.string.download_model_failed_tip
                }
                Toast.makeText(this, resultMsg, Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_about -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.dialog_about_title)
                    .setMessage(R.string.dialog_about_content)
                    .setPositiveButton(R.string.dialog_ok, DialogInterface.OnClickListener { _, _ -> })
                    .show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.openTakePictureView -> {
                val intent = Intent(this@MainActivity, ClassifierActivity::class.java)
                startActivityForResult(intent, TAKE_PICTURE_ACTIVITY_RESULT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PICTURE_ACTIVITY_RESULT -> {
                if (resultCode == RESULT_OK) {
                    val profileList = profileRepository.list()
                    val adapter = ProfileListAdapter(this, profileList)
                    profileListView.adapter = adapter
                }
            }
        }
    }

    companion object {
        val LOGGER = Logger()
    }

}