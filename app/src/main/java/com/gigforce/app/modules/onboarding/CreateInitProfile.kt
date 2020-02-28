package com.gigforce.app.modules.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.onboarding.controls.OBTextView
import com.gigforce.app.modules.onboarding.controls.OBToggleButton
import com.gigforce.app.modules.onboarding.controls.ViewChanger
import com.gigforce.app.modules.onboarding.models.UserData
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_confirm_otp.*
import kotlinx.android.synthetic.main.fragment_create_init_profile.*
import kotlinx.android.synthetic.main.fragment_create_init_profile.onboarding_chat_send_btn
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_userinfo.*
import kotlinx.android.synthetic.main.fragment_userinfo.view.*

/**
 * A simple [Fragment] subclass.
 * Use the [CreateInitProfile.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateInitProfile : Fragment() {

    private lateinit var viewModel: UserInfoViewModel
    private lateinit var storage: FirebaseStorage
    private lateinit var layout: View
    var recyclerView: RecyclerView? = null

    private lateinit  var updatesUserInfo: UserData

    private var userListFull: ArrayList<UserData> = ArrayList()
    private var userInfoFull: ArrayList<String> = ArrayList()

    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        userListFull.add(UserData("What's your name?"));userInfoFull.add("name");
        userListFull.add(UserData("What's your dob?"));userInfoFull.add("dob");
        userListFull.add(UserData("What's your gender?"));userInfoFull.add("gender");
        userListFull.add(UserData("What's your qualification?"));userInfoFull.add("qualification");
        userListFull.add(UserData("What's your yoq?"));userInfoFull.add("yoq");
        userListFull.add(UserData("Are you a student?"));userInfoFull.add("isStudent");
        userListFull.add(UserData("Will you do part time work?"));userInfoFull.add("part");
        layout = inflater.inflate(R.layout.fragment_create_init_profile, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onboarding_chat_send_btn?.setOnClickListener {
            // based on counter or position value change the below view
            setupRV()
            when (counter) {
                0 -> {
                        val textView = TextView(this.context);
                        textView.text = "I'm loving Android!"
                        framelayout?.removeAllViews()
                        framelayout?.addView(textView)
                }
                1 -> {
                        var textView2= TextView(this.context);
                        textView2.text = "this is second text view"
                        framelayout?.removeAllViews()
                        framelayout?.addView(textView2)
                }
                2 -> {
                        //LayoutInflater.from(context).inflate(R.layout.item_ob_toggle, this, true)
                    Toast.makeText(context, "counter: $counter", Toast.LENGTH_SHORT).show()
                }
                else -> {
                        Toast.makeText(context, "counter: $counter", Toast.LENGTH_SHORT).show()
                        }
            }


            counter++
        }
    }

    private fun setupRV() {
        this.rv_ob_chats.layoutManager = LinearLayoutManager(this.context)
        val adapter = CreateInitProfileRVAdapter()
        layout.onboarding_chat_send_btn.setOnClickListener {
            if (counter == 0) {
                adapter.data.add(ObChatLogItem("in", userInfoFull[counter].toString()))
            } else {
                if(counter < userInfoFull.size) {
                    adapter.data.add(ObChatLogItem("out", "User Input"))
                    adapter.data.add(ObChatLogItem("in", userInfoFull[counter].toString()))
                }
            }
        }
        this.rv_ob_chats.adapter = adapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CreateInitProfile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            CreateInitProfile().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

class CreateInitProfileRVAdapter: RecyclerView.Adapter<CreateInitProfileRVAdapter.ViewHolder>() {

    var data:ArrayList<ObChatLogItem> = ArrayList<ObChatLogItem>()

    override fun getItemViewType(position: Int): Int {
        if(data.get(position).flow_direction == "in") {
            return 101
        }else
        {
            return  102
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = (if(viewType == 101)
            R.layout.item_ob_chat_in
        else
            R.layout.item_ob_chat_out)

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data.get(position))
    }

    class ViewHolder(val view:View): RecyclerView.ViewHolder(view)
    {
        fun bind(item:ObChatLogItem) {
            view.findViewById<TextView>(R.id.txt).setText(item.text)
            // chanage profile_icon based in item.profile_icon_path
            val imageView = view?.findViewById<ImageView>(R.id.imageView)
            setChatUserImage(imageView)
        }

        fun setChatUserImage(imageView: ImageView){
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
}

data class ObChatLogItem(
    val flow_direction:String, // in, out
    val text: String,
    val timestamp: String = "", // ideally a date format
    val profile_icon_path: String = "", // leave it blank
    val required_input_type: String = "text"   // text, date, time, options
){

}