package com.gigforce.app.modules.chatmodule.ui.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.toDisplayText
import com.gigforce.app.modules.chatmodule.models.Message

class ChatRecyclerAdapter : RecyclerView.Adapter<ChatRecyclerAdapter.ChatViewHolder>(){

    private var chatModelList : ArrayList<Message>? = ArrayList()

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

    fun setData(msgs : ArrayList<Message>){
        chatModelList = msgs
        notifyDataSetChanged()
    }

    class ChatViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){

        private val linearLayout : LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView : TextView = itemView.findViewById(R.id.tv_msgValue)
        private val textViewTime : TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView : CardView = itemView.findViewById(R.id.cv_msgContainer)

        fun bindValues(msg : Message){
            when(msg.flowType){
                "in" -> {
                    textView.text = msg.content

                    textViewTime.text = msg.timestamp?.toDisplayText()

                    linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))
                    textView.setTextColor(Color.parseColor("#000000"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(26,5,160,5)
                    cardView.layoutParams = layoutParams
                }
                "out" -> {
                    textView.text = msg.content
                    textViewTime.text = msg.timestamp?.toDisplayText()
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
}