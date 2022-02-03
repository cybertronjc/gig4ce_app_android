package com.gigforce.core.recyclerView

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.CoreViewHolder
import com.gigforce.core.ICoreViewHolderFactory
import com.gigforce.core.IDataViewTypeGetter

open class CoreRecyclerAdapter(
    context: Context,
    val iViewTypeFinder: ICoreViewHolderFactory
) : RecyclerView.Adapter<CoreViewHolder>() {

    companion object {

        /*
        fun <T: View> default(context:Context, _collection: List<Any>, factory: (context:Context)->T):CoreRecyclerAdapter{
            return CoreRecyclerAdapter(context, _collection, CoreViewHolder.default<T>(context, factory))
        }*/
    }

//    @Inject
//    lateinit var iViewTypeFinder: ICoreViewHolderFactory

    // This Collection will not change on search
    private var _collection: List<Any> = ArrayList()
    var diffUtilCallBack : CoreDiffUtilCallback<*>? = null

    var collection:List<Any>
        get() = _collection
        set(value) {

            if(diffUtilCallBack != null){
                if(_collection.isEmpty()){
                    _collection = value
                    notifyDataSetChanged()
                } else {
                    diffUtilCallBack?.setOldAndNewList(
                        _collection,
                        value
                    )
                    val diffResult = DiffUtil.calculateDiff(diffUtilCallBack!!)
                    this._collection = value
                    diffResult.dispatchUpdatesTo(this)
                }
            } else{
                _collection = value
                this.notifyDataSetChanged()
            }
        }


    var itemClickListener: ItemClickListener? = null


    override fun getItemViewType(position: Int): Int {
        val data = collection.get(position)
        if(data is IDataViewTypeGetter){
            return data.getViewType()
        }

        throw IllegalArgumentException()
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

    override fun getItemCount(): Int {
        return collection.count()
    }

    override fun onBindViewHolder(
        holder: CoreViewHolder,
        position: Int
    ) {
        holder.IViewHolder.bind(collection[position])
        itemClickListener?.let {
            holder.itemView.setOnClickListener {
                itemClickListener?.onItemClick(it,position,collection[position])
            }
        }
    }
}