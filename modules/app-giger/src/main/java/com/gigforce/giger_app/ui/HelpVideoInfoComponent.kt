package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.cells.FeatureLayoutComponent
import com.gigforce.giger_app.repo.IHelpVideosDataRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HelpVideoInfoComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs){
    @Inject
    lateinit var repository: IHelpVideosDataRepository

    init {
        this.setOrientationAndRows(1,1)

        this.setSectionTitle("Help")
        this.setSectionIcon()

        repository.getData().observeForever {

            var data = it.slice(IntRange(0,1))
            this.setCollection(data)
        }
    }
}