package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.gigforce.common_ui.chat.models.MessageReceivingInfo
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class GroupMessageReadItemView(
        context: Context
) :
        RelativeLayout(context),
        IViewHolder,
        View.OnClickListener {

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    //Views
    private lateinit var contextImageView: GigforceImageView
    private lateinit var textViewName: TextView
    private lateinit var txtSubtitle: TextView


    init {

        LayoutInflater.from(context).inflate(R.layout.recycler_item_group_message_received_by, this, true)
        this.findViewById<View>(R.id.contactItemRoot).setOnClickListener(this)

        findViews()
    }

    private fun findViews() {

        contextImageView = this.findViewById(R.id.iv_profile)
        textViewName = this.findViewById(R.id.txt_title)
        txtSubtitle = this.findViewById(R.id.txt_subtitle)
    }

    private var dObj: MessageReceivingInfo? = null

    override fun bind(data: Any?) {
        data?.let {
            val dObj = data as MessageReceivingInfo
            dObj.let { chatHeader ->

                textViewName.text = chatHeader.profileName

                if(chatHeader.profilePicture.isNotBlank()) {
                    contextImageView.loadImageIfUrlElseTryFirebaseStorage(chatHeader.profilePicture,R.drawable.ic_user_2,R.drawable.ic_user_2)
                } else {
                    contextImageView.loadImage(R.drawable.ic_user_2)
                }
                txtSubtitle.text = context.getString(R.string.read_on_chat) + formatDate(chatHeader.readOn)
            }
        }
    }

    private fun formatDate(readOn: Timestamp): String {
        val chatDate = readOn.toDate()
        return if (DateUtils.isToday(chatDate.time)) SimpleDateFormat("hh:mm aa").format(
                chatDate
        ) else SimpleDateFormat("hh:mm aa,dd MMM").format(chatDate)
    }


    override fun onClick(v: View?) {
        dObj?.let {

            chatNavigation.navigateToChatPage(
                    chatType = ChatConstants.CHAT_TYPE_USER,
                    otherUserId = it.uid,
                    headerId = "",
                    otherUserName = it.profileName,
                    otherUserProfilePicture = it.profilePicture,
                    sharedFileBundle = null
            )
        }
    }
}