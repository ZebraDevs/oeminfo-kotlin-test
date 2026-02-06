package com.zebra.nilac.oeminfo_test

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.symbol.emdk.EMDKResults
import com.zebra.nilac.emdkloader.ProfileLoader
import com.zebra.nilac.emdkloader.interfaces.ProfileLoaderResultCallback
import com.zebra.nilac.emdkloader.utils.SignatureUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _profilesProcessed = MutableSharedFlow<Identifier>(extraBufferCapacity = 16)
    val profilesProcessed: SharedFlow<Identifier> = _profilesProcessed.asSharedFlow()

    private val callerSignature: String by lazy {
        SignatureUtils.getAppSigningCertificate(application.applicationContext)
    }

    fun processProfiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val identifiers = buildIdentifiers()

            for (identifier in identifiers) {
                if (identifier.uri == AppConstants.URI_IMEI && !Utils.isSIMInserted()) {
                    _profilesProcessed.tryEmit(identifier.copy(supported = false))
                    continue
                }

                val ok = processProfile(identifier)
                if (ok) {
                    _profilesProcessed.tryEmit(identifier)
                }
            }
        }
    }

    private fun buildIdentifiers(): List<Identifier> = listOf(
        Identifier(AppConstants.URI_SERIAL, AppConstants.CP_SERIAL_COLUMN_NAME),
        Identifier(AppConstants.URI_BT_MAC, AppConstants.CP_MAC_COLUMN_NAME),
        Identifier(AppConstants.URI_WIFI_MAC, AppConstants.CP_WIFI_COLUMN_NAME),
        Identifier(AppConstants.URI_IMEI, AppConstants.CP_IMEI_COLUMN_NAME),
    )

    private suspend fun processProfile(identifier: Identifier): Boolean {
        Log.i(TAG, "Processing AccessManager Profile with selected Identifier: ${identifier.uri}")

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
                    <parm name="CallerSignature" value="$callerSignature" />
                </characteristic>
            </characteristic>
        </wap-provisioningdoc>
    """.trimIndent()

        return ProfileLoader().processProfile(
            profileName = "OEMService",
            profileXml = profile
        ).also { ok ->
            if (!ok) Log.e(TAG, "Failed to process profile with identifier: ${identifier.uri}")
        }
    }

    private suspend fun ProfileLoader.processProfile(
        profileName: String,
        profileXml: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        processProfile(
            profileName,
            profileXml,
            object : ProfileLoaderResultCallback {
                override fun onProfileLoadFailed(errorObject: EMDKResults) {
                    if (cont.isActive) cont.resume(false)
                }

                override fun onProfileLoadFailed(message: String) {
                    if (cont.isActive) cont.resume(false)
                }

                override fun onProfileLoaded() {
                    if (cont.isActive) cont.resume(true)
                }
            }
        )
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}