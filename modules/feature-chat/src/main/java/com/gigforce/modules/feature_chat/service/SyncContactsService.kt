package com.gigforce.modules.feature_chat.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Binder
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SyncContactsService : Service(), Loader.OnLoadCompleteListener<Cursor> {

    private val binder: LocalBinder = LocalBinder()


    // Defines the array to hold values that replace the ?


    private val chatContactsRepository: ChatContactsRepository by lazy {
        ChatContactsRepository(SyncPref.getInstance(applicationContext))
    }

    private val syncPref: SyncPref by lazy {
        SyncPref.getInstance(applicationContext)
    }

    private var isCurrentlySyncingContacts = false
    private var shouldCallSyncAPI = false

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(isCurrentlySyncingContacts){
           return START_NOT_STICKY
        }

        isCurrentlySyncingContacts = true
        shouldCallSyncAPI = intent?.getBooleanExtra(SHOULD_CALL_SYNC_API,false) ?: false
        Log.d(TAG,"Started Syncing Contacts....")

        if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_CONTACTS
                )
                != PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(TAG, "READ_CONTACTS Permission not granted, exiting...")
            isCurrentlySyncingContacts = false
            return START_NOT_STICKY
        }

        val cursorLoader =  CursorLoader(
                this,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                SELECTION,
                SELECTION_ARGS,
                null
        )
        cursorLoader.registerListener(CONTACTS_LOADER_ID,this)
        cursorLoader.startLoading()

        return START_REDELIVER_INTENT
    }

    private fun cleanPhoneNo(phone: String): String {
        var updatedPhoneNumber = phone.replace("\\s|\t|[(]|[)]|[-]".toRegex(), "")
        if (updatedPhoneNumber.startsWith('+')) {
            updatedPhoneNumber = updatedPhoneNumber.replace("[+]".toRegex(), "")
        } else {
            updatedPhoneNumber = updatedPhoneNumber.replace("^0".toRegex(), "")
            updatedPhoneNumber = "91${updatedPhoneNumber}"
        }
        return updatedPhoneNumber
    }

    private fun mapCursorToContacts(cursor: Cursor): List<ContactModel> {
        Log.v(TAG, "${cursor.count} Items Loaded!")

        if (cursor.count == 0) {
            Log.v(TAG, "No Contacts Found in Phone, returning empty list")
            isCurrentlySyncingContacts = false
            return emptyList()
        }


        val contacts: ArrayList<ContactModel> = ArrayList()
        while (cursor.moveToNext()) {
            val name = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            )
            val phone = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            )
            val contactId = cursor.getString(
                    cursor.getColumnIndex((ContactsContract.Contacts._ID))
            )

            if(phone != null) {

                contacts.add(
                    ContactModel(
                        id = null,
                        mobile = cleanPhoneNo(phone),
                        name = name,
                        contactId = contactId
                    )
                )
            }
        }

        return contacts
    }

    override fun onLoadComplete(loader: Loader<Cursor>, cursor: Cursor?) {

        cursor?.let {
            val contactsList = mapCursorToContacts(cursor)
            val distinctContactList = contactsList.distinctBy { it.mobile }

            Log.d(TAG, "Contact Size after removing duplicate contacts : ${distinctContactList.size}")
            GlobalScope.launch {
                try {
                    chatContactsRepository.updateContacts(
                        distinctContactList,
                        shouldCallSyncAPI
                        )

                    Log.e(TAG, "Contacts Synced")
                    isCurrentlySyncingContacts = false
                } catch (e: Exception) {
                    isCurrentlySyncingContacts = false
                    Log.e(TAG, "Error while syncing Contacts", e)

                    FirebaseCrashlytics.getInstance().apply {
                        log("Error While Syncing Contacts")
                        recordException(e)
                    }
                }
            }
        }
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        isCurrentlySyncingContacts = false
    }


    inner class LocalBinder : Binder() {
        fun getService(): SyncContactsService = this@SyncContactsService
    }

    companion object{
        private const val TAG: String = "service/fetch/contacts"
        private const val CONTACTS_LOADER_ID = 2

        const val SHOULD_CALL_SYNC_API = "call_sync_api"

        private val PROJECTION: Array<out String> = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        private const val SEARCH_STRING = ""
        private val SELECTION_ARGS = arrayOf("%$SEARCH_STRING%")

        private const val SELECTION: String = "${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0 and ${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
    }


}