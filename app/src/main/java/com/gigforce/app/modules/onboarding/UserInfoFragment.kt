package com.gigforce.app.modules.onboarding

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.onboarding.adapters.UserDataAdapter
import com.gigforce.app.modules.onboarding.models.UserData
import com.gigforce.app.utils.GlideApp
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_userinfo.view.*
import kotlinx.android.synthetic.main.login_activity.*


class UserInfoFragment: Fragment() {
    companion object {
        fun newInstance() = UserInfoFragment()
    }

    private lateinit var viewModel: UserInfoViewModel
    private lateinit var storage: FirebaseStorage
    private lateinit var layout: View
    var recyclerView: RecyclerView? = null

    private lateinit  var updatesUserInfo: UserData

    private var userListFull: ArrayList<UserData> = ArrayList()
    private var userInfoFull: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //   return super.onCreateView(inflater, container, savedInstanceState)
        Log.d("OnBoardingUserInfo", "tried to create View")
        layout = inflater.inflate(R.layout.fragment_userinfo, container, false)
        //val view = layoutInflater.inflate(R.layout.chat_bottom_modal_sheet, null)
/*        val dialog = BottomSheetDialog(view.context)
        dialog.setContentView(view)
        dialog.show()*/
        userListFull.add(UserData("What's your name?"));userInfoFull.add("name");
        userListFull.add(UserData("What's your dob?"));userInfoFull.add("dob");
        userListFull.add(UserData("What's your gender?"));userInfoFull.add("gender");
        userListFull.add(UserData("What's your qualification?"));userInfoFull.add("qualification");
        userListFull.add(UserData("What's your yoq?"));userInfoFull.add("yoq");
        userListFull.add(UserData("Are you a student?"));userInfoFull.add("isStudent");
        userListFull.add(UserData("Will you do part time work?"));userInfoFull.add("part");
        return layout
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("OnBoardingUserInfo", "calling created view ")

        //val recyclerView: RecyclerView= view!!.findViewById(R.id.recviewUserInfo)

        viewModel = ViewModelProviders.of(this).get(UserInfoViewModel::class.java)

        val userList: ArrayList<UserData> = ArrayList()
        userList.add(UserData("What's your name?"))
        var counter=1;

        var editTextUserInput = layout.findViewById(R.id.onboarding_chat_edit_text) as EditText
        val mAdapter = UserDataAdapter(this.context, userList)
        var recycler = layout.findViewById(R.id.recviewUserInfo) as RecyclerView
        recycler.layoutManager = LinearLayoutManager(layout.context)
        recycler.adapter = mAdapter
        Log.d("OnBoardingUserInfo", "called created view ")

        //var bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_layout)

            //Adapter for spinner
            //mySpinner.adapter = ArrayAdapter(, android.R.layout.simple_spinner_item, myStrings)
//
//            var mySpinner: Spinner? = this.material_spinner as Spinner?
//
//            //item selected listener for spinner
//            mySpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                    Toast.makeText(context, myStrings[p2], Toast.LENGTH_SHORT).show()
//                    editTextUserInput.setText(myStrings[p2])
//                }
//            }

            /*
            layout.hyd.setOnClickListener {
                editTextUserInput.setText("Hyd")
            }*/

            if(counter == 1) {
                userList.add(userListFull[0])
                mAdapter.update(userList)
            }

            layout.onboarding_chat_send_btn.setOnClickListener {
                val userInput = layout.onboarding_chat_edit_text.text.toString();

                // if code for bottom sheet
                if(counter==3) {
                    layout.onboarding_chat_edit_text.setOnClickListener {
                        val dialog = BottomSheetDialog(view.context)
//            if (view.getParent() != null) {
//                (view.getParent() as ViewGroup).removeView(view) // <- fix
//            }

                        val inflater =
                                context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val bottomsheet = inflater.inflate(
                                R.layout.chat_bottom_modal_sheet, container,
                                false
                        );
                        //TODO: Change this (NOTE: Spinner)
                        dialog.setContentView(bottomsheet)
                        dialog.show()

                        //val myStrings = arrayOf("One", "Two", "Three", "Four", "Five")
                    }
                }

                if (userInput.isNullOrBlank()) {
                    //Error message
                    Toast.makeText(
                        this.context,
                        "Please enter the correct input",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
//                    if(counter==1){
//                        userList.add(userListFull[counter-1])
//                    }

                    //adding the userinfo input from the user into the firebase
                    userList.add(UserData(userInput))
                    userList.add(userListFull[counter])
                    setProfileImage()
                    //addNewUserInfo()
                    //viewModel.setUserProfile(updatesUserInfo)
                    //viewModel.setUserProfile("name","pd")//userInput.toString());
                    viewModel.setUserProfile(userInfoFull[counter-1],userInput.toString());
                    counter++
                    mAdapter.update(userList)
                    layout.onboarding_chat_edit_text.text = null
                    //if(counter==last){
                      //  userList.add(userListFull[counter])
                    //}
                    Log.d("OnBoardingUserInfo", editTextUserInput.text.toString())
//            this.findNavController().navigate(R.id.gotoOB) // AT the end
                }
            }
    }


    fun setProfileImage(){
        val imageView = view?.findViewById<ImageView>(R.id.img_obprofile)
        view?.context?.let {
            if (imageView != null) {
                GlideApp.with(it)
                    .load("")
                    .placeholder(R.drawable.placeholder_user)
                    .into(imageView)
            }
        }
    }
}