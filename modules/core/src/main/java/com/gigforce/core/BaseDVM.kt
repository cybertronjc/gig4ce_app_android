package com.gigforce.core

import android.os.Bundle
import com.gigforce.core.navigation.INavigation

interface IDataViewTypeGetter{
    fun getViewType():Int
}

abstract class BaseDVM() :
    IDataViewTypeGetter,
    INavArgsProvider
{
    override fun getNavArgs(): NavArgs? {
        return null
    }
}

abstract class SimpleDVM(
    private val defaultViewType:Int,
    private val onClickNavPath:String? = null
):BaseDVM()
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
    val navPath:String,
    val args: Bundle?
) {

}