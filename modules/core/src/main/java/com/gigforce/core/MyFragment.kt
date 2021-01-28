package com.gigforce.core

import androidx.fragment.app.Fragment
import com.gigforce.core.di.CoreComponentProvider
import com.gigforce.core.di.ICoreComponent
import com.gigforce.core.navigation.INavigation
import javax.inject.Inject

open class MyFragment: Fragment() {

    @Inject
    lateinit var navigation: INavigation

    init {
        (this.context?.applicationContext as? CoreComponentProvider)?.provide()?.inject(this)
    }
}