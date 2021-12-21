package com.gigforce.modules.feature_chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.databinding.ChatMediaRecyclerItemViewLayoutBinding
import com.gigforce.modules.feature_chat.models.ChatMediaDocsRecyclerItemData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatMediaRecyclerItemView(
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

    private lateinit var viewBinding: ChatMediaRecyclerItemViewLayoutBinding
    private var viewData: ChatMediaDocsRecyclerItemData.ChatMediaRecyclerItemData? = null

    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = ChatMediaRecyclerItemViewLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        viewData = null

        data?.let {
            val chatMediaData =
                it as ChatMediaDocsRecyclerItemData.ChatMediaRecyclerItemData
            viewData = chatMediaData


        }
    }

    override fun onClick(v: View?) {
        val currentViewData = viewData ?: return
    }


}