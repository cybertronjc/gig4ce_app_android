package com.gigforce.core.di.interfaces

import androidx.fragment.app.Fragment

interface INavHost {
    fun getFragment() : Fragment?
}