package com.gigforce.app.modules.home

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.app.R
import com.gigforce.app.modules.auth.ui.main.LoginViewModel
import com.gigforce.app.modules.chat.ChatsHomeFragment
import com.gigforce.app.modules.roaster.RoasterFragment
import com.gigforce.app.utils.reduceDragSensitivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment: Fragment(), View.OnClickListener {

    private val loginViewModel: LoginViewModel by activityViewModels<LoginViewModel>()

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_home, container, false);
        /*val btn: Button = view.findViewById(R.id.buttonCP) as Button
        btn.setOnClickListener() {
            findNavController().navigate(R.id.sampleOB)
        }*/
        return  view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        FirebaseAuth.getInstance().addAuthStateListener {
            Toast.makeText(context, "Auth state changed to ${if(it.currentUser==null) "SignedOut" else "Signed In"}",
                    Toast.LENGTH_SHORT).show()
            Log.e("home/firebase", "Auth state changed to ${if(it.currentUser==null) "SignedOut" else "Signed In"}")

            if(it.currentUser == null) {
                initAuth()
            }else {

            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.pager_home.reduceDragSensitivity()

        val stateAdapter: HomeViewsAdapter = HomeViewsAdapter(this)
        this.pager_home.adapter = stateAdapter
        this.pager_home.setCurrentItem(1, false)


    /*
    btn_signout.setOnClickListener {
        Toast.makeText(context, "Signing out", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
    }*/
    }

    fun initAuth() {
        this.findNavController().navigate(R.id.loginFragment)
    }

    class HomeViewsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        var fragment_chats:ChatsHomeFragment? = null
        var fragment_roaster: RoasterFragment? = null

        override fun createFragment(position: Int): Fragment {
            if(position == 0) {
                fragment_chats ?: let { fragment_chats = ChatsHomeFragment() }
                return fragment_chats!!
            }else{
                fragment_roaster ?: let { fragment_roaster =
                    RoasterFragment()
                }
                return fragment_roaster!!
            }
        }

    }

    override fun onClick(v: View?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        when (v?.id) {
            R.id.buttonCP -> {
                findNavController().navigate(R.id.gotoOB)
            }
            else -> {
            }
        }
    }
}