package com.gigforce.app.modules.chatmodule.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.modules.chatmodule.ui.adapters.ContactRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.OnContactClickListener
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.chatmodule.viewModels.ChatViewModel
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.VerticalItemDecorator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.contact_screen_fragment.*

class ContactScreenFragment : BaseFragment(), OnContactClickListener {

    private lateinit var viewModel : ChatViewModel
    private lateinit var mAdapter : ContactRecyclerAdapter

    companion object {
        fun newInstance() = ContactScreenFragment()
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
        attachListeners()

        val tempviewModel: ChatViewModel by activityViewModels<ChatViewModel>()
        viewModel = tempviewModel
        mAdapter =
            ContactRecyclerAdapter(
                initGlide()!!,
                this
            )
        subscribeViewModel()
        initRecycler()
    }

    fun initialize() {
        val chatViewModel: ChatViewModel by activityViewModels<ChatViewModel>()

        chatViewModel.chatHeaders.observe(viewLifecycleOwner, Observer {
            it.forEach {
                Log.d("CHAT", it.toString())
                chatViewModel.getChatMsgs(it.id)
            }
        })

        chatViewModel.chatMsgs.observe(viewLifecycleOwner, Observer {
            it.forEach {
                Log.d("CHAT", "MSG " + it.toString())
            }
        })
    }
//    override fun onCreate(savedInstanceState: Bundle?)
//    {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)
//        toolbar.overflowIcon!!.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
//
//    }

    private fun attachListeners(){
        fab.setOnClickListener { view ->
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

    private fun subscribeViewModel(){
        viewModel.chatHeaders.observe(viewLifecycleOwner, Observer {
            if (it!=null){
                mAdapter.setData(it)
            }
        })
    }

    private fun initRecycler(){
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

    override fun contactClick(url: String, name: String, chatHeaderId: String) {
//        val intent = Intent(activity?.applicationContext,ChatScreenFragment::class.java)
//        intent.putExtra(AppConstants.IMAGE_URL,url)
//        intent.putExtra(AppConstants.CONTACT_NAME,name)
//        startActivity(intent)
        if(name == getString(R.string.help)){
            navigate(R.id.helpChatFragment)
        }else {
            val bundle = Bundle()
            bundle.putSerializable(AppConstants.IMAGE_URL, url)
            bundle.putSerializable(AppConstants.CONTACT_NAME, name)
            bundle.putSerializable("chatHeaderId", chatHeaderId)
            navigate(R.id.chatScreenFragment, bundle)
        }
    }
}
