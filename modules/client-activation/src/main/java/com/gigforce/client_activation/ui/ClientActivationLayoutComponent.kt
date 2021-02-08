package com.gigforce.client_activation.ui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.client_activation.R
import com.gigforce.client_activation.repo.IClientActivationDataRepository
import com.gigforce.common_ui.cells.FeatureLayoutComponent
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
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
            if(it.size == 0){
                this.findViewById<ConstraintLayout>(R.id.top_cl).gone()
            }else {
                this.findViewById<ConstraintLayout>(R.id.top_cl).visible()
                this.setCollection(it)
            }
        }
    }
}