package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.giger_app.dataviewmodel.UpcomingGigSectionDVM
import com.gigforce.giger_app.repo.IUpcomingGigInfoRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingGigsComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {
    @Inject
    lateinit var repository: IUpcomingGigInfoRepository
    @Inject lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    init {
        this.setOrientationAndRows(0, 1)
    }

    override fun bind(data: Any?) {
        if (data is UpcomingGigSectionDVM) {
            repository.getData().observeForever {
                try {
                    if(sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                        super.bind(FeatureLayoutDVM(data.imageUrl, data.hi?.title?:data.title, it))
                    }else {
                        super.bind(FeatureLayoutDVM(data.imageUrl, data.title, it))
                    }
                } catch (e: Exception) {

                }
            }
        }
    }
}