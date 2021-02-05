package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.cells.FeatureLayoutComponent
import com.gigforce.giger_app.repo.IHelpVideosDataRepository
import com.gigforce.giger_app.repo.IUpcomingGigInfoRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingGigsComponent (context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs){
    @Inject
    lateinit var repository: IUpcomingGigInfoRepository

    init {
        this.setOrientationAndRows(0,1)

        this.setSectionTitle("Scheduled Gigs")
        this.setSectionIcon()

        repository.getData().observeForever {

            this.setCollection(it)
        }
    }
}