package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.giger_app.dataviewmodel.MainSectionDVM
import com.gigforce.giger_app.repo.IMainNavDataRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainNavigationComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {

    @Inject
    lateinit var repository: IMainNavDataRepository
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    init {
        this.setOrientationAndRows(1, 3)
    }

    override fun bind(data: Any?) {
        if (data is MainSectionDVM) {
            repository.getData().observeForever {
                try {
                    if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                        super.bind(
                            FeatureLayoutDVM(
                                data.imageUrl,
                                data.hi?.title ?: data.title,
                                it.filter { it.type == null || it.type == "" || it.type == "icon" || it.type == "folder" }
                            )
                        )
                    } else {
                        super.bind(FeatureLayoutDVM(data.imageUrl, data.title, it.filter { it.type == null || it.type == "" || it.type == "icon" || it.type == "folder" }))
                    }
                } catch (e: Exception) {
                }
            }
            super.bind(FeatureLayoutDVM(data.imageUrl, data.title, repository.getDefaultData(context)))
        }

    }

}