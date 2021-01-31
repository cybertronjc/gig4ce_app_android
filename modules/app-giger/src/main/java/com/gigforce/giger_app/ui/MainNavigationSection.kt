package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.cells.FeatureLayoutComponent
import com.gigforce.giger_app.IMainNavDataRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainNavigationSection(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs)
{

    @Inject lateinit var repository:IMainNavDataRepository

    init {
        this.setOrientationAndRows(0,2)

        this.setSectionTitle("All Features")
        this.setSectionIcon()

        repository.getData().observeForever {
            this.setCollection(it)
        }
    }

}