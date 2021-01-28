package com.gigforce.core.di

import android.content.Context
import com.gigforce.core.DataViewObject
import com.gigforce.core.MyFragment
import com.gigforce.core.recyclerView.CoreRecyclerAdapter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface ICoreComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ICoreComponent
    }

    fun inject(item:MyFragment)
    fun inject(item: DataViewObject)
    fun inject(item:CoreRecyclerAdapter)
}