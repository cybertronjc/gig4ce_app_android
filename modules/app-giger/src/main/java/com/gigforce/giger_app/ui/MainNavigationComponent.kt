package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_app.R
import com.gigforce.giger_app.dataviewmodel.MainSectionDVM
import com.gigforce.giger_app.repo.IMainNavDataRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainNavigationComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {

    @Inject
    lateinit var repository: IMainNavDataRepository
    init {
        this.setOrientationAndRows(1, 4)
    }

    override fun bind(data: Any?) {
        if(data is MainSectionDVM){
            repository.getData().observeForever {
                try {
                    super.bind(FeatureLayoutDVM(data.imageUrl, data.title, it))
                }catch (e:Exception){}
            }
            super.bind(FeatureLayoutDVM(data.imageUrl, data.title, repository.getDefaultData()))
        }

    }

}