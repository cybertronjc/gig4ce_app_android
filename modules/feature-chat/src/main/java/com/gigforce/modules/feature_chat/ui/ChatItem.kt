package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatMessage

class ChatItem(context: Context?) :
    RelativeLayout(context),
    IViewHolder,
    View.OnClickListener {

    init {
        LayoutInflater.from(context).inflate(R.layout.chat_item, this, true)
        this.setOnClickListener(this)
    }

    private var msg: ChatMessage? = null

    override fun bind(data: Any?) {
        // title:String, subtitle:String, timeDisplay:String, profilePath:String, unreadCount:Int, id:String, type: String
        msg = null
        data?.let {
            msg = data as ChatMessage

            val linearLayout: LinearLayout = this.findViewById(R.id.ll_msgContainer)
            val textView: TextView = this.findViewById(R.id.tv_msgValue)
            val textViewTime: TextView = this.findViewById(R.id.tv_msgTimeValue)
            val cardView: CardView = this.findViewById(R.id.cv_msgContainer)

            when (msg!!.flowType) {
                "in" -> {
                    textView.text = msg!!.content

                    textViewTime.text = msg!!.timestamp?.toDisplayText()
                    textViewTime.setTextColor(Color.parseColor("#979797"))

                    linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))
                    textView.setTextColor(Color.parseColor("#000000"))


                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(26, 5, 160, 5)
                    cardView.layoutParams = layoutParams
                }
                "out" -> {
                    textView.text = msg?.content

                    textViewTime.text = msg?.timestamp?.toDisplayText()
                    textViewTime.setTextColor(Color.parseColor("#ffffff"))

                    linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                    textView.setTextColor(Color.parseColor("#ffffff"))


                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.END
                    layoutParams.setMargins(160, 5, 26, 5)
                    cardView.layoutParams = layoutParams
                }
            }
        }
    }

    override fun onClick(v: View?) {
        msg.let {
            Toast.makeText(this.context, "Tapped", Toast.LENGTH_SHORT).show()
        }
    }
}