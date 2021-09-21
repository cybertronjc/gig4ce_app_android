package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.MentionUser
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.inflate
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.GroupChatMember
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.linkedin.android.spyglass.suggestions.SuggestionsResult
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager
import com.linkedin.android.spyglass.tokenization.QueryToken
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver
import com.linkedin.android.spyglass.ui.MentionsEditText
import java.util.*

class ChatFooter(context: Context, attrs: AttributeSet) :
        LinearLayout(context, attrs),
        QueryTokenReceiver,
        SuggestionsVisibilityManager,
        SuggestionsResultListener {

    companion object {
        private const val SUGGESTION_BUCKET = "names-suggestions"

        private val tokenizerConfig = WordTokenizerConfig.Builder()
                .setWordBreakChars(", ")
                .setExplicitChars("@")
//                .setExplicitChars("")
                .setMaxNumKeywords(2)
                .setThreshold(1)
                .build()
    }

    private var suggestionRecyclerView: RecyclerView
    private var mentionAdapter: PersonMentionAdapter

    var et_message: MentionsEditText
    var btn_send: AppCompatImageButton
    var attachmentOptionButton: ImageView

    var replyBlockedLayout: TextView
    var replyLayout: View

    private var replyMessagePreviewContainer: LinearLayout
    private var replyMessage : ChatMessage? = null

    private lateinit var viewModel: GroupChatViewModel

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.fragment_chat_footer, this, true)
        setBackgroundColor(Color.parseColor("#f6f7f8"))

        suggestionRecyclerView = findViewById(R.id.mention_suggestion_rv)
        suggestionRecyclerView.layoutManager = LinearLayoutManager(context)
        mentionAdapter = PersonMentionAdapter(emptyList())
        suggestionRecyclerView.adapter = mentionAdapter

        et_message = this.findViewById(R.id.et_typedMessageValue)
        btn_send = this.findViewById(R.id.btn_send_chat)
        attachmentOptionButton = this.findViewById(R.id.iv_greyPlus)

        replyBlockedLayout = this.findViewById(R.id.group_does_support_replies_text)
        replyLayout = this.findViewById(R.id.chat_footer_type_layout)

        replyMessagePreviewContainer = this.findViewById(R.id.reply_to_message_layout)
    }

    fun setGroupViewModel(viewModel: GroupChatViewModel) {
        this.viewModel = viewModel
    }

    fun disableInput(
        message : String
    ){
        replyLayout.gone()
        replyBlockedLayout.visible()
        replyBlockedLayout.text = message
    }

    fun enableInput(){
        replyBlockedLayout.gone()
        replyLayout.visible()
        replyBlockedLayout.text = ""
    }

    fun enableUserSuggestions() {
        et_message.tokenizer = WordTokenizer(tokenizerConfig)
        et_message.setQueryTokenReceiver(this)
        et_message.setSuggestionsVisibilityManager(this)
    }

    override fun onQueryReceived(queryToken: QueryToken): MutableList<String> {
        val buckets = Collections.singletonList(SUGGESTION_BUCKET)
        val nameSuggestions = viewModel.getGroupMembersNameSuggestions(queryToken.keywords)
        val result = SuggestionsResult(queryToken, nameSuggestions)
        onReceiveSuggestionsResult(result, SUGGESTION_BUCKET)
        return buckets
    }

    fun getMentionedPeopleInText(): List<MentionUser> {
        val text = et_message.mentionsText
        val mentionedSpans = text.mentionSpans

        if (mentionedSpans.isEmpty())
            return emptyList()

        val personMentions: MutableList<MentionUser> = mutableListOf()
        mentionedSpans.forEach { span ->

            val start = text.getSpanStart(span)
            val end = text.getSpanEnd(span)
            val mentionedPerson = span.mention as GroupChatMember

            personMentions.add(
                    MentionUser(
                            startFrom = start,
                            endTo = end,
                            userMentionedUid = mentionedPerson.uid,
                            profileName = mentionedPerson.name,
                            profilePicture = mentionedPerson.profilePicture
                    )
            )
        }

        return personMentions
    }

    fun isTypingEnabled() : Boolean{
        return replyLayout.isVisible
    }


    override fun displaySuggestions(display: Boolean) {
        suggestionRecyclerView.isVisible = display
    }

    override fun isDisplayingSuggestions(): Boolean {
        return suggestionRecyclerView.isVisible
    }

    override fun onReceiveSuggestionsResult(result: SuggestionsResult, bucket: String) {
        val suggestions = result.suggestions as List<GroupChatMember>
        mentionAdapter = PersonMentionAdapter(suggestions)
        suggestionRecyclerView.swapAdapter(mentionAdapter, true)
        displaySuggestions(suggestions.isNotEmpty())
    }

    fun openReplyUi(
            chatMessage: ChatMessage
    ) {
        this.replyMessage = chatMessage
        replyMessagePreviewContainer.removeAllViews()

        val replyView = LayoutInflater.from(context).inflate(
                R.layout.layout_reply_to_layout,
                null,
                false
        )
        replyMessagePreviewContainer.addView(replyView)

        //Setting common vars and listeners
        val senderNameTV: TextView = replyView.findViewById(R.id.user_name_tv)
        val messageTV: TextView = replyView.findViewById(R.id.tv_msgValue)
        val closeBtn: View = replyView.findViewById(R.id.close_btn)
        val messageImageIV: GigforceImageView = replyView.findViewById(R.id.message_image)

        closeBtn.setOnClickListener {
            closeReplyUi()
        }
        senderNameTV.text = chatMessage.senderInfo.name

        when (chatMessage.type) {
            ChatConstants.MESSAGE_TYPE_TEXT -> {
                messageTV.text = chatMessage.content
                messageImageIV.gone()
            }
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {
                messageTV.text = chatMessage.attachmentName
                messageImageIV.visible()

                if(chatMessage.thumbnailBitmap != null){
                    messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                } else if(chatMessage.thumbnail != null){
                    messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                }else if(chatMessage.attachmentPath != null){
                    messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.attachmentPath!!)
                } else {
                    //load default image
                }
            }
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {
                messageTV.text = chatMessage.attachmentName
                messageImageIV.visible()

                if(chatMessage.thumbnailBitmap != null){
                    messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                } else if(chatMessage.thumbnail != null){
                    messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                }else {
                    //load default image
                }
            }
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> {
                messageTV.text = chatMessage.locationPhysicalAddress
                messageImageIV.visible()

                if(chatMessage.thumbnailBitmap != null){
                    messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                } else if(chatMessage.thumbnail != null){
                    messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                }else if(chatMessage.attachmentPath != null){
                    messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.attachmentPath!!)
                } else {
                    //load default image
                }
            }
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                messageTV.text = chatMessage.attachmentName
                messageImageIV.visible()
                messageImageIV.loadImage(R.drawable.ic_document_background)
            }
            else -> {
            }
        }

        openSoftKeyboard(et_message)
        et_message.requestFocus()
    }

    fun closeReplyUi(){
        replyMessagePreviewContainer.removeAllViews()
        replyMessage = null
    }

    fun getReplyToMessage(): ChatMessage? {
        return replyMessage
    }

    fun openSoftKeyboard(view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethod.SHOW_FORCED,
                0
        )
    }


    // --------------------------------------------------
    // PersonMentionAdapter Class
    // --------------------------------------------------
    private class SuggestionItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.person_name)
        var picture: GigforceImageView = itemView.findViewById(R.id.person_image)
    }

    private inner class PersonMentionAdapter(
            private val suggestions: List<GroupChatMember>
    ) : RecyclerView.Adapter<SuggestionItemViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SuggestionItemViewHolder {
            val v: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.recycler_item_mention_suggestion, viewGroup, false)
            return SuggestionItemViewHolder(v)
        }

        override fun onBindViewHolder(viewHolder: SuggestionItemViewHolder, i: Int) {
            val person = suggestions[i]
            viewHolder.name.text = person.name

            if (person.profilePicture.isNotBlank())
                viewHolder.picture.loadImageIfUrlElseTryFirebaseStorage(person.profilePicture, R.drawable.ic_user_2, R.drawable.ic_user_2)
            else {
                viewHolder.picture.loadImage(R.drawable.ic_user_2)
            }

            viewHolder.itemView.setOnClickListener {
                et_message.insertMention(person)
                suggestionRecyclerView.swapAdapter(PersonMentionAdapter(emptyList()), true)
                displaySuggestions(false)
                et_message.requestFocus()
            }
        }

        override fun getItemCount(): Int {
            return suggestions.size
        }
    }
}