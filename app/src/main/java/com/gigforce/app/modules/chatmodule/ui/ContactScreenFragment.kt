package com.gigforce.app.modules.chatmodule.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.modules.chatmodule.ui.adapters.ContactRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.OnContactClickListener
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.viewModels.ChatHeadersViewModel
import com.gigforce.app.modules.chatmodule.viewModels.ChatViewModel
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.VerticalItemDecorator
import com.gigforce.app.utils.openPopupMenu
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.contact_screen_fragment.*
import kotlinx.android.synthetic.main.toolbar.*

/*
    This is supposed to be Chat Headers Screen
 */
class ContactScreenFragment : BaseFragment(), OnContactClickListener, PopupMenu.OnMenuItemClickListener {

    private val viewModel : ChatHeadersViewModel by activityViewModels<ChatHeadersViewModel>()
    private lateinit var mAdapter : ContactRecyclerAdapter

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    fun initialize() {
        initRecycler()
        viewModel.ChatHeaders.observe(viewLifecycleOwner, Observer {

            it ?. let{
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

    private fun attachListeners(){

        fab.setOnClickListener { view ->

            navigate(R.id.chatNewContactFragment)
            Snackbar.make(view, getString(R.string.add_new_contact), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.action), null).show()
        }
        iv_backArrow.setOnClickListener {
            showToast(getString(R.string.onback_operation))
        }
        iv_search.setOnClickListener {
            showToast(getString(R.string.search_operation))
        }
        back_arrow.setOnClickListener{
            activity?.onBackPressed()
        }
    }

    private fun initRecycler(){

        // Initialise Adapter for Chat Headers RecyclerView
        mAdapter = ContactRecyclerAdapter(initGlide()!!, this)

        rv_contacts.layoutManager = LinearLayoutManager(activity?.applicationContext)
        rv_contacts.addItemDecoration(VerticalItemDecorator(30))
        rv_contacts.adapter = mAdapter
    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_chat_contact, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId)
        {
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

    override fun contactClick(chatHeader: ChatHeader) {
//        val intent = Intent(activity?.applicationContext,ChatScreenFragment::class.java)
//        intent.putExtra(AppConstants.IMAGE_URL,url)
//        intent.putExtra(AppConstants.CONTACT_NAME,name)
//        startActivity(intent)
        if(chatHeader.otherUser?.name?.equals("Help")?:false){
            navigate(R.id.helpChatFragment)
        }else {
            val bundle = Bundle()
            bundle.putSerializable(AppConstants.IMAGE_URL, chatHeader.otherUser?.profilePic)
            bundle.putSerializable(AppConstants.CONTACT_NAME, chatHeader.otherUser?.name)
            bundle.putSerializable("chatHeaderId", chatHeader.id)
            bundle.putSerializable("forUserId", chatHeader.forUserId)
            bundle.putSerializable("otherUserId", chatHeader.otherUserId)
            navigate(R.id.chatScreenFragment, bundle)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
when(item?.itemId){
    R.id.mnu_invite_friend->{
        navigate(R.id.referrals_fragment)
    }

}
        return false
    }
}
