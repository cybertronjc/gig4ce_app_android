package com.gigforce.modules.feature_chat.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.CoreRecyclerAdapter
import com.gigforce.core.CoreViewHolder
import com.gigforce.core.IViewTypeFinder
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import javax.inject.Inject

class ChatAdapter(
    context: Context,
    var _collection: List<Any>
): CoreRecyclerAdapter(_collection) {

    @Inject
    lateinit var iViewTypeFinder: IViewTypeFinder

    init {
        (context.applicationContext as ChatModuleProvider).provideChatModule().inject(this)
        //todo: Remove and inject via dagger
        // iViewTypeFinder = ViewTypeFinder()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoreViewHolder {
        /*
            // Normal Code
            var view:View = null
            getView(viewType): switch(viewtype)
                case1 -> view = inflate(R.layout.file)
                case2 -> view = inflate(R.layout.file)
                case3 -> view = inflate(R.layout.file)
            return CustomViewHolder(view)
         */
        return iViewTypeFinder.getViewHolder(parent.context, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        /*
              object = collection(position)
              based on object -> return viewtype (generally a constant)
              eg. VIEWTYPE_CHAT_TEXT_IN, VIEWTYPE_CHAT_TEXT_OUT
         */
        return iViewTypeFinder.getViewType(collection.get(position))
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    override fun onBindViewHolder(holder: CoreViewHolder, position: Int) {
        holder.IViewHolder.bind(collection.get(position))
    }
}