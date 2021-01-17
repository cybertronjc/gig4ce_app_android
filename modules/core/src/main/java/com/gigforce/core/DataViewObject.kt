package com.gigforce.core

interface IDataViewTypeGetter{
    fun getViewType():Int
}

abstract class DataViewObject:IDataViewTypeGetter
{

}

abstract class SimpleDataViewObject(private val defaultViewType:Int):DataViewObject()
{
    override fun getViewType(): Int {
        return defaultViewType
    }
}