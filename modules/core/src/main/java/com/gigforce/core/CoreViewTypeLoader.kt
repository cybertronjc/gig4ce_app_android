package com.gigforce.core

import android.content.Context
import android.view.View
import java.lang.IllegalArgumentException

abstract class CoreViewHolderFactory: ICoreViewHolderFactory {

    private val viewTypeLoaders:ArrayList<IViewTypeLoader> = ArrayList()

    fun registerViewTypeLoader(loader:IViewTypeLoader){
        viewTypeLoaders.add(loader)
    }

    abstract fun registerAllViewTypeLoaders()

    init {
        registerAllViewTypeLoaders()
    }

    fun getView(context: Context, viewType: Int): View {
        var view:View? = null
        for(loader in viewTypeLoaders){
            view = loader.getView(context, viewType)
            view?.let {
                return it
            }
        }
        throw IllegalArgumentException()
    }

    override fun getViewHolder(context: Context, viewType:Int):CoreViewHolder{
        val view = getView(context, viewType)
        return CoreViewHolder(view)
    }
}

interface IViewTypeLoader{
    fun getView(context: Context, viewType: Int):View?
}