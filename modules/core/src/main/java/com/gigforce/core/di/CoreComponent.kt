package com.gigforce.core.di

import com.gigforce.core.recyclerView.CoreRecyclerAdapter
import dagger.Subcomponent

@Subcomponent
interface ICoreComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ICoreComponent
    }

    fun inject(item:CoreRecyclerAdapter)
}