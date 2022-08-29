package com.zebra.nilac.oeminfo_test

import android.content.Context
import android.telephony.TelephonyManager

object Utils {
    fun isSIMInserted(): Boolean {
        return TelephonyManager.SIM_STATE_ABSENT == (DefaultApplication.getInstance().applicationContext.getSystemService(
            Context.TELEPHONY_SERVICE
        ) as TelephonyManager).simState
    }
}