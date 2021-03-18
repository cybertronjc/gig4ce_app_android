package com.gigforce.modules.feature_chat.screens

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.models.ChatHeader
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject
import com.gigforce.modules.feature_chat.screens.vm.ChatHeadersViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [ChatHeadersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatHeadersFragment : Fragment() {

    @Inject
    lateinit var navigation: IChatNavigation
    private val viewModel: ChatHeadersViewModel by viewModels()

    private lateinit var contactsFab: FloatingActionButton
    private lateinit var noChatsLayout: View
    private lateinit var contactsButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var coreRecyclerView: CoreRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (this.requireContext().applicationContext as ChatModuleProvider)
                .provideChatModule()
                .inject(this)
        navigation.context = requireContext()
    }

    private fun setObserver(owner: LifecycleOwner) {
        Log.d("chat/header/fragment", "UserId " + FirebaseAuth.getInstance().currentUser!!.uid)
        viewModel.chatHeaders.observe(owner, Observer {
            Log.d("chat/header/fragment", "Observer Triggered")
            Log.d("chat/header/fragment", it.toString())
            this.setCollectionData(ArrayList(it))
        })

    }

    private fun setCollectionData(list: ArrayList<ChatHeader>) {
        noChatsLayout.isVisible = list.isEmpty()

        coreRecyclerView.collection =
                ArrayList(list.map {

                    var timeToDisplayText = ""
                    it.lastMsgTimestamp?.let {
                        val chatDate = it.toDate()
                        timeToDisplayText = if (DateUtils.isToday(chatDate.time)) SimpleDateFormat("hh:mm aa").format(chatDate) else SimpleDateFormat("dd MMM").format(chatDate)
                    }

                    ChatListItemDataObject(
                            id = it.id,
                            title = it.otherUser?.name ?: "",
                            subtitle = it.lastMsgText,
                            timeDisplay = timeToDisplayText,
                            type = it.chatType,
                            profilePath = it.otherUser?.profilePic ?: "",
                            unreadCount = it.unseenCount,
                            profileId = it.otherUserId,
                            isOtherUserOnline = it.isOtherUserOnline,
                            groupName = it.groupName,
                            groupAvatar = it.groupAvatar,
                            lastMessage = it.lastMsgText,
                            lastMessageType = it.lastMessageType,
                            lastMsgFlowType = it.lastMsgFlowType,
                            chatType = it.chatType,
                            status = it.status,
                            senderName = it.senderName
                    )
                })

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        cancelAnyNotificationIfShown()
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    private fun cancelAnyNotificationIfShown() {
        val mNotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(67)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        findViews(view)
        initListeners()
        setObserver(this.viewLifecycleOwner)

        if (!isStoragePermissionGranted())
            askForStoragePermission()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun findViews(view: View) {
        contactsFab = view.findViewById(R.id.contactsFab)
        contactsFab.setOnClickListener {
            navigation.navigateToContactsPage()
        }

        coreRecyclerView = view.findViewById(R.id.rv_chat_headers)
        noChatsLayout = view.findViewById(R.id.no_chat_layout)
        contactsButton = view.findViewById(R.id.go_to_contacts_btn)
        toolbar = view.findViewById(R.id.toolbar)

        contactsButton.isEnabled = true
        contactsButton.setOnClickListener {
            navigation.navigateToContactsPage()
        }

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initListeners() {

        val searchMenuItem = toolbar.menu.findItem(R.id.action_search)
        val searchView: SearchView = searchMenuItem.actionView as SearchView

        val v = searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
        v.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {

                if (!searchView.isIconified) {
                    searchView.isIconified = true;
                }
                searchMenuItem.collapseActionView();
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText.isNullOrBlank()) {
                    coreRecyclerView.resetFilter()
                } else {
                    coreRecyclerView.filter {

                        val item = it as ChatListItemDataObject
                        item.groupName.contains(
                                newText, true
                        ) || item.title.contains(
                                newText, true
                        ) || item.subtitle.contains(
                                newText, true
                        )

                    }
                }
                return false
            }
        }
        )

    }

    private fun askForStoragePermission() {
        Log.v(ChatPageFragment.TAG, "Permission Required. Requesting Permission")
        requestPermissions(
                arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA
                ),
                23
        )
    }

    private fun isStoragePermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}