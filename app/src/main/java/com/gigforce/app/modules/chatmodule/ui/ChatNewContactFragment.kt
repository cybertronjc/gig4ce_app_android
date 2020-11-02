package com.gigforce.app.modules.chatmodule.ui

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CursorAdapter
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.core.database.getStringOrNull
import androidx.fragment.app.activityViewModels
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.chatmodule.viewModels.ChatNewContactViewModel
import com.gigforce.app.modules.chatmodule.viewModels.FetchContactsService

/*
    /////////////////////////////////////////////////////////////////////////////////
 */
class ChatNewContactFragment: BaseFragment(),
        AdapterView.OnItemClickListener
{

    private val TAG:String = "chats/new/contact"

    private val viewModel: ChatNewContactViewModel by activityViewModels<ChatNewContactViewModel>()

    private var mService: FetchContactsService? = null

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as FetchContactsService.LocalBinder
            mService = binder.getService()
            loaderManager.initLoader(0, null, mService!!)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.e(TAG, "onServiceDisconnected")
        }
    }

    lateinit var contactsList: ListView

    var contactId: Long = 0
    var contactKey: String? = null
    var contactUri: Uri? = null

    // An adapter that binds the result Cursor to the ListView
    private var cursorAdapter: SimpleCursorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // extract args
        }

        // Initializes the loader
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_chat_new_contact, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Gets the ListView from the View list of the parent activity
        activity?.also {
            handleOnActivityCreated()
        }
    }

    private fun handleOnActivityCreated(){
        // check for permissions
        if(this.requireContext().checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED)
        {
            Log.v(TAG, "Permission Required. Requesting Permission")
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 1)
            return
        }

        Intent(this.context, FetchContactsService::class.java).also {
            Log.v(TAG, "Binding Service")
            requireActivity().bindService(it, mConnection, Context.BIND_AUTO_CREATE)
        }

        contactsList = requireActivity().findViewById(R.id.rv_contactsList)
        /*
        cursorAdapter = SimpleCursorAdapter(
                activity,
                R.layout.chat_new_contact_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0
        )*/

        //contactsList.adapter = cursorAdapter

        // Set the item click listener to be the current fragment.
        contactsList.onItemClickListener = this
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1) {
            handleOnActivityCreated()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Get the Cursor
        val cursor: Cursor? = (parent?.adapter as? CursorAdapter)?.cursor?.apply {
            // Move to the selected contact
            // moveToPosition(position)
            // Get the _ID value
            // contactId = getLong(CONTACT_ID_INDEX)
            // Get the selected LOOKUP KEY
            // contactKey = getString(CONTACT_KEY_INDEX)
            // Create the contact's content Uri
            // contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey)
            /*
             * You can use contactUri as the content URI for retrieving
             * the details for a contact.
             */
        }
    }

}