package com.gigforce.app.modules.chatmodule

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager


class SyncPref private constructor(
    context: Context
) {
    private val contactsSharedPref = PreferenceManager.getDefaultSharedPreferences(context)

    fun shouldSyncContacts(): Boolean {

        val lastSyncTime = contactsSharedPref.getLong(LAST_CONTACT_SYNC_TIME, 0L)
        if (lastSyncTime == 0L) {
            return true
        } else {
            return System.currentTimeMillis() - lastSyncTime > TWO_MINUTES
        }
    }

    fun setContactsAsSynced() {
        contactsSharedPref.edit { putLong(LAST_CONTACT_SYNC_TIME, System.currentTimeMillis()) }
    }

    companion object {
        const val LAST_CONTACT_SYNC_TIME = "last_contact_sync_time"
        const val TWO_MINUTES =  30 * 1000

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