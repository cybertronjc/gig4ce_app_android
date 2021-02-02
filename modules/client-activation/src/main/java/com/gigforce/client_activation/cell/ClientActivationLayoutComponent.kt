package com.gigforce.client_activation.cell

import android.content.Context
import android.util.AttributeSet
import com.gigforce.client_activation.IClientActivationDataRepository
import com.gigforce.common_ui.cells.FeatureLayoutComponent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClientActivationLayoutComponent (context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs)
{
    @Inject
    lateinit var repository : IClientActivationDataRepository
    init {
        this.setSectionTitle("Explore Gigs")
        this.setSectionIcon()

        repository.getData().observeForever {
            this.setCollection(it)
        }
    }
}