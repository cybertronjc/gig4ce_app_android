package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.common_ui.cells.FeatureLayoutComponent
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_app.R
import com.gigforce.giger_app.repo.IMainNavDataRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainNavigationComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {

    @Inject
    lateinit var repository: IMainNavDataRepository

    init {
        this.setOrientationAndRows(0, 2)

        this.setSectionTitle("All Features")
        this.setSectionIcon()

        repository.getData().observeForever {
            if (it.size == 0) {
                this.findViewById<ConstraintLayout>(R.id.top_cl).gone()
            } else {
                this.findViewById<ConstraintLayout>(R.id.top_cl).visible()
                this.setCollection(it)
            }
        }
    }

}