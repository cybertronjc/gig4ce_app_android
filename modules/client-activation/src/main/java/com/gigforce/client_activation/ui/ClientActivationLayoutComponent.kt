package com.gigforce.client_activation.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.client_activation.repo.IClientActivationDataRepository
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClientActivationLayoutComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {
    @Inject
    lateinit var repository: IClientActivationDataRepository

    init {
        repository.getData().observeForever {
            bind(FeatureLayoutDVM("", "Explore Gigs", it))
        }
    }
}