package com.zebra.nilac.oeminfo_test

import android.app.Application
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.symbol.emdk.EMDKResults
import com.zebra.nilac.emdkloader.ProfileLoader
import com.zebra.nilac.emdkloader.interfaces.ProfileLoaderResultCallback
import com.zebra.nilac.emdkloader.utils.SignatureUtils
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val profilesProcessed = MutableLiveData<Identifier>()

    fun processProfiles() {
        //Process the profile for all 3 available identifiers
        val identifiers: ArrayList<Identifier> = arrayListOf<Identifier>().apply {
            add(
                Identifier(
                    AppConstants.URI_SERIAL,
                    AppConstants.CP_SERIAL_COLUMN_NAME
                )
            )
            add(
                Identifier(
                    AppConstants.URI_BT_MAC,
                    AppConstants.CP_MAC_COLUMN_NAME
                )
            )
            add(
                Identifier(
                    AppConstants.URI_WIFI_MAC,
                    AppConstants.CP_WIFI_COLUMN_NAME
                )
            )
            add(
                Identifier(
                    AppConstants.URI_IMEI,
                    AppConstants.CP_IMEI_COLUMN_NAME
                )
            )
        }

        viewModelScope.launch {
            processProfile(identifiers[0], object : ProcessProfileResult {
                override fun onProcessed(isProcessed: Boolean) {
                    if (isProcessed) {
                        profilesProcessed.postValue(identifiers[0])
                        processProfile(identifiers[1], object : ProcessProfileResult {
                            override fun onProcessed(isProcessed: Boolean) {
                                if (isProcessed) {
                                    profilesProcessed.postValue(identifiers[1])

                                    //Skip if the device doesn't have any inserted SIMs
//                                    if (!Utils.isSIMInserted()) {
//                                        profilesProcessed.postValue(identifiers[2].apply {
//                                            supported = false
//                                        })
//                                        return
//                                    }

                                    processProfile(identifiers[2], object : ProcessProfileResult {
                                        override fun onProcessed(isProcessed: Boolean) {
                                            if (isProcessed) {
                                                profilesProcessed.postValue(identifiers[2])
                                                processProfile(
                                                    identifiers[3],
                                                    object : ProcessProfileResult {
                                                        override fun onProcessed(isProcessed: Boolean) {
                                                            profilesProcessed.postValue(identifiers[3])
                                                        }
                                                    })
                                            }
                                        }
                                    })
                                }
                            }
                        })
                    }
                }
            })
        }
    }

    private fun processProfile(
        identifier: Identifier,
        processProfileResult: ProcessProfileResult
    ) {
        Log.i(
            TAG,
            "Processing AccessManager Profile with selected Identifier: ${identifier.uri}"
        )

        val profile = """
            <wap-provisioningdoc>
                <characteristic type="ProfileInfo">
                    <parm name="created_wizard_version" value="11.0.1" />
                </characteristic>
                <characteristic type="Profile">
                    <parm name="ProfileName" value="OEMService" />
                    <parm name="ModifiedDate" value="2022-08-17 10:20:36" />
                    <parm name="TargetSystemVersion" value="10.4" />

                    <characteristic type="AccessMgr" version="10.4">
                        <parm name="emdk_name" value="" />
                        <parm name="ServiceAccessAction" value="4" />
                        <parm name="ServiceIdentifier" value="${identifier.uri}" />
                        <parm name="CallerPackageName" value="com.zebra.nilac.oeminfo_test" />
                        <parm name="CallerSignature" 
                            value="${SignatureUtils.getAppSigningCertificate(application.applicationContext)}" />
                    </characteristic>
                </characteristic>
            </wap-provisioningdoc>"""

        ProfileLoader().processProfile(
            "OEMService",
            profile,
            object : ProfileLoaderResultCallback {
                override fun onProfileLoadFailed(errorObject: EMDKResults) {
                    //Nothing to see here..
                }

                override fun onProfileLoadFailed(message: String) {
                    Log.e(TAG, "Failed to process profile with identifier: ${identifier.uri}")
                    processProfileResult.onProcessed(false)
                }

                override fun onProfileLoaded() {
                    processProfileResult.onProcessed(true)
                }
            })
    }

    private interface ProcessProfileResult {
        fun onProcessed(isProcessed: Boolean)
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}