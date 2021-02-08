package com.gigforce.modules.feature_chat.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var contactsButton: MaterialButton

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

        ChatListFragment@ this.view?.findViewById<CoreRecyclerView>(R.id.rv_chat_headers)?.collection =
                ArrayList(list.map {
                    ChatListItemDataObject(
                            id = it.id,
                            title = it.otherUser?.name ?: "",
                            subtitle = it.lastMsgText,
                            timeDisplay = "2 min",
                            type = it.chatType,
                            profilePath = "",
                            unreadCount = it.unseenCount,
                            profileId = it.otherUserId,
                            isOtherUserOnline = it.isOtherUserOnline,
                            groupName = it.groupName,
                            groupAvatar = it.groupAvatar,
                            lastMessage = it.lastMsgText,
                            lastMessageType = it.lastMessageType

                    )
                })

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        findViews(view)
        setObserver(this.viewLifecycleOwner)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun findViews(view: View) {
        contactsFab = view.findViewById(R.id.contactsFab)
        contactsFab.setOnClickListener {
            navigation.navigateToContactsPage()
        }

        noChatsLayout = view.findViewById(R.id.no_chat_layout)
        contactsButton = view.findViewById(R.id.contacts_btn)

        contactsButton.setOnClickListener {
            navigation.navigateToContactsPage()
        }
    }
}