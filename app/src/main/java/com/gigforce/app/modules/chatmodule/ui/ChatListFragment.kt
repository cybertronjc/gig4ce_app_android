package com.gigforce.app.modules.chatmodule.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.chatmodule.ChatConstants
import com.gigforce.app.modules.chatmodule.SyncPref
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.service.FetchContactsService
import com.gigforce.app.modules.chatmodule.ui.adapters.ChatListRecyclerAdapter
import com.gigforce.app.modules.chatmodule.viewModels.ChatHeadersViewModel
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.VerticalItemDecorator
import kotlinx.android.synthetic.main.contact_screen_fragment.*

/*
    This is supposed to be Chat Headers Screen
 */
class ChatListFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    ChatListRecyclerAdapter.OnChatItemClickListener {


    private val viewModel: ChatHeadersViewModel by viewModels()
    private val mAdapter: ChatListRecyclerAdapter by lazy {
        ChatListRecyclerAdapter(requireContext(), initGlide(R.drawable.ic_user,R.drawable.ic_user)!!, this)
    }

    private val syncPref: SyncPref by lazy {
        SyncPref.getInstance(requireContext())
    }
    private var mService: FetchContactsService? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as FetchContactsService.LocalBinder
            mService = binder.getService()
            LoaderManager.getInstance(this@ChatListFragment).apply {
                initLoader(0, null, mService!!)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.e(TAG, "onServiceDisconnected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.contact_screen_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        checkForPermissionElseSyncContacts()
    }

    fun initialize() {
        initRecycler()
        viewModel.chatHeaders.observe(viewLifecycleOwner, Observer {

            it?.let {
                mAdapter.setData(it)
            }

            it.forEach {
                Log.d("CHAT", it.toString())
                // viewModel.getChatMsgs(it.id)
            }

            no_chat_layout.isVisible = it.isEmpty()
        })
        attachListeners()
        viewModel.startWatchingChatHeaders()
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
                arrayOf(
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_CONTACTS_PERMISSION
            )
        } else {
            bindSyncContactService()
        }
    }

    private fun bindSyncContactService() {
        if (syncPref.shouldSyncContacts()) {

            Intent(this.context, FetchContactsService::class.java).also {
                Log.v(TAG, "Binding Service")
                requireActivity().bindService(it, mConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun attachListeners() {

        fab.setOnClickListener { view ->
            navigate(R.id.chatNewContactFragment)
        }

        contacts_btn.setOnClickListener {
            navigate(R.id.chatNewContactFragment)
        }
        iv_backArrow.setOnClickListener {
            showToast(getString(R.string.onback_operation))
        }
        iv_search.setOnClickListener {
            showToast(getString(R.string.search_operation))
        }
        back_arrow.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initRecycler() {
        rv_contacts.layoutManager = LinearLayoutManager(activity?.applicationContext)
        rv_contacts.addItemDecoration(VerticalItemDecorator(5))
        rv_contacts.adapter = mAdapter
    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_chat_contact, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_invite_friends -> {
                showToast(getString(R.string.invite_friends))
                true
            }
            R.id.action_referesh -> {
                showToast(getString(R.string.refresh))
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.mnu_invite_friend -> {
                navigate(R.id.referrals_fragment)
            }

        }
        return false
    }

    override fun onChatItemClicked(chatHeader: ChatHeader) {

        if (chatHeader.chatType == ChatConstants.CHAT_TYPE_USER) {

            val bundle = Bundle()
            bundle.putString(AppConstants.IMAGE_URL, chatHeader.otherUser?.profilePic)
            bundle.putString(AppConstants.CONTACT_NAME, chatHeader.otherUser?.name)
            bundle.putString(ChatFragment.INTENT_EXTRA_CHAT_HEADER_ID, chatHeader.id)
            bundle.putString(ChatFragment.INTENT_EXTRA_FOR_USER_ID, chatHeader.forUserId)
            bundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_ID, chatHeader.otherUserId)
            navigate(R.id.chatScreenFragment, bundle)
        } else if (chatHeader.chatType == ChatConstants.CHAT_TYPE_GROUP) {

            val bundle = Bundle()
            bundle.putString(GroupChatFragment.INTENT_EXTRA_GROUP_ID, chatHeader.groupId)
            navigate(R.id.groupChatFragment, bundle)
        }
    }

    companion object {
        const val TAG = "ChatListFragment"
        const val REQUEST_CONTACTS_PERMISSION = 101
    }
}
