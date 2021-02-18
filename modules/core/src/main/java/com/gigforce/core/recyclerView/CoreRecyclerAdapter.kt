package com.gigforce.core.recyclerView

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.CoreViewHolder
import com.gigforce.core.IDataViewTypeGetter
import com.gigforce.core.ICoreViewHolderFactory
import java.lang.IllegalArgumentException

open class CoreRecyclerAdapter(
    context: Context,
    val iViewTypeFinder: ICoreViewHolderFactory
) : RecyclerView.Adapter<CoreViewHolder>(){

    companion object {

        /*
        fun <T: View> default(context:Context, _collection: List<Any>, factory: (context:Context)->T):CoreRecyclerAdapter{
            return CoreRecyclerAdapter(context, _collection, CoreViewHolder.default<T>(context, factory))
        }*/
    }

//    @Inject
//    lateinit var iViewTypeFinder: ICoreViewHolderFactory

    private var _collection: List<Any> = ArrayList()
    var collection:List<Any>
        get() = _collection
        set(value) {
            _collection = value;
            this.notifyDataSetChanged();
        }

    override fun getItemViewType(position: Int): Int {
        val data = collection.get(position)
        Log.i("Core/RV", (data).toString())
        Log.i("Core/RV", (data is IDataViewTypeGetter).toString())
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
        holder.IViewHolder.bind(collection[position]);
    }
}