package com.gigforce.learning.ui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.learning.R
import com.gigforce.common_ui.cells.FeatureLayoutComponent
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.learning.repo.ILearningDataRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LearningLayoutComponent(context: Context, attrs: AttributeSet?) :
        FeatureLayoutComponent(context, attrs)
{
    @Inject lateinit var repository : ILearningDataRepository
    init {
        this.setSectionTitle("Learning")
        this.setSectionIcon()

        repository.getData().observeForever {
            if(it.size == 0){
                this.findViewById<ConstraintLayout>(R.id.top_cl).gone()
            }else {
                this.findViewById<ConstraintLayout>(R.id.top_cl).visible()
                this.setCollection(it)
            }
        }
    }
}