package com.gigforce.app.modules.chatmodule.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.service.FetchContactsService
import com.gigforce.app.modules.chatmodule.ui.adapters.ContactsRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.OnContactClickListener
import com.gigforce.app.modules.chatmodule.viewModels.ContactsViewModel
import com.gigforce.app.modules.gigerVerfication.selfieVideo.CaptureSelfieVideoFragment
import com.gigforce.app.utils.AppConstants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_chat_new_contact.*

/*
    /////////////////////////////////////////////////////////////////////////////////
 */
class ContactsFragment : BaseFragment(), OnContactClickListener, PopupMenu.OnMenuItemClickListener {



    private val TAG: String = "chats/new/contact"
    private val viewModel: ContactsViewModel by viewModels()

    private var mService: FetchContactsService? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as FetchContactsService.LocalBinder
            mService = binder.getService()
            LoaderManager.getInstance(this@ContactsFragment).apply {
                initLoader(0, null, mService!!)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.e(TAG, "onServiceDisconnected")
        }
    }

    private val contactsAdapter: ContactsRecyclerAdapter by lazy {
        ContactsRecyclerAdapter(initGlide()!!, this)
    }

    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }

    //Views
    private lateinit var contactRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_chat_new_contact, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        initViewModel()
       checkForPermissionElseSyncContacts()
    }

    private fun findViews(view: View) {
        contactRecyclerView = view.findViewById(R.id.rv_contactsList)
        contactRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        contactRecyclerView.adapter = contactsAdapter
        processing_contacts_progressbar.visible()

        askContactsPermission.setOnClickListener {

            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                data = uri
                startActivityForResult(this, REQUEST_CONTACTS_PERMISSION)
            }
        }

        imageView41.setOnClickListener {

            val popUp = PopupMenu(activity?.applicationContext, it)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_contact)
            popUp.show()
        }
    }

    private fun initViewModel() {
        viewModel.contacts.observe(viewLifecycleOwner, Observer {
            showContactsOnView(it)
        })
    }

    private fun showContactsOnView(it: List<ContactModel>?) {
        val contacts = it ?: return
        processing_contacts_progressbar.gone()
        contactsAdapter.setData(contacts)
    }

    private fun checkForPermissionElseSyncContacts() {
        // check for permissions

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(TAG, "Permission Required. Requesting Permission")
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_CONTACTS_PERMISSION
            )
        } else {
            bindSyncContactService()
        }
    }

    private fun bindSyncContactService() {
        Intent(this.context, FetchContactsService::class.java).also {
            Log.v(TAG, "Binding Service")
            requireActivity().bindService(it, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            var allPermsGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermsGranted = false
                    break
                }
            }

            if (allPermsGranted)
                bindSyncContactService()
            else
                showPermissionLayout()
        }
    }

    private fun showPermissionLayout() {
        contactsPermissionLayout.visible()
    }

    override fun contactClick(contact: ContactModel) {

        val bundle = Bundle()
        bundle.putString(
            AppConstants.IMAGE_URL,
            contact.imageUrl
        )
        bundle.putString(AppConstants.CONTACT_NAME, contact.name)
        bundle.putString("chatHeaderId", contact.headerId)
        bundle.putString("forUserId", currentUserId)
        bundle.putString("otherUserId", contact.uid)
        navigate(R.id.chatScreenFragment, bundle)
    }

    companion object {
        const val REQUEST_CONTACTS_PERMISSION = 101
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
      return when(item?.itemId) {
            R.id.action_referesh -> {
                checkForPermissionElseSyncContacts()
                true
            }
            else -> {
                showToast("Coming soon")
                false
            }
        }

    }
}