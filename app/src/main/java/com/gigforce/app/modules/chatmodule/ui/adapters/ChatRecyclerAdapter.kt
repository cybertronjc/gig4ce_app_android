package com.abhijai.gigschatdemo.contacts_module.ui.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.abhijai.gigschatdemo.contacts_module.models.ChatModel
import com.gigforce.app.R

class ChatRecyclerAdapter : RecyclerView.Adapter<ChatRecyclerAdapter.ChatViewHolder>(){

    private var chatModelList : ArrayList<ChatModel>? = ArrayList()
    class ChatViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        private val linearLayout : LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView : TextView = itemView.findViewById(R.id.tv_msgValue)
        private val textViewTime : TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView : CardView = itemView.findViewById(R.id.cv_msgContainer)
        fun bindValues(chatModel : ChatModel){
            when(chatModel.fromContact){
                true -> {
                    textView.text = chatModel.message
                    textViewTime.text = chatModel.msgTime
                    linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))
                    textView.setTextColor(Color.parseColor("#000000"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(26,5,160,5)
                    cardView.layoutParams = layoutParams
                }
                false -> {
                    textView.text = chatModel.message
                    textViewTime.text = chatModel.msgTime
                    linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                    textView.setTextColor(Color.parseColor("#ffffff"))
                    textViewTime.setTextColor(Color.parseColor("#ffffff"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.END
                    layoutParams.setMargins(160,5,26,5)
                    cardView.layoutParams = layoutParams

                }
            }
        }
    }

    private fun manageViewsParams(){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gig_chat_item,parent,false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatModelList?.size?:0
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        chatModelList?.let {
            holder.bindValues(it[position])
        }
    }

    fun setData(list : ArrayList<ChatModel>){
        chatModelList = list
        notifyDataSetChanged()
    }
}