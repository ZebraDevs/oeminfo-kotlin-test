package com.zebra.nilac.oeminfo_test

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.zebra.nilac.emdkloader.EMDKLoader
import com.zebra.nilac.emdkloader.ProfileLoader
import com.zebra.nilac.emdkloader.interfaces.EMDKManagerInitCallBack
import com.zebra.nilac.emdkloader.interfaces.ProfileLoaderResultCallback
import com.zebra.nilac.oeminfo_test.databinding.ActivityMainBinding
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var mBinder: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinder = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinder.root)

        setSupportActionBar(mBinder.toolbar)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.profilesProcessed.collect { id ->
                    if (!id.supported && id.uri == AppConstants.URI_IMEI) {
                        mBinder.infoContainer.imeiCode.text =
                            getString(R.string.imei_not_supported)
                        return@collect
                    }

                    Log.i(TAG, "Profile with Identifier: ${id.uri} was successfully processed")
                    retrieveOEMInfo(id)
                }
            }
        }

        initEMDKManager()
    }

    private fun initEMDKManager() {
        //Initialising EMDK Manager First...
        Log.i(TAG, "Initialising EMDK Manager")

        EMDKLoader.getInstance().initEMDKManager(this, object : EMDKManagerInitCallBack {
            override fun onFailed(message: String) {
                Log.e(TAG, "Failed to initialise EMDK Manager")
            }

            override fun onSuccess() {
                Log.i(TAG, "EMDK Manager was successfully initialised")

                mainViewModel.processProfiles()
            }
        })
    }

    private fun retrieveOEMInfo(identifier: Identifier) {
        contentResolver.query(
            identifier.uri.toUri(),
            arrayOf(
                identifier.column
            ),
            null,
            null
        ).use {
            if (it != null) {
                val columnIndexDisplayName =
                    it.getColumnIndexOrThrow(identifier.column)

                while (it.moveToNext()) {
                    Log.i(TAG, "Column Data: ${identifier.uri}")
                    val value = it.getString(columnIndexDisplayName)
                    fillUI(identifier, value)
                    break
                }
            }
        }
    }

    private fun fillUI(identifier: Identifier, value: String) {
        when (identifier.uri) {
            AppConstants.URI_SERIAL -> {
                mBinder.infoContainer.serialNumber.text = value
            }

            AppConstants.URI_BT_MAC -> {
                mBinder.infoContainer.macAddress.text = value
            }

            AppConstants.URI_IMEI -> {
                mBinder.infoContainer.imeiCode.text = value
            }

            AppConstants.URI_WIFI_MAC -> {
                mBinder.infoContainer.wifiMacAddress.text = value
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}