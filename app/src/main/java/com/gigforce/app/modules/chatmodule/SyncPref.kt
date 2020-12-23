package com.gigforce.app.modules.chatmodule

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager


class SyncPref private constructor(
    context: Context
) {
    private val contactsSharedPref = PreferenceManager.getDefaultSharedPreferences(context)

    fun shouldSyncContacts(): Boolean {

        val lastSyncTime = contactsSharedPref.getLong(LAST_CONTACT_SYNC_TIME, 0L)
        return if (lastSyncTime == 0L) {
            true
        } else {
            Log.d(TAG,"last sync time : $lastSyncTime")
            Log.d(TAG,"Current sync time : ${System.currentTimeMillis()}")
            Log.d(TAG,"Diff : ${System.currentTimeMillis() - lastSyncTime}")
            Log.d(TAG,"Diff Res: ${System.currentTimeMillis() - lastSyncTime > TWO_MINUTES}")
            System.currentTimeMillis() - lastSyncTime > TWO_MINUTES
        }
    }

    fun setContactsAsSynced() {
        contactsSharedPref.edit { putLong(LAST_CONTACT_SYNC_TIME, System.currentTimeMillis()) }
    }

    companion object {
        const val LAST_CONTACT_SYNC_TIME = "last_contact_sync_time"
        const val TWO_MINUTES =  120 * 1000
        const val TAG = "SyncPref"

        private var syncPref: SyncPref? = null


        @Synchronized
        fun getInstance(context: Context): SyncPref {

            if (syncPref == null) {
                syncPref = SyncPref(context)
            }

            return syncPref!!
        }
    }
}