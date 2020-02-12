package com.gigforce.app.modules.onboarding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.onboarding.adapters.UserDataAdapter
import com.gigforce.app.modules.onboarding.models.UserData
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import kotlinx.android.synthetic.main.fragment_userinfo.*
import kotlinx.android.synthetic.main.fragment_userinfo.view.*


class UserInfoFragment: Fragment() {
    companion object {
        fun newInstance() = UserInfoFragment()
    }

    private lateinit var viewModel: UserInfoViewModel
    private lateinit var storage: FirebaseStorage
    private lateinit var layout: View
    var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //   return super.onCreateView(inflater, container, savedInstanceState)
        Log.d("OnBoardingUserInfo", "tried to create View")
        layout = inflater.inflate(R.layout.fragment_userinfo, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("OnBoardingUserInfo", "calling created view ")

        //val recyclerView: RecyclerView= view!!.findViewById(R.id.recviewUserInfo)
        val userList: ArrayList<UserData> = ArrayList()
        userList.add(UserData("PD"))
        userList.add(UserData("yogesh"))
        //userList.add(UserData("Mayank"))

        var editTextUserInput = layout.findViewById(R.id.onboarding_chat_edit_text) as EditText

        var recycler = layout.findViewById(R.id.recviewUserInfo) as RecyclerView
        recycler.layoutManager=LinearLayoutManager(layout.context)
        recycler.adapter=UserDataAdapter(this.context,userList)
        Log.d("OnBoardingUserInfo", "called created view ")

        layout.onboarding_chat_send_btn.setOnClickListener{
            userList.add(UserData("What's your name?"))
            userList.add(UserData(editTextUserInput.text.toString()))
            Log.d("OnBoardingUserInfo", editTextUserInput.text.toString())
            this.findNavController().navigate(R.id.gotoOB)
        }

    }
}