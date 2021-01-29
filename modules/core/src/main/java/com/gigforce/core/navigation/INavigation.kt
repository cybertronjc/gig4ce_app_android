package com.gigforce.core.navigation

import android.content.Context
import android.os.Bundle
import androidx.navigation.NavOptions

interface INavigation {
    fun NavigateTo(context: Context, dest:String, args: Bundle? = null, navOptions: NavOptions? = null)
}