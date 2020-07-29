package com.abhijai.gigschatdemo.contacts_module.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhijai.gigschatdemo.contacts_module.models.ChatModel
import com.abhijai.gigschatdemo.contacts_module.ui.adapters.ChatRecyclerAdapter
import com.abhijai.gigschatdemo.contacts_module.viewModels.ContactViewModel
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.VerticalItemDecorator
import kotlinx.android.synthetic.main.fragment_chat_screen.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatScreenFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var viewModel: ContactViewModel
    private lateinit var mAdapter: ChatRecyclerAdapter
    lateinit var imageUrl: String
    lateinit var username: String

    companion object {
        fun newInstance() = ChatScreenFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUrl = it.getSerializable(AppConstants.IMAGE_URL).toString()
            username = it.getSerializable(AppConstants.CONTACT_NAME).toString()

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
        viewModel.prepareChatList(username)
        manageNewMessageToContact()
        manageBackIcon()
    }

    private fun init() {
        mAdapter = ChatRecyclerAdapter()
        initIntent()
        initListeners()
        initRecycler()
        viewModel = ViewModelProviders.of(this).get(ContactViewModel::class.java)
    }

    private fun initRecycler() {
        rv_chats.layoutManager = LinearLayoutManager(activity?.applicationContext)
        rv_chats.addItemDecoration(VerticalItemDecorator(30))
        rv_chats.adapter = mAdapter
    }

    private fun subscribeViewModel() {
        viewModel.getChatLiveData().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mAdapter.setData(it as ArrayList<ChatModel>)
                rv_chats.scrollToPosition(it.size - 1)
            }
        })
    }

    private fun initIntent() {
//        val intentData = Intent()
//        val url = intentData.getStringExtra(AppConstants.IMAGE_URL)
//        val contactName = intentData.getStringExtra(AppConstants.CONTACT_NAME)
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

    private fun manageTime(): String {
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
        return formatted
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
                viewModel.addNewMessageToTheList(message, msgTime)
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

}
