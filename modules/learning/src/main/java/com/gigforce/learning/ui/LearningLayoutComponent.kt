package com.gigforce.learning.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.cells.FeatureLayoutComponent
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
            this.setCollection(it)
        }
    }
}