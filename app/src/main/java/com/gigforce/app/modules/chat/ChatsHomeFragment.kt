package com.gigforce.app.modules.chat

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_chats_home.*

class ChatsHomeFragment : Fragment() {

    companion object {
        fun newInstance() = ChatsHomeFragment()
    }

    private lateinit var viewModel: ChatsHomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ChatsHomeViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.rv_chats.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        this.rv_chats.adapter = ChatsAdapter()
    }
}

class ChatsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ChatsViewHolder(val view: View): RecyclerView.ViewHolder(view) {

        init {
            setProfileImage()
        }

//        fun bind(data:Any) {
//
//        }

        fun setProfileImage(){
            val imageView = view.findViewById<ImageView>(R.id.img_obprofile)
            GlideApp.with(view.context)
                .load("")
                .placeholder(R.drawable.placeholder_user)
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_header, parent, false);
        return ChatsViewHolder(view);
    }

    override fun getItemCount(): Int {
        return 100;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }
}