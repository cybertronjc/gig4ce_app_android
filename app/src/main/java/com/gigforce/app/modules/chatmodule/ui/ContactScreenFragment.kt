package com.abhijai.gigschatdemo.contacts_module.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhijai.gigschatdemo.contacts_module.ui.adapters.ContactRecyclerAdapter
import com.abhijai.gigschatdemo.contacts_module.ui.adapters.OnContactClickListener
import com.abhijai.gigschatdemo.contacts_module.viewModels.ContactViewModel
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.VerticalItemDecorator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.contact_screen_fragment.*

class ContactScreenFragment : BaseFragment(),OnContactClickListener {

    private lateinit var viewModel : ContactViewModel
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
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        attachListeners()

        viewModel = ViewModelProviders.of(this).get(ContactViewModel::class.java)
        mAdapter =
            ContactRecyclerAdapter(
                initGlide()!!,
                this
            )
        subscribeViewModel()
        viewModel.prepareList()
        initRecycler()
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
            Snackbar.make(view, "Add New Contact", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        iv_backArrow.setOnClickListener {
            showToast("onBackPressed operation")
        }
        iv_search.setOnClickListener {
            showToast("Search Operation...")
        }
    }

    private fun subscribeViewModel(){
        viewModel.getContactsLiveData().observe(viewLifecycleOwner, Observer {
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
                showToast("Invite Friends...")
                true
            }
            R.id.action_referesh -> {
                showToast("Referesh...")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun contactClick(url: String, name: String) {
//        val intent = Intent(activity?.applicationContext,ChatScreenFragment::class.java)
//        intent.putExtra(AppConstants.IMAGE_URL,url)
//        intent.putExtra(AppConstants.CONTACT_NAME,name)
//        startActivity(intent)
        if(name.equals("Help")){
            navigate(R.id.helpChatFragment)
        }else {
            val bundle = Bundle()
            bundle.putSerializable(AppConstants.IMAGE_URL, url)
            bundle.putSerializable(AppConstants.CONTACT_NAME, name)
            navController.navigate(R.id.chatScreenFragment, bundle)
        }
    }
}
