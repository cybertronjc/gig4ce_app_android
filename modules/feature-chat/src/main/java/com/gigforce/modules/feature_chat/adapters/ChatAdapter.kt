package com.gigforce.modules.feature_chat.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.CoreViewHolder
import javax.inject.Inject

class ChatAdapter(
    val collection: List<Any>
): RecyclerView.Adapter<CoreViewHolder>() {

    // @Inject
    lateinit var iViewTypeFinder: IViewTypeFinder

    init {
        iViewTypeFinder = ViewTypeFinder()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoreViewHolder {
        return iViewTypeFinder.getViewHolder(parent.context, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return iViewTypeFinder.getViewType(collection.get(position))
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    override fun onBindViewHolder(holder: CoreViewHolder, position: Int) {
        holder.IViewHolder.bind(collection.get(position))
    }
}