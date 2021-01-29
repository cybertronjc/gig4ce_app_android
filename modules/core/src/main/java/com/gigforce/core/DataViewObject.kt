package com.gigforce.core

import android.content.Context
import android.os.Bundle
import com.gigforce.core.navigation.INavigation
import javax.inject.Inject

interface IDataViewTypeGetter{
    fun getViewType():Int
}

abstract class DataViewObject() :
    IDataViewTypeGetter,
    INavArgsProvider
{
    override fun getNavArgs(): NavArgs? {
        return null
    }
}

abstract class SimpleDataViewObject(
    private val defaultViewType:Int,
    private val onClickNavPath:String? = null
):DataViewObject()
{
    override fun getViewType(): Int {
        return defaultViewType
    }

    override fun getNavArgs(): NavArgs? {
        onClickNavPath ?. let {
            return NavArgs(onClickNavPath, null)
        }
        return null
    }
}

interface INavigationProvider{
    fun getINavigation():INavigation
}

interface INavArgsProvider{
    fun getNavArgs():NavArgs?
}

data class NavArgs(
    val path:String,
    val args: Bundle?
) {

}