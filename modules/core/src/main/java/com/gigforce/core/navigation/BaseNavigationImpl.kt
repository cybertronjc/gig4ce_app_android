package com.gigforce.core.navigation

import android.content.Context

 abstract class BaseNavigationImpl(): INavigation {

     private lateinit var _context:Context

     override var context: Context
         get() = _context
         set(value) {_context = value}

 }