package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_app.R
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

        this.setSectionTitle("Scheduled Gigs")
        this.setSectionIcon()

        repository.getData().observeForever {
            Log.d("dataUpcomingGigCard", it.toString())
            if (it.size == 0) {
                this.findViewById<ConstraintLayout>(R.id.top_cl).gone()
            } else {
                this.findViewById<ConstraintLayout>(R.id.top_cl).visible()
                this.setCollection(it)
            }
        }
    }
}