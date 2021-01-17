package com.gigforce.core

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class CoreRecyclerAdapter(
    private var _collection: List<Any>
    // private val viewHolderFn: (parent:ViewGroup, viewType: Int) -> CoreViewHolder?
)
    : RecyclerView.Adapter<CoreViewHolder>(){

    companion object {

        /*
        fun <T: View> default(context:Context, _collection: List<Any>, factory: (context:Context)->T):CoreRecyclerAdapter{
            return CoreRecyclerAdapter(context, _collection, CoreViewHolder.default<T>(context, factory))
        }*/
    }

    var collection:List<Any>
        get() = _collection
        set(value) {
            _collection = value;
            this.notifyDataSetChanged();
        }

        /*
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CoreViewHolder {
        return viewHolderFn(parent, viewType);
    }*/

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