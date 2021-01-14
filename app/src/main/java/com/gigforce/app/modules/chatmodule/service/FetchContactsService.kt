package com.gigforce.app.modules.chatmodule.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.gigforce.app.R
import com.gigforce.app.modules.chatmodule.SyncPref
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.repository.ChatContactsRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@SuppressLint("InlinedApi")
private val PROJECTION: Array<out String> = arrayOf(
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.LOOKUP_KEY,
    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
    ContactsContract.Contacts.HAS_PHONE_NUMBER,
    ContactsContract.CommonDataKinds.Phone.NUMBER
)

// Defines the text expression
@SuppressLint("InlinedApi")
private val SELECTION: String =
    "${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0 and ${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"

/*
 * Defines an array that contains resource ids for the layout views
 * that get the Cursor column contents. The id is pre-defined in
 * the Android framework, so it is prefaced with "android.R.id"
 */
private val TO_IDS: IntArray = intArrayOf(R.id.txt_contact_item)

// The column index for the _ID column
private const val CONTACT_ID_INDEX: Int = 0

// The column index for the CONTACT_KEY column
private const val CONTACT_KEY_INDEX: Int = 1

//////////////////////////////////////////////////
// SERVICE
//////////////////////////////////////////////////

class FetchContactsService : Service(), LoaderManager.LoaderCallbacks<Cursor> {

    private val TAG: String = "service/fetch/contacts"

    private val binder: LocalBinder = LocalBinder()

    // Defines a variable for the search string
    private val searchString: String = ""

    // Defines the array to hold values that replace the ?
    private val selectionArgs = arrayOf(searchString)

    private val chatContactsRepository: ChatContactsRepository by lazy {
        ChatContactsRepository(SyncPref.getInstance(applicationContext))
    }


    private val syncPref : SyncPref by lazy {
        SyncPref.getInstance(applicationContext)
    }
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {


        selectionArgs[0] = "%$searchString%"

        return CursorLoader(
            this,
            // Contacts.CONTENT_URI,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            SELECTION,
            selectionArgs,
            null
        )
    }

    private fun cleanPhoneNo(phone: String): String {
        var updated_phone = phone.replace("\\s|\t|[(]|[)]|[-]".toRegex(), "")
        if (updated_phone.startsWith('+')) {
            updated_phone = updated_phone.replace("[+]".toRegex(), "")
        } else {
            updated_phone = updated_phone.replace("^0".toRegex(), "")
            updated_phone = "91${updated_phone}"  // todo: CountryCode need to be generalised
        }
        return updated_phone
    }

    private fun processCursor(cursor: Cursor): List<ContactModel> {

        Log.v(TAG, "${cursor.count} Items Loaded!")

        cursor.moveToFirst()


        val contacts: ArrayList<ContactModel> = ArrayList<ContactModel>()

        //todo: Crash Error: CursorIndexOutOfBoundsException (If no Contacts)
        while (!cursor.isLast) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
            val phone =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val contactId = cursor.getString(cursor.getColumnIndex((ContactsContract.Contacts._ID)))

            contacts.add(
                ContactModel(
                    id = null,
                    mobile = cleanPhoneNo(phone),
                    name = name,
                    contactId = contactId
                )
            )
            cursor.moveToNext()
        }
        return contacts.distinctBy { it.mobile }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {

        if (syncPref.shouldSyncContacts()) {

            cursor?.let {
                val processCursor = processCursor(cursor)
                Log.d(TAG, "Count : ${processCursor.size}")
                Log.d(TAG, "Size : ${processCursor}")

                GlobalScope.launch {

                    try {
                        chatContactsRepository.updateContacts(processCursor)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        //todo: Handle this??
    }

    inner class LocalBinder : Binder() {
        fun getService(): FetchContactsService = this@FetchContactsService
    }
}