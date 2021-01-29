package com.gigforce.core

import android.content.Context
import com.gigforce.core.di.CoreComponentProvider
import com.gigforce.core.navigation.INavigation
import javax.inject.Inject

interface IDataViewTypeGetter{
    fun getViewType():Int
}

abstract class DataViewObject() :IDataViewTypeGetter
{

}

abstract class SimpleDataViewObject(private val defaultViewType:Int):DataViewObject()
{
    override fun getViewType(): Int {
        return defaultViewType
    }
}

interface INavigationProvider{
    fun getINavigation():INavigation
}