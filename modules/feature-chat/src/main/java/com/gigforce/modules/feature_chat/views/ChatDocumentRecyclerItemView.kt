package com.gigforce.modules.feature_chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatMediaViewModels
import javax.inject.Inject

class ChatDocumentRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker


    //Views
    private lateinit var docIcon : ImageView
    private lateinit var docFileName : TextView
    private lateinit var docFileDetails : TextView
    private lateinit var docFileDate : TextView

    init {
        setDefault()
        inflate()
        findViews()
    }

    private fun findViews() {
        docIcon = this.findViewById(R.id.doc_icon)
        docFileName = this.findViewById(R.id.doc_file_name)
        docFileDetails = this.findViewById(R.id.doc_file_details)
        docFileDate = this.findViewById(R.id.doc_file_date)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.recycler_view_chat_document_item_view, this, true)
    }

    override fun bind(data: Any?) {
        data?.let {
            val mediaData = it as ChatMediaViewModels.ChatMediaDocItemData
            docFileName.text = mediaData.docName ?: ""
            docFileDetails.text = mediaData.docDetail ?: ""
            docFileDate.text = mediaData.docDate ?: ""
        }
    }

    override fun onClick(v: View?) {

    }

}