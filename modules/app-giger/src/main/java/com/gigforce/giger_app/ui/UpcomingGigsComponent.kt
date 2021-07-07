package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.giger_app.dataviewmodel.UpcomingGigSectionDVM
import com.gigforce.giger_app.repo.IUpcomingGigInfoRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingGigsComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {
    @Inject
    lateinit var repository: IUpcomingGigInfoRepository

    init {
        this.setOrientationAndRows(0, 1)
    }

    override fun bind(data: Any?) {
        if (data is UpcomingGigSectionDVM) {
            repository.getData().observeForever {
                try {
                    super.bind(FeatureLayoutDVM(data.imageUrl, data.title, it))
                } catch (e: Exception) {

                }
            }
        }
    }
}