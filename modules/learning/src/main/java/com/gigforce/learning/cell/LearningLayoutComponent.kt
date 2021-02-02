package com.gigforce.learning.cell

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.cells.FeatureLayoutComponent
import com.gigforce.learning.ILearningDataRepository
import javax.inject.Inject

class LearningLayoutComponent(context: Context, attrs: AttributeSet?) :
        FeatureLayoutComponent(context, attrs)
{
    @Inject lateinit var respository : ILearningDataRepository
    init {

    }
}