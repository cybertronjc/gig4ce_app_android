package com.gigforce.client_activation.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.client_activation.client_activation.dataviewmodel.ClientActivationLayoutDVM
import com.gigforce.client_activation.repo.IClientActivationDataRepository
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.common_ui.viewdatamodels.SeeMoreItemDVM
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClientActivationLayoutComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {
    @Inject
    lateinit var repository: IClientActivationDataRepository

    @Inject
    lateinit var navigation: INavigation

    override fun bind(data: Any?) {
        if(data is ClientActivationLayoutDVM){
            repository.getData().observeForever {
                if (it.size > data.showItem) {
                    var itemToShow = data.showItem
                    if (itemToShow == 0) {
                        super.bind(FeatureLayoutDVM("", "", emptyList()))
                    } else {
                        val list: List<Any> = it.slice(IntRange(0, itemToShow - 1))
                        list.toMutableList().add(SeeMoreItemDVM("", "", data.seeMoreNav))
                        super.bind(FeatureLayoutDVM(data.image,data.title, list))

                    }
                } else {
                    super.bind(FeatureLayoutDVM("", "", emptyList()))
                }

            }
        }
    }

}