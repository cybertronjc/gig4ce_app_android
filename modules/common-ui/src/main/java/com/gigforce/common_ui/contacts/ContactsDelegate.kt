package com.gigforce.common_ui.contacts

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.gigforce.core.datamodels.profile.Contact
import kotlin.Exception


class ContactsDelegate constructor(
    private val contentResolver: ContentResolver
){

    companion object {
        const val INTENT_PICK_ONE_CONTACT = 292
    }

    fun parseResults(
        uri : Uri,
        onSuccess : (contact : List<PhoneContact>) -> Unit,
        onFailure : (e : Exception) -> Unit
    ){

        val cursor = contentResolver.query(
            Phone.CONTENT_URI, null,
            Phone.CONTACT_ID + " = " + uri.lastPathSegment, null, null
        )

        if (cursor == null) {
            onFailure.invoke(Exception("got null cursor on querying for contact"))
            return
        }

        val contactList = mutableListOf<PhoneContact>()
        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    contactList.add(getContactDataFromCursor(c))
                } while (c.moveToNext())
            }
        }

        onSuccess.invoke(contactList)
    }


    private fun getContactDataFromCursor(c: Cursor): PhoneContact {

        val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
        val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
        val hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
        val thumbnailUri = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
        val fullSizeImageUri = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))

        val contactNumbers = mutableListOf<String>()

        val phoneIdx = c.getColumnIndex(Phone.DATA)
        if (c.moveToFirst()) {
            while (!c.isAfterLast) {
                val phoneNumber = c.getString(phoneIdx)
                addToContactList(contactNumbers, phoneNumber)
                c.moveToNext()
            }
        }

        return PhoneContact(
            name,
            contactNumbers
        )
    }

    private fun addToContactList(contactNumbers: MutableList<String>, phoneNumber: String?) {

        if (phoneNumber == null)
            return

        var trimmedPhoneNumber = phoneNumber.replace("\\s".toRegex(), "")

        if (trimmedPhoneNumber.length > 10) {

            if (trimmedPhoneNumber.contains("+91"))
                trimmedPhoneNumber = trimmedPhoneNumber.substring(trimmedPhoneNumber.indexOf("+") + 3)
            else if (trimmedPhoneNumber.startsWith("0"))
                trimmedPhoneNumber = trimmedPhoneNumber.substring(1)
        }

        if (!contactNumbers.contains(trimmedPhoneNumber))
            contactNumbers.add(trimmedPhoneNumber)
    }
}