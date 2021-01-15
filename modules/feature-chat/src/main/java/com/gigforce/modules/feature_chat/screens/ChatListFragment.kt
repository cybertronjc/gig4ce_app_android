package com.gigforce.modules.feature_chat.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject
import com.gigforce.modules.feature_chat.ui.ChatListView

/**
 * A simple [Fragment] subclass.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : Fragment() {

    val viewModel:ChatListFragmentViewModel by viewModels<ChatListFragmentViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.chatHeaders.observe(this, Observer {
            ChatListFragment@this.view?.findViewById<ChatListView>(R.id.rv_chat_headers)?.headers =
                ArrayList(it.map {
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
        })

        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            ChatListFragment().apply {
                arguments = Bundle().apply {
                    // putString(ARG_PARAM1, param1)
                }
            }
    }
}