package com.gigforce.modules.feature_chat.screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatHeader
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject
import com.gigforce.modules.feature_chat.screens.vm.ChatHeadersViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [ChatHeadersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatHeadersFragment : Fragment() {

    val viewModel: ChatHeadersViewModel by viewModels<ChatHeadersViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

        }
    }

    private fun setObserver(owner:LifecycleOwner){
        Log.d("chat/header/fragment", "UserId "+ FirebaseAuth.getInstance().currentUser!!.uid)
        viewModel.chatHeaders.observe(owner, Observer {
            Log.d("chat/header/fragment", "Observer Triggered")
            this.setCollectionData(it)
        })

    }

    private fun setCollectionData(list:ArrayList<ChatHeader>){
        ChatListFragment@this.view?.findViewById<CoreRecyclerView>(R.id.rv_chat_headers)?.collection =
                ArrayList(list.map {
                    ChatListItemDataObject(
                            id = it.id,
                            title = it.otherUser?.name ?: "",
                            subtitle = it.lastMsgText,
                            timeDisplay = "2 min",
                            type = it.chatType,
                            profilePath = "",
                            unreadCount = it.unseenCount
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
        setObserver(this.viewLifecycleOwner)
        super.onViewCreated(view, savedInstanceState)
    }
}