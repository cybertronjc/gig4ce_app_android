package com.gigforce.learning.ui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.learning.R
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.common_ui.repository.repo.ILearningDataRepository
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LearningLayoutComponent(context: Context, attrs: AttributeSet?) :
        FeatureLayoutComponent(context, attrs)
{
    @Inject lateinit var repository : ILearningDataRepository
    init {
        repository.getData().observeForever {
            bind(FeatureLayoutDVM("", "Learning", it))
        }
    }
}