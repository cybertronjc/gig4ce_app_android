package com.gigforce.core

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CoreRecyclerAdapter(private val context: Context,
                          private var _collection: List<Any>,
                          private val viewHolderFn: (parent:ViewGroup, viewType: Int) -> CoreViewHolder
)
    : RecyclerView.Adapter<CoreViewHolder>(){

    var collection:List<Any>
        get() = _collection
        set(value) {
            _collection = value;
            this.notifyDataSetChanged();
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CoreViewHolder {
        return viewHolderFn(parent, viewType);
    }

    override fun getItemCount(): Int {
        return collection.count()
    }

    override fun onBindViewHolder(
        holder: CoreViewHolder,
        position: Int
    ) {
        holder.IViewHolder.bind(collection[position]);
    }
}