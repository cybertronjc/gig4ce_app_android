package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.R
import com.gigforce.giger_app.dataviewmodel.HelpVideosSectionDVM
import com.gigforce.giger_app.repo.IHelpVideosDataRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HelpVideoInfoComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {
    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var repository: IHelpVideosDataRepository

    init {
        this.setOrientationAndRows(1, 1)
    }

    override fun bind(data: Any?) {
        if (data is HelpVideosSectionDVM) {
            repository.getData().observeForever {
                if (it.isNotEmpty()) {
                    var videoToShow = data.showVideo
                    if (videoToShow == 0) {
                        super.bind(FeatureLayoutDVM("", "", emptyList()))
                    } else {
                        if(it.size>videoToShow){
                            enableSeemoreButton()
                        }
                        super.bind(FeatureLayoutDVM(data.imageUrl, data.title, it.slice(IntRange(0, videoToShow - 1))))

                    }
                } else {
                    super.bind(FeatureLayoutDVM("", "", emptyList()))
                }
            }

            this.findViewById<TextView>(R.id.layout_title).setOnClickListener {
                data.navPath?.let {
                    navigation.navigateTo(it)
                }
            }

            this.findViewById<View>(R.id.see_more_btn).setOnClickListener {
                data.navPath?.let {
                    navigation.navigateTo(it)
                }
            }
        }
    }
}