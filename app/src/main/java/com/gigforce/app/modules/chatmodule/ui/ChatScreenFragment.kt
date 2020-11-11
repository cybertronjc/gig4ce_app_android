package com.gigforce.app.modules.chatmodule.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.modules.chatmodule.ui.adapters.ChatRecyclerAdapter
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.chatmodule.models.ChatMessage
import com.gigforce.app.modules.chatmodule.models.Message
import com.gigforce.app.modules.chatmodule.models.MessageType
import com.gigforce.app.modules.chatmodule.ui.adapters.OnChatMessageClickListener
import com.gigforce.app.modules.chatmodule.viewModels.ChatMessagesViewModel
import com.gigforce.app.modules.chatmodule.viewModels.ChatViewModel
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.VerticalItemDecorator
import kotlinx.android.synthetic.main.fragment_chat_screen.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatScreenFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    OnChatMessageClickListener {

    private val viewModel: ChatMessagesViewModel by activityViewModels<ChatMessagesViewModel>()

    private lateinit var mAdapter: ChatRecyclerAdapter
    private lateinit var imageUrl: String
    private lateinit var username: String
    private lateinit var forUserId: String
    private lateinit var chatHeaderId: String
    private lateinit var otherUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUrl = it.getSerializable(AppConstants.IMAGE_URL).toString()
            username = it.getSerializable(AppConstants.CONTACT_NAME).toString()
            chatHeaderId = it.getSerializable("chatHeaderId").toString()
            forUserId = it.getSerializable("forUserId").toString()
            otherUserId = it.getSerializable("otherUserId").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_chat_screen, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        manageTime()
        subscribeViewModel()
        manageNewMessageToContact()
        manageBackIcon()
    }

    private fun init() {
        mAdapter = ChatRecyclerAdapter(this)
        initIntent()
        initListeners()
        initRecycler()
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(activity?.applicationContext)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rv_chats.layoutManager = layoutManager

        rv_chats.addItemDecoration(VerticalItemDecorator(30))
        // rv_chats.setHasFixedSize(true)
        rv_chats.adapter = mAdapter
    }

    private fun subscribeViewModel() {
        viewModel.getChatMessagesLiveData(chatHeaderId)
            .observe(viewLifecycleOwner, Observer
            {
//                if (it != null) {
//                    mAdapter.setData(it)
//                    rv_chats.smoothScrollToPosition(0)
//                }
        })
    }

    private fun initIntent() {
        val req = initGlide()
        val uri = Uri.parse("android.resource://com.gigforce.app/drawable/" + imageUrl)
        req!!.load(uri).into(civ_personImage)
        tv_nameValueInChat.text = username
    }

    private fun initListeners() {
        iv_verticalDots.setOnClickListener {
            manageMenu(it)
        }
    }

    private fun manageMenu(view: View) {
        val popUp = PopupMenu(activity?.applicationContext, view)
        popUp.setOnMenuItemClickListener(this)
        popUp.inflate(R.menu.menu_chat)
        popUp.show()
    }

    private fun manageTime(): LocalDateTime {
        var current: LocalDateTime
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            current = LocalDateTime.now()
            DateTimeFormatter.ofPattern("HH:mm")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val formatted = current.format(formatter)

        println("Current Date and Time is: $formatted")
        tv_lastSeenValue.text = "last seen at $formatted"
        return current
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_block -> {
            showToast("Block")
            true
        }
        R.id.action_report -> {
            showToast("Report")
            true
        }
        else -> {
            false
        }
    }

    private fun manageNewMessageToContact() {
        iv_sendMessage.setOnClickListener {
            if (validateNewMessageTask()) {
                val message = et_typedMessageValue.text.toString()
                val msgTime = manageTime()
                viewModel.sendNewText(chatHeaderId, forUserId, otherUserId, message)
                et_typedMessageValue.setText("")
            }
        }
    }

    private fun validateNewMessageTask(): Boolean {
        if (et_typedMessageValue.text.toString().isBlank()) {
            return false
        }
        return true
    }

    private fun manageBackIcon() {
        iv_backArrowInChat.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun chatMessageClicked(
        messageType: MessageType,
        position: Int,
        message: ChatMessage
    ) {

    }

}
