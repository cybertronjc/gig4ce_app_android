package com.gigforce.learning.learning

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.repository.repo.ILearningDataRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AssessmentLayoutComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs)
{
    @Inject
    lateinit var repository : ILearningDataRepository
    init {
        this.setSectionTitle("Learning")
        this.setSectionIcon()

//        repository.getData().observeForever {
//            if(it.size == 0){
//                this.findViewById<ConstraintLayout>(R.id.top_cl).gone()
//            }else {
//                this.findViewById<ConstraintLayout>(R.id.top_cl).visible()
//                this.setCollection(it)
//            }
//        }
    }
}