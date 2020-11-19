package com.gigforce.app.modules.chatmodule.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.ui.adapters.ChatListRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.OnContactClickListener
import com.gigforce.app.modules.chatmodule.viewModels.ChatHeadersViewModel
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.VerticalItemDecorator
import kotlinx.android.synthetic.main.contact_screen_fragment.*

/*
    This is supposed to be Chat Headers Screen
 */
class ChatListFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    ChatListRecyclerAdapter.OnChatItemClickListener {


    private val viewModel: ChatHeadersViewModel by activityViewModels<ChatHeadersViewModel>()
    private val mAdapter: ChatListRecyclerAdapter by lazy {
        ChatListRecyclerAdapter(initGlide()!!, this)
    }

    /*
    companion object {
        fun newInstance() = ContactScreenFragment()
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.contact_screen_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    fun initialize() {
        initRecycler()
        viewModel.ChatHeaders.observe(viewLifecycleOwner, Observer {

            it?.let {
                mAdapter.setData(it)
            }

            it.forEach {
                Log.d("CHAT", it.toString())
                // viewModel.getChatMsgs(it.id)
            }
        })

        /*
        viewModel.chatMsgs.observe(viewLifecycleOwner, Observer {
            it.forEach {
                Log.d("CHAT", "MSG " + it.toString())
            }
        })*/
        attachListeners()
    }

    private fun attachListeners() {

        fab.setOnClickListener { view ->
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
        rv_contacts.addItemDecoration(VerticalItemDecorator(30))
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


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.mnu_invite_friend -> {
                navigate(R.id.referrals_fragment)
            }

        }
        return false
    }

    override fun onChatItemClicked(chatHeader: ChatHeader) {
        val bundle = Bundle()
        bundle.putString(AppConstants.IMAGE_URL, chatHeader.otherUser?.profilePic)
        bundle.putString(AppConstants.CONTACT_NAME, chatHeader.otherUser?.name)
        bundle.putString("chatHeaderId", chatHeader.id)
        bundle.putString("forUserId", chatHeader.forUserId)
        bundle.putString("otherUserId", chatHeader.otherUserId)
        navigate(R.id.chatScreenFragment, bundle)
    }
}
